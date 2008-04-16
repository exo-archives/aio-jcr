/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.storage.jdbc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

import javax.jcr.RepositoryException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.impl.storage.jdbc.db.GenericConnectionFactory;
import org.exoplatform.services.jcr.impl.storage.jdbc.db.MySQLConnectionFactory;
import org.exoplatform.services.jcr.impl.storage.jdbc.db.OracleConnectionFactory;
import org.exoplatform.services.jcr.impl.storage.jdbc.db.WorkspaceStorageConnectionFactory;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.DBInitializer;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.DBInitializerException;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.OracleDBInitializer;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.PgSQLDBInitializer;
import org.exoplatform.services.jcr.impl.storage.jdbc.update.StorageUpdateManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id:GenericWorkspaceDataContainer.java 13433 2007-03-15 16:07:23Z peterit $
 */
public class JDBCWorkspaceDataContainer extends WorkspaceDataContainerBase implements Startable {
  
  public final static String CONTAINER_NAME = "containerName";
  public final static String SOURCE_NAME = "sourceName";
  public final static String MULTIDB = "multi-db";
  public final static String SINGLEDB = "single-db";

  public final static String MAXBUFFERSIZE = "max-buffer-size";
  public final static String SWAPDIR = "swap-directory";
  
  /**
   * Describe which type of RDBMS will be used (DB creation metadata etc.)
   */
  public final static String DB_DIALECT = "dialect";
  public final static String DB_DRIVER = "driverClassName";
  public final static String DB_URL = "url";
  public final static String DB_USERNAME = "username";
  public final static String DB_PASSWORD = "password";

  /**
   * Describe which type of JDBC dialect will be used to iteract with RDBMS.
   * Used for type of ConnectionFactory decision.
   */
  public final static int                    DEF_MAXBUFFERSIZE = 1024 * 200;                                    // 200k

  public final static String                 DEF_SWAPDIR  = System.getProperty("java.io.tmpdir");

  protected static Log                       log = ExoLogger.getLogger("jcr.JDBCWorkspaceDataContainer");

  protected final String                     containerName;

  protected final String                     dbSourceName;

  protected final boolean                    multiDb;

  protected final String                     dbDriver;

  protected final String                     dbDialect;

  protected final String                     dbUrl;

  protected final String                     dbUserName;

  protected final String                     dbPassword;

  protected final ValueStoragePluginProvider valueStorageProvider;

  protected String                           storageVersion;

  protected int                              maxBufferSize;

  protected File                             swapDirectory;

  protected FileCleaner                      swapCleaner;

  protected GenericConnectionFactory         connFactory;

  class SharedConnectionFactory extends GenericConnectionFactory {

    final private Connection connection;

    SharedConnectionFactory(Connection connection,
        String containerName,
        boolean multiDb,
        ValueStoragePluginProvider valueStorageProvider,
        int maxBufferSize,
        File swapDirectory,
        FileCleaner swapCleaner) {

      super(null,
          null,
          null,
          null,
          null,
          containerName,
          multiDb,
          valueStorageProvider,
          maxBufferSize,
          swapDirectory,
          swapCleaner);

      this.connection = connection;
    }

    public Connection getJdbcConnection() throws RepositoryException {
      return connection;
    }
  }

  /**
   * Constructor with value storage plugins
   * @param wsConfig
   * @param valueStrorageProvider
   * @throws RepositoryConfigurationException
   * @throws NamingException
   */
  public JDBCWorkspaceDataContainer(WorkspaceEntry wsConfig,
      RepositoryEntry repConfig,
      InitialContextInitializer contextInit,
      ValueStoragePluginProvider valueStorageProvider) throws RepositoryConfigurationException,
      NamingException,
      RepositoryException,
      IOException {
    
    checkIntegrity(wsConfig, repConfig);
    
    this.containerName = wsConfig.getName();
    this.multiDb = Boolean.parseBoolean(wsConfig.getContainer().getParameterValue(MULTIDB));
    this.valueStorageProvider = valueStorageProvider;

    // ------------- Database config ------------------
    String pDbDialect = null;
    try {
      pDbDialect = detectDialect(wsConfig.getContainer().getParameterValue(DB_DIALECT));
      log.info("Using a dialect '" + pDbDialect + "'");
    } catch (RepositoryConfigurationException e) {
      log.info("Using a default dialect '" + DBConstants.DB_DIALECT_GENERIC + "'");
      pDbDialect = DBConstants.DB_DIALECT_GENERIC;
    }
    this.dbDialect = pDbDialect;

    String pDbDriver = null;
    String pDbUrl = null;
    String pDbUserName = null;
    String pDbPassword = null;
    try {
      pDbDriver = wsConfig.getContainer().getParameterValue(DB_DRIVER);

      // username/passwd may not pesent 
      try {
        pDbUserName = wsConfig.getContainer().getParameterValue(DB_USERNAME);
        pDbPassword = wsConfig.getContainer().getParameterValue(DB_PASSWORD);
      } catch (RepositoryConfigurationException e) {
        pDbUserName = pDbPassword = null;
      }

      pDbUrl = wsConfig.getContainer().getParameterValue(DB_URL); // last here!
    } catch (RepositoryConfigurationException e) {
    }

    if (pDbUrl != null) {
      this.dbDriver = pDbDriver;
      this.dbUrl = pDbUrl;
      this.dbUserName = pDbUserName;
      this.dbPassword = pDbPassword;
      this.dbSourceName = null;
      log.info("Connect to JCR database as user '" + this.dbUserName + "'");
    } else {
      this.dbDriver = null;
      this.dbUrl = null;
      this.dbUserName = null;
      this.dbPassword = null;
      this.dbSourceName = wsConfig.getContainer().getParameterValue(SOURCE_NAME);
    }

    // ------------- Values swap config ------------------
    try {
      String bsParam = wsConfig.getContainer().getParameterValue(MAXBUFFERSIZE);
      this.maxBufferSize = Integer.parseInt(bsParam);
    } catch (RepositoryConfigurationException e) {
      this.maxBufferSize = DEF_MAXBUFFERSIZE;
    }

    try {
      String sdParam = wsConfig.getContainer().getParameterValue(SWAPDIR);
      this.swapDirectory = new File(sdParam);
    } catch (RepositoryConfigurationException e1) {
      this.swapDirectory = new File(DEF_SWAPDIR);
    }
    if (!swapDirectory.exists())
      swapDirectory.mkdirs();

    this.swapCleaner = new FileCleaner(false);
    
    initDatabase();

    String suParam = null;
    boolean enableStorageUpdate = false;
    try {
      suParam = wsConfig.getContainer().getParameterValue("update-storage");
      enableStorageUpdate = Boolean.parseBoolean(suParam);
    } catch (RepositoryConfigurationException e) {
      log.debug("update-storage parameter is not set " + dbSourceName);
    }

    this.storageVersion = StorageUpdateManager.checkVersion(dbSourceName, this.connFactory
        .getJdbcConnection(), multiDb, enableStorageUpdate);

    // check for FileValueStorage
//    if (valueStorageProvider instanceof StandaloneStoragePluginProvider) {
//      WorkspaceStorageConnection conn = null;
//      try {
//        conn = openConnection();
//        ((StandaloneStoragePluginProvider) valueStorageProvider).checkConsistency(conn);
//      } finally {
//        if (conn != null)
//          conn.rollback();
//      }
//    }

    log.info(getInfo());
  }

  protected GenericConnectionFactory defaultConnectionFactory() throws NamingException,
      RepositoryException {
    // by default
    if (dbSourceName != null) {
      DataSource ds = (DataSource) new InitialContext().lookup(dbSourceName);
      if (ds != null)
        return new GenericConnectionFactory(ds,
            containerName,
            multiDb,
            valueStorageProvider,
            maxBufferSize,
            swapDirectory,
            swapCleaner);

      throw new RepositoryException("Datasource '" + dbSourceName
          + "' is not bound in this context.");
    }

    return new GenericConnectionFactory(dbDriver,
        dbUrl,
        dbUserName,
        dbPassword,
        containerName,
        multiDb,
        valueStorageProvider,
        maxBufferSize,
        swapDirectory,
        swapCleaner);
  }

  protected DBInitializer defaultDBInitializer(String sqlPath) throws NamingException,
      RepositoryException,
      IOException {
    return new DBInitializer(containerName, this.connFactory.getJdbcConnection(), sqlPath, multiDb);
  }

  protected void checkIntegrity(WorkspaceEntry wsConfig, RepositoryEntry repConfig) throws RepositoryConfigurationException {
    boolean isMulti;
    for (WorkspaceEntry wsEntry : repConfig.getWorkspaceEntries()) {
      if (wsEntry.getName().equals(wsConfig.getName())
          || !wsEntry.getContainer().getType().equals(wsConfig.getContainer().getType())
          || !wsEntry.getContainer().getType().equals(this.getClass().getName()))
        continue;
      
      // MULTIDB
      if (!wsEntry.getContainer().getParameterValue(MULTIDB).equals(wsConfig.getContainer()
          .getParameterValue(MULTIDB))) {
        throw new RepositoryConfigurationException("All workspaces must be "+MULTIDB+" or "+SINGLEDB+". But "
            + wsEntry.getName()
            + "- multi-db="
            + wsEntry.getContainer().getParameterValue(MULTIDB)
            + " and "
            + wsConfig.getName()
            + "- multi-db="
            + wsConfig.getContainer().getParameterValue(MULTIDB));
      }

      isMulti = Boolean.parseBoolean(wsConfig.getContainer().getParameterValue(MULTIDB));

      // source name
      String wsSourceName = null;
      String newWsSourceName = null;
      try {
        wsSourceName = wsEntry.getContainer().getParameterValue("sourceName");
        newWsSourceName = wsConfig.getContainer().getParameterValue("sourceName");
      } catch (RepositoryConfigurationException e) {
      }

      if (wsSourceName != null && newWsSourceName != null) {
        if (isMulti) {
          if (wsSourceName.equals(newWsSourceName)) {
            throw new RepositoryConfigurationException("SourceName " + wsSourceName
                + " alredy in use in " + wsEntry.getName()
                + ". SourceName must be different in "+MULTIDB+". Check configuration for "
                + wsConfig.getName());
          }
        } else {
          if (!wsSourceName.equals(newWsSourceName)) {
            throw new RepositoryConfigurationException("SourceName must be equals in "+SINGLEDB+" "
                + "repository." + " Check " + wsEntry.getName() + " and " + wsConfig.getName());
          }
        }
        continue;
      }
      
      // db-url
      String wsUri = null;
      String newWsUri = null;
      try {
        wsUri = wsEntry.getContainer().getParameterValue("db-url");
        newWsUri = wsConfig.getContainer().getParameterValue("db-url");
      } catch (RepositoryConfigurationException e) {
      }
      
      if (wsUri != null && newWsUri != null) {
        if (isMulti) {
          if (wsUri.equals(newWsUri)) {
            throw new RepositoryConfigurationException("db-url  " + wsUri + " alredy in use in "
                + wsEntry.getName()
                + ". db-url must be different in "+MULTIDB+". Check configuration for "
                + wsConfig.getName());

          }
        } else {
          if (!wsUri.equals(newWsUri)) {
            throw new RepositoryConfigurationException("db-url must be equals in "+SINGLEDB+" "
                + "repository." + " Check " + wsEntry.getName() + " and " + wsConfig.getName());
          }
        }
      }
    }
  }

  protected void initDatabase() throws NamingException, RepositoryException, IOException {

    DBInitializer dbInitilizer = null;
    String sqlPath = null;
    if (dbDialect == DBConstants.DB_DIALECT_ORACLEOCI) {
      log.warn(DBConstants.DB_DIALECT_ORACLEOCI + " dialect is experimental!");
      // sample of connection factory customization
      if (dbSourceName != null)
        this.connFactory = defaultConnectionFactory();
      else
        this.connFactory = new OracleConnectionFactory(dbDriver,
            dbUrl,
            dbUserName,
            dbPassword,
            containerName,
            multiDb,
            valueStorageProvider,
            maxBufferSize,
            swapDirectory,
            swapCleaner);

      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.ora.sql";

      // a particular db initializer may be configured here too
      dbInitilizer = new OracleDBInitializer(containerName,
          this.connFactory.getJdbcConnection(),
          sqlPath,
          multiDb);
    } else if (dbDialect == DBConstants.DB_DIALECT_ORACLE) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.ora.sql";
      dbInitilizer = new OracleDBInitializer(containerName,
          this.connFactory.getJdbcConnection(),
          sqlPath,
          multiDb);
    } else if (dbDialect == DBConstants.DB_DIALECT_PGSQL) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.pgsql.sql";
      dbInitilizer = new PgSQLDBInitializer(containerName,
          this.connFactory.getJdbcConnection(),
          sqlPath,
          multiDb);
    } else if (dbDialect == DBConstants.DB_DIALECT_MYSQL) {
      // [PN] 28.06.07
      if (dbSourceName != null) {
        DataSource ds = (DataSource) new InitialContext().lookup(dbSourceName);
        if (ds != null)
          this.connFactory = new MySQLConnectionFactory(ds,
              containerName,
              multiDb,
              valueStorageProvider,
              maxBufferSize,
              swapDirectory,
              swapCleaner);
        else
          throw new RepositoryException("Datasource '" + dbSourceName
              + "' is not bound in this context.");
      } else
        this.connFactory = new MySQLConnectionFactory(dbDriver,
            dbUrl,
            dbUserName,
            dbPassword,
            containerName,
            multiDb,
            valueStorageProvider,
            maxBufferSize,
            swapDirectory,
            swapCleaner);
      
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.mysql.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbDialect == DBConstants.DB_DIALECT_MYSQL_UTF8) {
      // [PN] 13.07.08
      if (dbSourceName != null) {
        DataSource ds = (DataSource) new InitialContext().lookup(dbSourceName);
        if (ds != null)
          this.connFactory = new MySQLConnectionFactory(ds,
              containerName,
              multiDb,
              valueStorageProvider,
              maxBufferSize,
              swapDirectory,
              swapCleaner);
        else
          throw new RepositoryException("Datasource '" + dbSourceName
              + "' is not bound in this context.");
      } else
        this.connFactory = new MySQLConnectionFactory(dbDriver,
            dbUrl,
            dbUserName,
            dbPassword,
            containerName,
            multiDb,
            valueStorageProvider,
            maxBufferSize,
            swapDirectory,
            swapCleaner);
      
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.mysql-utf8.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbDialect == DBConstants.DB_DIALECT_MSSQL) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.mssql.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbDialect == DBConstants.DB_DIALECT_DERBY) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.derby.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbDialect == DBConstants.DB_DIALECT_DB2) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.db2.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbDialect == DBConstants.DB_DIALECT_DB2V8) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.db2v8.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbDialect == DBConstants.DB_DIALECT_SYBASE) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.sybase.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else {
      // generic, DB_HSQLDB
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    }

    // database type
    try {
      dbInitilizer.init();
    } catch (DBInitializerException e) {
      log.error("Error of init db " + e, e);
    }
  }

  protected GenericConnectionFactory getConnectionFactory() {
    return connFactory;
  }

  protected String detectDialect(String confParam) {
    for (String dbType : DBConstants.DB_DIALECTS) {
      if (dbType.equalsIgnoreCase(confParam))
        return dbType;
    }

    return DBConstants.DB_DIALECT_GENERIC; // by default
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#openConnection()
   */
  public WorkspaceStorageConnection openConnection() throws RepositoryException {

    return connFactory.openConnection();
  }

  public WorkspaceStorageConnection reuseConnection(WorkspaceStorageConnection original) throws RepositoryException {

    if (original instanceof JDBCStorageConnection) {
      WorkspaceStorageConnectionFactory cFactory = new SharedConnectionFactory(((JDBCStorageConnection) original)
          .getJdbcConnection(),
          containerName,
          multiDb,
          valueStorageProvider,
          maxBufferSize,
          swapDirectory,
          swapCleaner);

      return cFactory.openConnection();
    } else {
      return openConnection();
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#getName()
   */
  public String getName() {
    return containerName;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#getInfo()
   */
  public String getInfo() {
    String str = "JDBC based JCR Workspace Data container \n" + "container name: " + containerName
        + " \n" + "data source JNDI name: " + dbSourceName + "\n" + "is multi database: " + multiDb
        + "\n" + "storage version: " + storageVersion + "\n" + "value storage provider: "
        + valueStorageProvider + "\n" + "max buffer size (bytes): " + maxBufferSize + "\n"
        + "swap directory path: " + swapDirectory.getAbsolutePath();
    return str;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.DataContainer#getStorageVersion()
   */
  public String getStorageVersion() {
    return storageVersion;
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    this.swapCleaner.start(); 
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    this.swapCleaner.halt();
    this.swapCleaner.interrupt();

    //TODO HSQLDB Stop
    // if (dbDialect.equals(DB_DIALECT_GENERIC) ||
    // dbDialect.equals(DB_DIALECT_HSQLDB)) {
    // // shutdown in-process HSQLDB database
    // System.out.println("Shutdown in-process HSQLDB database...");
    // try {
    // JDBCStorageConnection conn = (JDBCStorageConnection) openConnection();
    // Connection jdbcConn = conn.getJdbcConnection();
    // String dbUrl = jdbcConn.getMetaData().getURL();
    // if (dbUrl.startsWith("jdbc:hsqldb:file") ||
    // dbUrl.startsWith("jdbc:hsqldb:mem")) {
    // // yeah, there is in-process hsqldb, shutdown it now
    // jdbcConn.createStatement().execute("SHUTDOWN");
    // System.out.println("Shutdown in-process HSQLDB database... done.");
    // }
    // } catch (Throwable e) {
    // log.error("JDBC Data container stop error " + e);
    // e.printStackTrace();
    // }
    // }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    if (obj instanceof JDBCWorkspaceDataContainer) {
      JDBCWorkspaceDataContainer another = (JDBCWorkspaceDataContainer) obj;

      if (getDbSourceName() != null)
        // by jndi ds name
        return getDbSourceName().equals(another.getDbSourceName());

      // by db connection params
      return getDbDriver().equals(another.getDbDriver()) && getDbUrl().equals(another.getDbUrl())
          && getDbUserName().equals(another.getDbUserName());
    }

    return false;
  }

  protected String getDbSourceName() {
    return dbSourceName;
  }

  protected String getDbDriver() {
    return dbDriver;
  }

  protected String getDbUrl() {
    return dbUrl;
  }

  protected String getDbUserName() {
    return dbUserName;
  }

  // ------------------ development code ---------------

  @Deprecated
  protected GenericConnectionFactory connectionFactory(String dbType) throws NamingException,
      RepositoryException {
    try {
      String cfClassName = "org.exoplatform.services.jcr.impl.storage.jdbc." + dbType
          + "ConnectionFactory";
      Class<? extends GenericConnectionFactory> cfClass = (Class<? extends GenericConnectionFactory>) Class
          .forName(cfClassName);

      if (dbSourceName != null) {
        DataSource dataSource = (DataSource) new InitialContext().lookup(dbSourceName);
        if (dataSource != null) {
          Constructor<? extends GenericConnectionFactory> cfConstructor = cfClass
              .getConstructor(new Class[] { DataSource.class, String.class, boolean.class,
                  ValueStoragePluginProvider.class, int.class, File.class, FileCleaner.class });

          return cfConstructor.newInstance(new Object[] { dataSource, containerName, multiDb,
              valueStorageProvider, maxBufferSize, swapDirectory, swapCleaner });
        }
      }

      Constructor<? extends GenericConnectionFactory> cfConstructor = cfClass
          .getConstructor(new Class[] { String.class, String.class, String.class, String.class,
              String.class, boolean.class, ValueStoragePluginProvider.class, int.class, File.class,
              FileCleaner.class });

      return cfConstructor
          .newInstance(new Object[] { dbDriver, dbUrl, dbUserName, dbPassword, containerName,
              multiDb, valueStorageProvider, maxBufferSize, swapDirectory, swapCleaner });

    } catch (ClassNotFoundException e) {
      // no definition for the class with the specified name could be found
      if (log.isDebugEnabled())
        log.warn("No definition for the connection factory of database type " + dbType
            + " is found. A generic one will be used. " + e, e);
      else
        log.warn("No definition for the connection factory of database type " + dbType
            + " is found. A generic one will be used. " + e);

      return defaultConnectionFactory();

    } catch (NoSuchMethodException e) {
      // a particular method cannot be found
      throw new RepositoryException(e);
    } catch (InstantiationException e) {
      // it is an interface or is an abstract class
      throw new RepositoryException(e);
    } catch (IllegalArgumentException e) {
      // a method has been passed an illegal or inappropriate argument
      throw new RepositoryException(e);
    } catch (InvocationTargetException e) {
      // an exception thrown by an invoked method or constructor
      throw new RepositoryException(e);
    } catch (SecurityException e) {
      // a security violation
      throw new RepositoryException(e);
    } catch (IllegalAccessException e) {
      // the currently executing method does not have access to the definition of 
      // the specified class, field, method or constructor
      throw new RepositoryException(e);
    }
  }

  @Deprecated
  protected DBInitializer dbInitializer(String dbType, Connection connection, String scriptPath) throws NamingException,
      RepositoryException,
      IOException {
    try {
      String dbiClassName = "org.exoplatform.services.jcr.impl.storage.jdbc.init." + dbType
          + "DBInitializer";
      Class<? extends DBInitializer> cfClass = (Class<? extends DBInitializer>) Class
          .forName(dbiClassName);

      Constructor<? extends DBInitializer> cfConstructor = cfClass.getConstructor(new Class[] {
          String.class, Connection.class, String.class });

      return cfConstructor.newInstance(new Object[] { containerName, connection, scriptPath });

    } catch (ClassNotFoundException e) {
      // no definition for the class with the specified name could be found
      if (log.isDebugEnabled())
        log.warn("No definition for the connection factory of database type " + dbType
            + " is found. A generic one will be used. " + e, e);
      else
        log.warn("No definition for the connection factory of database type " + dbType
            + " is found. A generic one will be used. " + e);

      return defaultDBInitializer(scriptPath);

    } catch (NoSuchMethodException e) {
      // a particular method cannot be found
      throw new RepositoryException(e);
    } catch (InstantiationException e) {
      // it is an interface or is an abstract class
      throw new RepositoryException(e);
    } catch (IllegalArgumentException e) {
      // a method has been passed an illegal or inappropriate argument
      throw new RepositoryException(e);
    } catch (InvocationTargetException e) {
      // an exception thrown by an invoked method or constructor
      throw new RepositoryException(e);
    } catch (SecurityException e) {
      // a security violation
      throw new RepositoryException(e);
    } catch (IllegalAccessException e) {
      // the currently executing method does not have access to the definition of 
      // the specified class, field, method or constructor
      throw new RepositoryException(e);
    }
  }
}
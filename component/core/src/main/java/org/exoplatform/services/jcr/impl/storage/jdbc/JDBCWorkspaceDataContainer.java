/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
import org.exoplatform.services.jcr.impl.storage.jdbc.db.OracleConnectionFactory;
import org.exoplatform.services.jcr.impl.storage.jdbc.db.WorkspaceStorageConnectionFactory;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.DBInitializer;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.DBInitializerException;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.OracleDBInitializer;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.PgSQLDBInitializer;
import org.exoplatform.services.jcr.impl.storage.jdbc.update.StorageUpdateManager;
import org.exoplatform.services.jcr.impl.storage.value.StandaloneStoragePluginProvider;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id:GenericWorkspaceDataContainer.java 13433 2007-03-15 16:07:23Z peterit $
 */
public class JDBCWorkspaceDataContainer extends WorkspaceDataContainerBase implements Startable{
  
  public final static String CONTAINER_NAME = "containerName";
  public final static String SOURCE_NAME = "sourceName";
  public final static String MULTIDB = "multi-db";

  public final static String MAXBUFFERSIZE = "max-buffer-size";
  public final static String SWAPDIR = "swap-directory";
  
  /**
   * Describe which type of RDBMS will be used (DB creation metadata etc.)
   */
  public final static String DB_TYPE = "db-type";
  public final static String DB_DRIVER = "db-driver";
  public final static String DB_URL = "db-url";
  public final static String DB_USERNAME = "db-username";
  public final static String DB_PASSWORD = "db-password";
  
  public final static String DB_TYPE_GENERIC = "Generic".intern();
  public final static String DB_TYPE_ORACLE = "Oracle".intern();
  public final static String DB_TYPE_ORACLEOCI = "Oracle-OCI".intern();
  public final static String DB_TYPE_PGSQL = "PgSQL".intern();
  public final static String DB_TYPE_MYSQL = "MySQL".intern();
  public final static String DB_TYPE_HSQLDB = "HSQLDB".intern();
  public final static String DB_TYPE_DB2 = "DB2".intern();
  public final static String DB_TYPE_MSSQL = "MSSQL".intern();
  public final static String DB_TYPE_SYBASE = "Sybase".intern();
  public final static String DB_TYPE_DERBY = "Derby".intern();
  
   
  
  public final static String[] DB_TYPES = {DB_TYPE_GENERIC, DB_TYPE_ORACLE, DB_TYPE_ORACLEOCI, DB_TYPE_PGSQL, 
    DB_TYPE_MYSQL, DB_TYPE_HSQLDB, DB_TYPE_DB2, DB_TYPE_MSSQL, DB_TYPE_SYBASE, DB_TYPE_DERBY};

  /**
   * Describe which type of JDBC dialect will be used to iteract with RDBMS.
   * Used for type of ConnectionFactory decision.
   */
  public final static String                 DB_DIALECT        = "db-dialect";

  public final static int                    DEF_MAXBUFFERSIZE = 1024 * 200;                                    // 200k

  public final static String                 DEF_SWAPDIR       = System
                                                                   .getProperty("java.io.tmpdir");

  protected static Log                       log               = ExoLogger
                                                                   .getLogger("jcr.JDBCWorkspaceDataContainer");

  protected final String                     containerName;

  protected final String                     dbSourceName;

  protected final boolean                    multiDb;

  protected final String                     dbDriver;

  protected final String                     dbType;

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
      //LogConfigurationInitializer logCongig,
      ValueStoragePluginProvider valueStorageProvider) throws RepositoryConfigurationException,
      NamingException,
      RepositoryException,
      IOException {
    checkIntegrity(wsConfig, repConfig);
    this.containerName = wsConfig.getName();
    this.multiDb = Boolean.parseBoolean(wsConfig.getContainer().getParameterValue(MULTIDB));
    this.valueStorageProvider = valueStorageProvider;

    // ------------- Database config ------------------
    // dbType
    String pDbType = null;
    try {
      pDbType = detectDialect(wsConfig.getContainer().getParameterValue(DB_TYPE));
      log.info("Using a db-type '" + pDbType + "'");
    } catch (RepositoryConfigurationException e) {
      log.info("Using a default db-type '" + DB_TYPE_GENERIC + "'");
      pDbType = DB_TYPE_GENERIC;
    }
    this.dbType = pDbType;

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

    //Context context = new InitialContext();
    //DataSource dataSource = (DataSource) context.lookup(dbSourceName);

    initDatabase();

    String suParam = null;
    boolean enableStorageUpdate = false;
    try {
      suParam = wsConfig.getContainer().getParameterValue("update-storage");
      enableStorageUpdate = Boolean.parseBoolean(suParam);
    } catch (RepositoryConfigurationException e) {
      log.debug("update-storage parameter is not set " + dbSourceName);
    }

    //checkVersion(dataSource, enableStorageUpdate);
    this.storageVersion = StorageUpdateManager.checkVersion(dbSourceName, this.connFactory
        .getJdbcConnection(), multiDb, enableStorageUpdate);

    // check for FileValueStorage
    if (valueStorageProvider instanceof StandaloneStoragePluginProvider) {
      WorkspaceStorageConnection conn = null;
      try {
        conn = openConnection();
        ((StandaloneStoragePluginProvider) valueStorageProvider).checkConsistency(conn);
      } finally {
        if (conn != null)
          conn.rollback();
      }
    }

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
      
      // multi-db
      if (!wsEntry.getContainer().getParameterValue("multi-db").equals(wsConfig.getContainer()
          .getParameterValue("multi-db"))) {
        throw new RepositoryConfigurationException("All workspaces must be multi-db or single-db. But "
            + wsEntry.getName()
            + "- multi-db:"
            + wsEntry.getContainer().getParameterValue("multi-db")
            + " and "
            + wsConfig.getName()
            + "- multi-db:"
            + wsConfig.getContainer().getParameterValue("multi-db"));
      }

      isMulti = Boolean.parseBoolean(wsConfig.getContainer().getParameterValue("multi-db"));

      // source name
      String wsSourceName = null;
      String newWsSourceName = null;
      try {
        wsSourceName = wsEntry.getContainer().getParameterValue("sourceName");
        newWsSourceName = wsConfig.getContainer().getParameterValue("sourceName");
      } catch (RuntimeException e1) {
      }

      if (wsSourceName != null && newWsSourceName != null) {
        if (isMulti) {
          if (wsSourceName.equals(newWsSourceName)) {
            throw new RepositoryConfigurationException("SourceName " + wsSourceName
                + " alredy in use in " + wsEntry.getName()
                + ".SourceName must be different in multi-db. Check configuration for "
                + wsConfig.getName());
          }
        } else {
          if (!wsSourceName.equals(newWsSourceName)) {
            throw new RepositoryConfigurationException("SourceName must be equals in single-db "
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
                + ". db-url must be different in multi-db. Check configuration for "
                + wsConfig.getName());

          }
        } else {
          if (!wsUri.equals(newWsUri)) {
            throw new RepositoryConfigurationException("db-url must be equals in single-db "
                + "repository." + " Check " + wsEntry.getName() + " and " + wsConfig.getName());
          }
        }
      }
    }
  }

  protected void initDatabase() throws NamingException, RepositoryException, IOException {

    DBInitializer dbInitilizer = null;
    String sqlPath = null;
    if (dbType == DB_TYPE_ORACLEOCI) {
      //if (multiDb)
      //  throw new RepositoryConfigurationException("Oracle multi database option is not supported now, try to use multi-db=false");

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

      //sqlPath = "/conf/storage/jcr-sjdbc.ora.sql";
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.ora.sql";

      // a particular db initializer may be configured here too
      dbInitilizer = new OracleDBInitializer(containerName,
          this.connFactory.getJdbcConnection(),
          sqlPath,
          multiDb);
    } else if (dbType == DB_TYPE_ORACLE) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.ora.sql";
      dbInitilizer = new OracleDBInitializer(containerName,
          this.connFactory.getJdbcConnection(),
          sqlPath,
          multiDb);
    } else if (dbType == DB_TYPE_PGSQL) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.pgsql.sql";
      dbInitilizer = new PgSQLDBInitializer(containerName,
          this.connFactory.getJdbcConnection(),
          sqlPath,
          multiDb);
    } else if (dbType == DB_TYPE_MYSQL) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.mysql.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbType == DB_TYPE_MSSQL) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.mssql.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbType == DB_TYPE_DERBY) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.derby.sql";
    } else if (dbType == DB_TYPE_DB2) {
      this.connFactory = defaultConnectionFactory();
      sqlPath = "/conf/storage/jcr-" + (multiDb ? "m" : "s") + "jdbc.db2.sql";
      dbInitilizer = defaultDBInitializer(sqlPath);
    } else if (dbType == DB_TYPE_SYBASE) {
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
    for (String dbType : DB_TYPES) {
      if (dbType.equalsIgnoreCase(confParam))
        return dbType;
    }

    return DB_TYPE_GENERIC; // by default
  }

  // check version of the database
//  public String checkVersion(DataSource dataSource, boolean enableStorageUpdate) throws RepositoryException {
//    this.storageVersion = StorageUpdateManager.checkVersion(dbSourceName, dataSource, multiDb, enableStorageUpdate);
//    
//    return this.storageVersion;
//  }

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
    this.swapCleaner = new FileCleaner();
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    this.swapCleaner.halt();
    this.swapCleaner.interrupt();

    if (dbType.equals(DB_TYPE_GENERIC) || dbType.equals(DB_TYPE_HSQLDB)) {
      // shutdown in-process HSQLDB database
      System.out.println("Shutdown in-process HSQLDB database...");
      try {
        JDBCStorageConnection conn = (JDBCStorageConnection) openConnection();
        Connection jdbcConn = conn.getJdbcConnection();
        String dbUrl = jdbcConn.getMetaData().getURL();
        if (dbUrl.startsWith("jdbc:hsqldb:file") || dbUrl.startsWith("jdbc:hsqldb:mem")) {
          // yeah, there is in-process hsqldb, shutdown it now
          jdbcConn.createStatement().execute("SHUTDOWN");
          System.out.println("Shutdown in-process HSQLDB database... done.");
        }
      } catch (Throwable e) {
        log.error("JDBC Data container stop error " + e);
        e.printStackTrace();
      }
    }
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
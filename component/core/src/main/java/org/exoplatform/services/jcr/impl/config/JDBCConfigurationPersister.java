/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.   *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.config.ConfigurationPersister;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;

/**
 * Repository service configuration persister
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class JDBCConfigurationPersister implements ConfigurationPersister {

  public final static String PARAM_SOURCE_NAME = "sourceName";
  public final static String PARAM_DIALECT = "dialect";
  
  public final static String DB_DIALECT_GENERIC = "Generic".intern();
  public final static String DB_DIALECT_ORACLE = "Oracle".intern();
  public final static String DB_DIALECT_PGSQL = "PgSQL".intern();
  public final static String DB_DIALECT_MYSQL = "MySQL".intern();
  public final static String DB_DIALECT_HSQLDB = "HSQLDB".intern();
  public final static String DB_DIALECT_DB2 = "DB2".intern();
  public final static String DB_DIALECT_MSSQL = "MSSQL".intern();
  public final static String DB_DIALECT_SYBASE = "Sybase".intern();
  public final static String DB_DIALECT_DERBY = "Derby".intern();
  
  protected static final String CONFIGNAME = "REPOSITORY-SERVICE-WORKING-CONFIG"; 
  protected static final String C_DATA = "CONFIGDATA";
  
  protected static final String CONFIG_TABLENAME = "JCR_CONFIG";
  
  protected String sourceName;
  protected String dialect;
  
  public class ConfigurationNotFoundException extends RepositoryConfigurationException {
    ConfigurationNotFoundException(String m) {
      super(m);
    }
  }
  
  public class ConfigurationNotInitializedException extends RepositoryConfigurationException {
    ConfigurationNotInitializedException(String m) {
      super(m);
    }
  }
  
  protected class ConfigDataHolder {
    
    private final byte[] config;
    
    ConfigDataHolder(InputStream source) throws IOException {
      ByteArrayOutputStream configOut = new ByteArrayOutputStream();
      byte[] b = new byte[1024];
      int read = 0;
      while ((read = source.read(b))>0) {
        configOut.write(b, 0, read);
      }
      this.config = configOut.toByteArray(); 
    }
    
    InputStream getStream() {
      return new ByteArrayInputStream(config);
    }
    
    int getLength() {
      return config.length;
    }
  }
  
  public void init(PropertiesParam params) throws RepositoryConfigurationException {
    String sourceNameParam = params.getProperty(PARAM_SOURCE_NAME);
    String dialectParam = params.getProperty(PARAM_DIALECT);
    if (sourceNameParam == null)
      throw new RepositoryConfigurationException("Repository service configuration. Source name (sourceName) is expected");
    
    this.sourceName = sourceNameParam;
    this.dialect = dialectParam;
  }
  
  protected void checkInitialized() throws RepositoryConfigurationException {
    if (sourceName == null)
      throw new RepositoryConfigurationException("Repository service configuration persister isn not initialized. Call init() before.");
  }
  
  protected Connection openConnection() throws NamingException, SQLException {
    DataSource ds = (DataSource) new InitialContext().lookup(sourceName);
    return ds.getConnection();
  }
  
  /**
   * Check if config table already exists 
   * @param con
   */
  protected boolean isDbInitialized(Connection con) {
    try {
      ResultSet trs = con.getMetaData().getTables(null, null, CONFIG_TABLENAME, null);
      return trs.next();
    } catch(SQLException e) {
      return false;
    }
  }
  
  public boolean hasConfig() throws RepositoryConfigurationException {
    
    checkInitialized();
    
    try {
      Connection con = openConnection();
      if (isDbInitialized(con)) {
        // check that data exists
        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM " + CONFIG_TABLENAME + " WHERE NAME=?");
        try {
          ps.setString(1, CONFIGNAME);
          ResultSet res = ps.executeQuery();
          if (res.next()) {
            return res.getInt(1) > 0;
          }
        } finally {
          con.close();
        }
      }
      return false; 
    } catch(final SQLException e) {
      throw new RepositoryConfigurationException("Database exception. " + e, e);
    } catch(final NamingException e) {
      throw new RepositoryConfigurationException("JDNI exception. " + e, e);
    }
  }

  
  
  public InputStream read() throws RepositoryConfigurationException {
    
    checkInitialized();
    
    try {
      Connection con = openConnection();
      try {
        if (isDbInitialized(con)) {
          
          PreparedStatement ps = con.prepareStatement("SELECT * FROM " + CONFIG_TABLENAME + " WHERE name=?");
          ps.setString(1, CONFIGNAME);
          ResultSet res = ps.executeQuery();
          
          if (res.next()) {
            ConfigDataHolder config = new ConfigDataHolder(res.getBinaryStream("config"));
            return config.getStream();
          } else
            throw new ConfigurationNotFoundException("No configuration data is found in database. Source name " + sourceName);
            
        } else
          throw new ConfigurationNotInitializedException("Configuration table not is found in database. Source name " + sourceName);          
        
      } finally {
        con.close();
      }
    } catch(final IOException e) {
      throw new RepositoryConfigurationException("Configuration read exception. " + e, e);
    } catch(final SQLException e) {
      throw new RepositoryConfigurationException("Database exception. " + e, e);
    } catch(final NamingException e) {
      throw new RepositoryConfigurationException("JDNI exception. " + e, e);
    }
  }

  public void write(InputStream confData) throws RepositoryConfigurationException {
    
    checkInitialized();
    
    try {
      Connection con = openConnection();
      try {
        if (!isDbInitialized(con)) {
          // init db
          String binType = "BLOB";
          if (dialect != null)
            if (dialect.equalsIgnoreCase(DB_DIALECT_GENERIC) || dialect.equalsIgnoreCase(DB_DIALECT_HSQLDB)) {
              binType = "VARBINARY(102400)"; // 100Kb
            } else if (dialect.equalsIgnoreCase(DB_DIALECT_PGSQL)) {
              binType = "BYTEA";
            } else if (dialect.equalsIgnoreCase(DB_DIALECT_MSSQL)) {
              binType = "VARBINARY(max)";
            } else if (dialect.equalsIgnoreCase(DB_DIALECT_SYBASE)) {
              binType = "VARBINARY(255)";
            }
          
          String sql = "CREATE TABLE " + CONFIG_TABLENAME + " (" +
              "NAME VARCHAR(64) NOT NULL, " +
              "CONFIG " + binType + " NOT NULL, " +
              "CONSTRAINT JCR_CONFIG_PK PRIMARY KEY(NAME))";
          con.createStatement().executeUpdate(sql);
        } 
        
        if (isDbInitialized(con)) {
          
          PreparedStatement ps = null;
          
          ConfigDataHolder config = new ConfigDataHolder(confData);
          
          if (hasConfig()) {
            ps = con.prepareStatement("UPDATE " + CONFIG_TABLENAME + " SET CONFIG=? WHERE NAME=?");
            ps.setBinaryStream(1, config.getStream(), config.getLength()); 
            ps.setString(2, CONFIGNAME);
          } else {
            ps = con.prepareStatement("INSERT INTO " + CONFIG_TABLENAME + "(NAME, CONFIG) VALUES (?,?)");
            ps.setString(1, CONFIGNAME);
            ps.setBinaryStream(2, config.getStream(), config.getLength());
          }
          
          if (ps.executeUpdate()<=0) {
            System.out.println(this.getClass().getCanonicalName() 
                + " [WARN] Repository service configuration doesn't stored ok. No rows was affected in JDBC operation. Datasource " + sourceName);
          }
        } else
          throw new ConfigurationNotInitializedException("Configuration table can not be created in database. Source name " + sourceName);          
        
      } finally {
        con.close();
      }
    } catch(final IOException e) {
      throw new RepositoryConfigurationException("Configuration read exception. " + e, e);
    } catch(final SQLException e) {
      throw new RepositoryConfigurationException("Database exception. " + e, e);
    } catch(final NamingException e) {
      throw new RepositoryConfigurationException("JDNI exception. " + e, e);
    }
  }

}

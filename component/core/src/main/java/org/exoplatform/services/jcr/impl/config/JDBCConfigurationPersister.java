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

import org.exoplatform.services.jcr.config.ConfigurationPersister;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;

public class JDBCConfigurationPersister implements ConfigurationPersister {

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
  
  protected static final String CONFIG_TABLENAME = "jcr_config";
  
  protected final String sourceName;
  protected final String dialect;
  
  public class JDBCRepositoryConfigurationNotFoundException extends RepositoryConfigurationException {
    JDBCRepositoryConfigurationNotFoundException(String m) {
      super(m);
    }
  }
  
  public class JDBCRepositoryConfigurationNotInitializedException extends RepositoryConfigurationException {
    JDBCRepositoryConfigurationNotInitializedException(String m) {
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
  
  public JDBCConfigurationPersister(String sourceName, String dialect) {
    this.sourceName = sourceName;
    this.dialect = dialect;
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
    try {
      Connection con = openConnection();
      if (isDbInitialized(con)) {
        // check that data exists
        PreparedStatement ps = con.prepareStatement("select count(*) from " + CONFIG_TABLENAME + " where name=?");
        try {
          ps.setString(1, CONFIGNAME);
          ResultSet res = ps.executeQuery();
          return res.next();
        } finally {
          con.close();
        }
      } else
        return false;
    } catch(final SQLException e) {
      throw new RepositoryConfigurationException("Database exception. " + e, e);
    } catch(final NamingException e) {
      throw new RepositoryConfigurationException("JDNI exception. " + e, e);
    }
  }

  
  
  public InputStream read() throws RepositoryConfigurationException {
    try {
      Connection con = openConnection();
      try {
        if (isDbInitialized(con)) {
          
          PreparedStatement ps = con.prepareStatement("select * from " + CONFIG_TABLENAME + " where name=?");
          ps.setString(1, CONFIGNAME);
          ResultSet res = ps.executeQuery();
          
          if (res.next()) {
            ConfigDataHolder config = new ConfigDataHolder(res.getBinaryStream("config"));
            return config.getStream();
          } else
            throw new JDBCRepositoryConfigurationNotFoundException("No configuration data is found in database. Source name " + sourceName);
            
        } else
          throw new JDBCRepositoryConfigurationNotInitializedException("Configuration table not is found in database. Source name " + sourceName);          
        
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
    try {
      Connection con = openConnection();
      try {
        if (!isDbInitialized(con)) {
          // init db
          String binType = "blob";
          if (dialect.equalsIgnoreCase(DB_DIALECT_GENERIC) || dialect.equalsIgnoreCase(DB_DIALECT_HSQLDB)) {
            binType = "varbinary(102400)"; // 100Kb
          } else if (dialect.equalsIgnoreCase(DB_DIALECT_PGSQL)) {
            binType = "bytea";
          } else if (dialect.equalsIgnoreCase(DB_DIALECT_MSSQL)) {
            binType = "varbinary(max)";
          } else if (dialect.equalsIgnoreCase(DB_DIALECT_SYBASE)) {
            binType = "varbinary(255)";
          }
          
          String sql = "create table " + CONFIG_TABLENAME + " (" +
              "name varchar(64) not null, " +
              "config " + binType + " not null, " +
              "constraint jcr_config_pk primary key(name))";
          con.createStatement().executeUpdate(sql);
        } 
        
        if (isDbInitialized(con)) {
          
          PreparedStatement ps = null;
          
          ConfigDataHolder config = new ConfigDataHolder(confData);
          
          if (hasConfig()) {
            ps = con.prepareStatement("update " + CONFIG_TABLENAME + " set config=? where name=?");
            ps.setBinaryStream(1, config.getStream(), config.getLength()); 
            ps.setString(2, CONFIGNAME);
          } else {
            ps = con.prepareStatement("insert into " + CONFIG_TABLENAME + "(name, config) values (?,?)");
            ps.setString(1, CONFIGNAME);
            ps.setBinaryStream(2, config.getStream(), config.getLength());
          }
          
          if (ps.executeUpdate()<=0) {
            System.out.println(this.getClass().getCanonicalName() 
                + " [WARN] Repository service configuration doesn't stored ok. No rows was affected in JDBC operation. Datasource " + sourceName);
          }
        } else
          throw new JDBCRepositoryConfigurationNotInitializedException("Configuration table can not be created in database. Source name " + sourceName);          
        
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

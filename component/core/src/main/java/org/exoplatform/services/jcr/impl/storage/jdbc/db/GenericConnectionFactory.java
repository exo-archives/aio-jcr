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
package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.jcr.RepositoryException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 *
 * 15.03.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: GenericConnectionFactory.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class GenericConnectionFactory implements WorkspaceStorageConnectionFactory {

  protected final Log log = ExoLogger.getLogger("jcr.GenericConnectionFactory");
  
  protected final DataSource dbDataSource;
  protected final String dbDriver;
  protected final String dbUrl;
  protected final String dbUserName;
  protected final String dbPassword;
  
  protected final String containerName;
  protected final boolean multiDb;
  protected final ValueStoragePluginProvider valueStorageProvider;
  protected final int maxBufferSize;
  protected final File swapDirectory;
  protected final FileCleaner swapCleaner;
  
  protected GenericConnectionFactory( 
      DataSource dataSource,
      String dbDriver,
      String dbUrl, 
      String dbUserName, 
      String dbPassword,
      String containerName, 
      boolean multiDb, 
      ValueStoragePluginProvider valueStorageProvider, 
      int maxBufferSize, 
      File swapDirectory, 
      FileCleaner swapCleaner) {
    
    this.containerName = containerName;
    this.multiDb = multiDb;
    this.valueStorageProvider = valueStorageProvider;
    this.maxBufferSize = maxBufferSize;
    this.swapDirectory = swapDirectory;
    this.swapCleaner = swapCleaner;

    this.dbDataSource = dataSource;
    this.dbDriver = dbDriver;
    this.dbUrl = dbUrl;
    this.dbUserName = dbUserName;
    this.dbPassword = dbPassword;
  }
  
  public GenericConnectionFactory(DataSource dataSource, 
      String containerName, 
      boolean multiDb, 
      ValueStoragePluginProvider valueStorageProvider, 
      int maxBufferSize, 
      File swapDirectory, 
      FileCleaner swapCleaner) {
    
    this(dataSource, null, null, null, null, containerName, multiDb, valueStorageProvider,
        maxBufferSize, swapDirectory, swapCleaner);
  }
  
  public GenericConnectionFactory(
      String dbDriver,
      String dbUrl, 
      String dbUserName, 
      String dbPassword,
      String containerName, 
      boolean multiDb, 
      ValueStoragePluginProvider valueStorageProvider, 
      int maxBufferSize, 
      File swapDirectory, 
      FileCleaner swapCleaner) throws RepositoryException {

    this(null, dbDriver, dbUrl, dbUserName, dbPassword, containerName, multiDb, valueStorageProvider,
        maxBufferSize, swapDirectory, swapCleaner);
    
    try {
      Class.forName(dbDriver).newInstance();
    } catch (InstantiationException e) {
      throw new RepositoryException(e); 
    } catch (IllegalAccessException e) {
      throw new RepositoryException(e);
    } catch (ClassNotFoundException e) {
      throw new RepositoryException(e);
    }
  }
  
  public WorkspaceStorageConnection openConnection() throws RepositoryException {
    
    try {

      if (multiDb) {
        return new MultiDbJDBCConnection(
            getJdbcConnection(), 
            containerName, 
            valueStorageProvider, 
            maxBufferSize, 
            swapDirectory, 
            swapCleaner);
      }
      
      return new SingleDbJDBCConnection(
          getJdbcConnection(), 
          containerName, 
          valueStorageProvider, 
          maxBufferSize, 
          swapDirectory, 
          swapCleaner);

    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }
  
  public Connection getJdbcConnection() throws RepositoryException {
    try {
      return dbDataSource != null ? dbDataSource.getConnection() : 
        (dbUserName != null ? 
            DriverManager.getConnection(dbUrl, dbUserName, dbPassword) : 
              DriverManager.getConnection(dbUrl));
    } catch (SQLException e) {
        String err = "Error of JDBC connection open. SQLException: " + e.getMessage() 
          + ", SQLState: " + e.getSQLState()
          + ", VendorError: " + e.getErrorCode();
        throw new RepositoryException(err, e);
    }
  }
  
}

/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.File;
import java.sql.SQLException;

import javax.jcr.RepositoryException;
import javax.sql.DataSource;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

/**
 * Created by The eXo Platform SARL
 *
 * 16.03.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: MySQLConnectionFactory.java 13869 2007-03-28 13:50:50Z peterit $
 */
public class MySQLConnectionFactory extends GenericConnectionFactory {

  public MySQLConnectionFactory (
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
    
    super(dbDriver, dbUrl, dbUserName, dbPassword, containerName, multiDb, valueStorageProvider, maxBufferSize, swapDirectory, swapCleaner);
  }
  
  public MySQLConnectionFactory (
      DataSource dbDataSource, 
      String containerName, 
      boolean multiDb, 
      ValueStoragePluginProvider valueStorageProvider, 
      int maxBufferSize, 
      File swapDirectory, 
      FileCleaner swapCleaner) {
    
    super(dbDataSource, containerName, multiDb, valueStorageProvider, maxBufferSize, swapDirectory, swapCleaner);    
  }

  @Override
  public WorkspaceStorageConnection openConnection() throws RepositoryException {
    try {

      if (multiDb) {
        return new MySQLMultiDbJDBCConnection(
            getJdbcConnection(), 
            containerName, 
            valueStorageProvider, 
            maxBufferSize, 
            swapDirectory, 
            swapCleaner);
      }
      
      return new MySQLSingleDbJDBCConnection(
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

  
  
}

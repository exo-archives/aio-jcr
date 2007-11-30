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
import java.sql.SQLException;

import javax.jcr.RepositoryException;
import javax.sql.DataSource;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

/**
 * Created by The eXo Platform SAS
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

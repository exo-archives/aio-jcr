/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.storage.jdbc.optimisation.db;

import org.exoplatform.services.jcr.impl.storage.jdbc.db.GenericConnectionFactory;
import org.exoplatform.services.jcr.impl.storage.jdbc.monitor.ManagedConnection;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.jcr.RepositoryException;
import javax.sql.DataSource;

/**
 * Created by The eXo Platform SAS
 * 
 * 15.03.2007
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class GenericCQConnectionFactory extends GenericConnectionFactory {

  /**
   * GenericConnectionFactory constructor.
   * 
   * @param dataSource
   *          - DataSource
   * @param dbDriver
   *          - JDBC Driver
   * @param dbUrl
   *          - JDBC URL
   * @param dbUserName
   *          - database username
   * @param dbPassword
   *          - database user password
   * @param containerName
   *          - Container name (see configuration)
   * @param multiDb
   *          - multidatabase state flag
   * @param valueStorageProvider
   *          - external Value Storages provider
   * @param maxBufferSize
   *          - Maximum buffer size (see configuration)
   * @param swapDirectory
   *          - Swap directory (see configuration)
   * @param swapCleaner
   *          - Swap cleaner (internal FileCleaner).
   */
  protected GenericCQConnectionFactory(DataSource dataSource,
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
    super(dataSource,
          dbDriver,
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

  /**
   * GenericConnectionFactory constructor.
   * 
   * @param dataSource
   *          - DataSource
   * @param containerName
   *          - Container name (see configuration)
   * @param multiDb
   *          - multidatabase state flag
   * @param valueStorageProvider
   *          - external Value Storages provider
   * @param maxBufferSize
   *          - Maximum buffer size (see configuration)
   * @param swapDirectory
   *          - Swap directory (see configuration)
   * @param swapCleaner
   *          - Swap cleaner (internal FileCleaner).
   */
  public GenericCQConnectionFactory(DataSource dataSource,
                                    String containerName,
                                    boolean multiDb,
                                    ValueStoragePluginProvider valueStorageProvider,
                                    int maxBufferSize,
                                    File swapDirectory,
                                    FileCleaner swapCleaner) {

    this(dataSource,
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
  }

  /**
   * GenericConnectionFactory constructor.
   * 
   * @param dbDriver
   *          - JDBC Driver
   * @param dbUrl
   *          - JDBC URL
   * @param dbUserName
   *          - database username
   * @param dbPassword
   *          - database user password
   * @param containerName
   *          - Container name (see configuration)
   * @param multiDb
   *          - multidatabase state flag
   * @param valueStorageProvider
   *          - external Value Storages provider
   * @param maxBufferSize
   *          - Maximum buffer size (see configuration)
   * @param swapDirectory
   *          - Swap directory (see configuration)
   * @param swapCleaner
   *          - Swap cleaner (internal FileCleaner).
   */
  public GenericCQConnectionFactory(String dbDriver,
                                    String dbUrl,
                                    String dbUserName,
                                    String dbPassword,
                                    String containerName,
                                    boolean multiDb,
                                    ValueStoragePluginProvider valueStorageProvider,
                                    int maxBufferSize,
                                    File swapDirectory,
                                    FileCleaner swapCleaner) throws RepositoryException {

    this(null,
         dbDriver,
         dbUrl,
         dbUserName,
         dbPassword,
         containerName,
         multiDb,
         valueStorageProvider,
         maxBufferSize,
         swapDirectory,
         swapCleaner);

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

  /**
   * {@inheritDoc}
   */
  public WorkspaceStorageConnection openConnection() throws RepositoryException {
    return openConnection(false);
  }

  /**
   * {@inheritDoc}
   */
  public WorkspaceStorageConnection openConnection(boolean readOnly) throws RepositoryException {
    try {

      if (multiDb) {
        return new MultiDbJDBCConnection(getJdbcConnection(readOnly),
                                         readOnly,
                                         containerName,
                                         valueStorageProvider,
                                         maxBufferSize,
                                         swapDirectory,
                                         swapCleaner);
      }

      return new SingleDbJDBCConnection(getJdbcConnection(readOnly),
                                        readOnly,
                                        containerName,
                                        valueStorageProvider,
                                        maxBufferSize,
                                        swapDirectory,
                                        swapCleaner);

    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Connection getJdbcConnection(boolean readOnly) throws RepositoryException {
    try {
      final Connection conn = dbDataSource != null
          ? dbDataSource.getConnection()
          : (dbUserName != null
              ? DriverManager.getConnection(dbUrl, dbUserName, dbPassword)
              : DriverManager.getConnection(dbUrl));

      if (readOnly) {
        // set this feature only if it asked
        conn.setReadOnly(readOnly);
      }

      return monitorInterest == 0 ? conn : new ManagedConnection(conn, monitorInterest);
    } catch (SQLException e) {
      String err = "Error of JDBC connection open. SQLException: " + e.getMessage()
          + ", SQLState: " + e.getSQLState() + ", VendorError: " + e.getErrorCode();
      throw new RepositoryException(err, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Connection getJdbcConnection() throws RepositoryException {
    return getJdbcConnection(false);
  }

}

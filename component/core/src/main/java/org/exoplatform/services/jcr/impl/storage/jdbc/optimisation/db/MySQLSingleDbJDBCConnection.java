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

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by The eXo Platform SAS
 * 
 * 20.03.2007
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MySQLSingleDbJDBCConnection extends SingleDbJDBCConnection {

  /**
   * MySQL Singledatabase JDBC Connection constructor.
   * 
   * @param dbConnection
   *          JDBC connection, shoudl be opened before
   * @param readOnly
   *          boolean if true the dbConnection was marked as READ-ONLY.
   * @param containerName
   *          Workspace Storage Container name (see configuration)
   * @param valueStorageProvider
   *          External Value Storages provider
   * @param maxBufferSize
   *          Maximum buffer size (see configuration)
   * @param swapDirectory
   *          Swap directory File (see configuration)
   * @param swapCleaner
   *          Swap cleaner (internal FileCleaner).
   * @throws SQLException
   * 
   * @see org.exoplatform.services.jcr.impl.util.io.FileCleaner
   */
  public MySQLSingleDbJDBCConnection(Connection dbConnection,
                                     boolean readOnly,
                                     String containerName,
                                     ValueStoragePluginProvider valueStorageProvider,
                                     int maxBufferSize,
                                     File swapDirectory,
                                     FileCleaner swapCleaner) throws SQLException {

    super(dbConnection,
          readOnly,
          containerName,
          valueStorageProvider,
          maxBufferSize,
          swapDirectory,
          swapCleaner);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int addNodeRecord(NodeData data) throws SQLException {
    // check if parent exists
    if (data.getParentIdentifier() != null) {
      ResultSet item = findItemByIdentifier(getInternalId(data.getParentIdentifier()));
      try {
        if (!item.next())
          throw new SQLException("Parent is not found. Behaviour of " + JCR_FK_ITEM_PARENT);
      } finally {
        item.close();
      }
    }
    return super.addNodeRecord(data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int addPropertyRecord(PropertyData data) throws SQLException {
    // check if parent exists
    if (data.getParentIdentifier() != null) {
      ResultSet item = findItemByIdentifier(getInternalId(data.getParentIdentifier()));
      try {
        if (!item.next())
          throw new SQLException("Parent is not found. Behaviour of " + JCR_FK_ITEM_PARENT);
      } finally {
        item.close();
      }
    }
    return super.addPropertyRecord(data);
  }

}

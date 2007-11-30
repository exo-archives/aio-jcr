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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

/**
 * Created by The eXo Platform SAS
 *
 * 20.03.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: MySQLMultiDbJDBCConnection.java 13869 2007-03-28 13:50:50Z peterit $
 */
public class MySQLSingleDbJDBCConnection extends SingleDbJDBCConnection {

  public MySQLSingleDbJDBCConnection(Connection dbConnection,
      String containerName, ValueStoragePluginProvider valueStorageProvider,
      int maxBufferSize, File swapDirectory, FileCleaner swapCleaner) throws SQLException {
  
    super(dbConnection, containerName, valueStorageProvider, 
        maxBufferSize, swapDirectory, swapCleaner);
  }

  @Override
  protected void addNodeRecord(NodeData data) throws SQLException {
    // check if parent exists
    if (data.getParentIdentifier() != null) {
      ResultSet item = findItemByIdentifier(getInternalId(data.getParentIdentifier()));
      try {
        if(!item.next())
          throw new SQLException("Parent is not found. Behaviour of " + JCR_FK_ITEM_PARENT);
      } finally {
        item.close();
      }
    }
    super.addNodeRecord(data);
  }

  @Override
  protected void addPropertyRecord(PropertyData data) throws SQLException {
    // check if parent exists
    if (data.getParentIdentifier() != null) {
      ResultSet item = findItemByIdentifier(getInternalId(data.getParentIdentifier()));
      try {
        if(!item.next())
          throw new SQLException("Parent is not found. Behaviour of " + JCR_FK_ITEM_PARENT);
      } finally {
        item.close();
      }
    }
    super.addPropertyRecord(data);
  }

  
}

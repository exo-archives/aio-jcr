/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
 * Created by The eXo Platform SARL
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
      ResultSet item = findItemByIdentifier(getIdentifier(data.getParentIdentifier()));
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
      ResultSet item = findItemByIdentifier(getIdentifier(data.getParentIdentifier()));
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

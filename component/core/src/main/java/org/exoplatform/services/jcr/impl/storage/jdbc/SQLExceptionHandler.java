/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.jdbc;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SARL        .
 * @author Peter Nedonosko
 * @version $Id: SQLExceptionHandler.java 13869 2007-03-28 13:50:50Z peterit $
 */

public class SQLExceptionHandler {
  
  private final String containerName;
  private final JDBCStorageConnection conn;
  
  // ---------------- SQLException handler -------------------
  // TODO avoid DB calls!
  
  SQLExceptionHandler(String containerName, JDBCStorageConnection conn) {
    this.containerName = containerName;
    this.conn = conn;
  }

  protected String handleAddException(Exception e, ItemData item) throws RepositoryException, InvalidItemStateException {
    String message = "["+containerName+"] ";
    String errMessage = e.getMessage();
    String itemInfo = (item.isNode() ? "NODE " : "PROPERTY ")
      + item.getQPath().getAsString() + " " + item.getIdentifier()
      + (errMessage != null ? ". Cause: " + errMessage : "");

    if (errMessage != null) {
      // try detect error by foreign key names
      String umsg = errMessage.toLowerCase().toUpperCase();
      if (umsg.indexOf(conn.JCR_FK_ITEM_PARENT)>=0) {
        // we see that error contains JCR_FK_XITEM_PARENT constraint name, so it's constraint violation...
        message += "Parent not found for " + itemInfo;
        throw new InvalidItemStateException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_NODE_ITEM)>=0) {
        // Foreign key NODE->ITEM tables vioaltion. A item record not found for node created.
        message += "Item relation in JCR_ITEM table not found (not created). " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_PROPERTY_NODE)>=0) {
        // Foreign key PROPERTY->NODE tables vioaltion
        message += "Parent not found (not created) in JCR_NODE table. " + itemInfo;
        throw new InvalidItemStateException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_PROPERTY_ITEM)>=0) {
        // Foreign key PROPERTY->ITEM tables vioaltion
        message += "Item relation in JCR_ITEM table not found (not created). " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_VALUE_PROPERTY)>=0) {
        // Foreign key VALUE->PROPERTY tables vioaltion
        message += "Property not found (not created) in JCR_PROPERTY table for values created. " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (umsg.indexOf(conn.JCR_PK_ITEM)>=0) {
        // primary key ITEM tables vioaltion
        message += "An item with given ID already exists in JCR_ITEM table. " + itemInfo;
        throw new RepositoryException(message, e);
      }
    }
    
    // try detect integrity violation
    RepositoryException ownException = null;
    try {
      NodeData parent = (NodeData) conn.getItemData(item.getParentIdentifier());
      if (parent != null) {
        // have a parent
        try {
          ItemData me = conn.getItemData(item.getIdentifier());
          if (me != null) {
            // item already exists
            message += "Item already exists in storage: " + itemInfo;
            ownException = new ItemExistsException(message, e);
            throw ownException;
          }
          
          me = conn.getItemData(parent, new QPathEntry(item.getQPath().getName(), item.getQPath()
              .getIndex()));
          if (me != null) {
            message += "Item already exists in storage: " + itemInfo;
            ownException = new ItemExistsException(message, e);
            throw ownException;
          }
              
          
          
        } catch(Exception ep) {
          // item not found or other things but error of item reading
          if (ownException != null) throw ownException;
        }
        message += "Error of item add. " + itemInfo;
        ownException = new RepositoryException(message, e);
        throw ownException;
      }
    } catch(Exception ep) {
      // no parent or error access it
      if (ownException != null) throw ownException;
    }
    message += "Error of item add. " + itemInfo;
    throw new InvalidItemStateException(message, e);
  }

  protected String handleDeleteException(Exception e, ItemData item) throws RepositoryException, InvalidItemStateException {
    String message = "["+containerName+"] ";
    String errMessage = e.getMessage();
    String itemInfo = (item.isNode() ? "NODE " : "PROPERTY ")
      + item.getQPath().getAsString() + " " + item.getIdentifier()
      + (errMessage != null ? ". Cause: " + errMessage : "");

    if (errMessage != null) {
      // try detect error by foreign key names
      String umsg = errMessage.toLowerCase().toUpperCase();
      if (umsg.indexOf(conn.JCR_FK_ITEM_PARENT)>=0) {
        // we see that error contains JCR_FK_MNODEPARENT constraint name, so it's constraint violation...
        message += "Can't delete parent till child exists " + itemInfo;
        throw new InvalidItemStateException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_NODE_ITEM)>=0) {
        // Foreign key NODE->ITEM tables vioaltion.
        message += "Can't delete item till related node exists in JCR_NODE table " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_PROPERTY_NODE)>=0) {
        // Foreign key PROPERTY->NODE tables vioaltion
        message += "Can't delete node till child property(ies) exists in JCR_PROPERTY table " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_PROPERTY_ITEM)>=0) {
        // Foreign key PROPERTY->ITEM tables vioaltion
        message += "Can't delete item till related property exists in JCR_PROPERTY table " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_VALUE_PROPERTY)>=0) {
        // Foreign key VALUE->PROPERTY tables vioaltion
        message += "Can't delete property till related property values exists in JCR_VALUE table " + itemInfo;
        throw new RepositoryException(message, e);
      }
    }
    
    message += "Error of item delete " + itemInfo;
    throw new RepositoryException(message, e);
  }

  protected String handleUpdateException(Exception e, ItemData item) throws RepositoryException, InvalidItemStateException {
    String message = "["+containerName+"] ";
    String errMessage = e.getMessage();
    String itemInfo = (item.isNode() ? "NODE " : "PROPERTY ")
      + item.getQPath().getAsString() + " " + item.getIdentifier()
      + (errMessage != null ? ". Cause: " + errMessage : "");

    if (errMessage != null)
      // try detect error by foreign key names
      if (errMessage.toLowerCase().toUpperCase().indexOf(conn.JCR_FK_VALUE_PROPERTY)>=0) {
        // Foreign key VALUE->PROPERTY tables vioaltion
        message += "Property not found (not created) in JCR_PROPERTY table for values updated. " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (errMessage.toLowerCase().toUpperCase().indexOf(conn.JCR_PK_ITEM)>=0) {
        // primary key ITEM tables vioaltion
        message += "An item with given ID already exists in JCR_ITEM table. " + itemInfo;
        throw new RepositoryException(message, e);  
      }

    // try detect integrity violation
    RepositoryException ownException = null;
    try {
      ItemData me = conn.getItemData(item.getIdentifier());
      if (me != null) {
        // item already exists
        message += "Item exists but update error " + itemInfo;
        ownException = new RepositoryException(message, e);
        throw ownException;
      }
    } catch(Exception ep) {
      // item not found or other things but error of item reading
      if (ownException != null) throw ownException;
    }
    message += "Error of item update. " + itemInfo;
    throw new InvalidItemStateException(message, e);
  }

}

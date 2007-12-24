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
package org.exoplatform.services.jcr.impl.storage.jdbc;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.storage.JCRInvalidItemStateException;

/**
 * Created by The eXo Platform SAS.
 * 
 * The eXo JCR database has constraints can be violated.
 * 
 * JCR_PK_XCONTAINER - Can not be exisits two containers with same version
 * 
 * JCR_PK_XITEM - Item already exists with this ID
 * JCR_FK_XITEM_PARENT - Parent not found by ID
 * JCR_IDX_XITEM_PARENT - Item already exists with the parent, name, index, type(N/P), persisted version
 * JCR_IDX_XITEM_PARENT_NAME - Item already exists with the type(N/P), parent, name, index, persisted version
 * JCR_IDX_XITEM_PARENT_ID - Item already exists with the type(N/P), parent, ID, persisted version
 * 
 * JCR_PK_XVALUE - Value already exists with the ID (impossible, ID is autiincremented)
 * JCR_FK_XVALUE_PROPERTY - There is no property exists for the value.
 * JCR_IDX_XVALUE_PROPERTY - Value already exists with the property and order number
 * 
 * JCR_PK_XREF - Reference already exists to the node from property with order number
 * JCR_IDX_XREF_PROPERTY - Reference already exists with the property and order number
 * 
 * @author Peter Nedonosko
 * @version $Id: SQLExceptionHandler.java 13869 2007-03-28 13:50:50Z peterit $
 */

public class SQLExceptionHandler {
  
  private final String containerName;
  private final JDBCStorageConnection conn;
  
  // ---------------- SQLException handler -------------------
  
  SQLExceptionHandler(String containerName, JDBCStorageConnection conn) {
    this.containerName = containerName;
    this.conn = conn;
  }

  protected String handleAddException(Exception e, ItemData item) throws RepositoryException, InvalidItemStateException {
    String message = "["+containerName+"] ADD " + (item.isNode() ? "NODE. " : "PROPERTY. ");
    String errMessage = e.getMessage();
    String itemInfo = item.getQPath().getAsString() + ", ID: " 
      + item.getIdentifier() + ", ParentID: " + item.getParentIdentifier() 
      + (errMessage != null ? ". Cause >>>> " + errMessage : "");

    if (errMessage != null) {
      // try detect error by foreign key names
      String umsg = errMessage.toLowerCase().toUpperCase();
      if (umsg.indexOf(conn.JCR_FK_ITEM_PARENT)>=0) {
        message += "Parent not found. Item " + itemInfo;
        throw new InvalidItemStateException(message, e);
      } else if (umsg.indexOf(conn.JCR_PK_ITEM)>=0) {
        message += "Item is already exists. Condition: ID. " + itemInfo;
        // InvalidItemStateException ! - because it's impossible add new item with existed UUID
        throw new InvalidItemStateException(message, e);
      } else if (umsg.indexOf(conn.JCR_IDX_ITEM_PARENT)>=0 || umsg.indexOf(conn.JCR_IDX_ITEM_PARENT_NAME)>=0) {
        message += "Item already exists. Condition: parent ID, name, index. " + itemInfo;
        throw new ItemExistsException(message, e);
      } else if (umsg.indexOf(conn.JCR_IDX_ITEM_PARENT_ID)>=0) {
        message += "Item already exists. Condition: parent ID and ID. " + itemInfo;
        throw new ItemExistsException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_VALUE_PROPERTY)>=0) {
        message += "Property is not exist but the value is being created. Condition: property ID. " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (umsg.indexOf(conn.JCR_IDX_VALUE_PROPERTY)>=0) {
        message += "Property already exists. Condition: property ID, order number. " + itemInfo;
        throw new RepositoryException(message, e);        
      } else if (umsg.indexOf(conn.JCR_PK_VALUE)>=0) {
        message += "[FATAL] Value already exists with the ValueID. Impossible state, check is ValueID is autoincremented. " + itemInfo;
        throw new RepositoryException(message, e);        
      } else if (umsg.indexOf(conn.JCR_PK_REF)>=0) {
        message += "Reference chain already exists. Condition: node ID, property ID, order number. " + itemInfo;
        throw new RepositoryException(message, e);        
      } else if (umsg.indexOf(conn.JCR_IDX_REF_PROPERTY)>=0) {
        message += "Referenceable property value already exists. Condition: property ID, order number. " + itemInfo;
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
    String message = "["+containerName+"] DELETE " + (item.isNode() ? "NODE. " : "PROPERTY. ");
    String errMessage = e.getMessage();
    String itemInfo = item.getQPath().getAsString() + " " + item.getIdentifier()
      + (errMessage != null ? ". Cause >>>> " + errMessage : "");

    if (errMessage != null) {
      // try detect error by foreign key names
      String umsg = errMessage.toLowerCase().toUpperCase();
      if (umsg.indexOf(conn.JCR_FK_ITEM_PARENT)>=0) {
        message += "Can not delete parent till childs exists. Item " + itemInfo;
        throw new InvalidItemStateException(message, e);
      } else if (umsg.indexOf(conn.JCR_FK_VALUE_PROPERTY)>=0) {
        message += "[FATAL] Can not delete property item till it contains values. Condition: property ID. " + itemInfo;
        throw new RepositoryException(message, e);
      }
    }
      
    message += "Error of item delete " + itemInfo;
    throw new RepositoryException(message, e);
  }

  protected String handleUpdateException(Exception e, ItemData item) throws RepositoryException, InvalidItemStateException {
    String message = "["+containerName+"] EDIT " + (item.isNode() ? "NODE. " : "PROPERTY. ");
    String errMessage = e.getMessage();
    String itemInfo = item.getQPath().getAsString() + " " + item.getIdentifier()
      + (errMessage != null ? ". Cause >>>> " + errMessage : "");

    if (errMessage != null)
      // try detect error by foreign key names
      if (errMessage.toLowerCase().toUpperCase().indexOf(conn.JCR_FK_VALUE_PROPERTY)>=0) {
        message += "Property is not exists but the value is being created. Condition: property ID. " + itemInfo;
        throw new RepositoryException(message, e);
      } else if (errMessage.toLowerCase().toUpperCase().indexOf(conn.JCR_PK_ITEM)>=0) {
        message += "Item already exists. Condition: ID. " + itemInfo;
        throw new JCRInvalidItemStateException(message, item.getIdentifier(), ItemState.ADDED, e);  
      }

    // try detect integrity violation
    RepositoryException ownException = null;
    try {
      ItemData me = conn.getItemData(item.getIdentifier());
      if (me != null) {
        // item already exists
        message += "Item already exists. But update errors. " + itemInfo;
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

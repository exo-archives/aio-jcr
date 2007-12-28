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
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 *
 * 15.12.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemDataRemoveVisitor extends ItemDataTraversingVisitor {

  private static Log log = ExoLogger.getLogger("jcr.ItemDataRemoveVisitor");
  
  protected List<ItemState> itemRemovedStates = new ArrayList<ItemState>();
  
  protected List<ItemState> reversedItemRemovedStates = null;
  
  protected final SessionImpl session;
  
  protected final boolean validate;
  
  protected NodeData removedRoot = null;
  
  protected QPath ancestorToSave = null;
  
  // a deletion without any validation
  public ItemDataRemoveVisitor(ItemDataConsumer dataManager) {
    this(dataManager, null);
  }
  
  public ItemDataRemoveVisitor(ItemDataConsumer dataManager, QPath ancestorToSave) {
    super(dataManager);
    this.session = null;
    this.validate = false;
    this.ancestorToSave = ancestorToSave;
  }
  
  public ItemDataRemoveVisitor(SessionImpl session, boolean validate) {
    super(session.getTransientNodesManager());
    this.session = session;
    this.validate = validate;
  }
   
  protected void validate(PropertyData property) throws RepositoryException {
    // 1. check AccessDeniedException
    validateAccessDenied(property);
    
    // 2. check ConstraintViolationException - PropertyDefinition for mandatory/protected flags
    validateConstraints(property);
    
    // 3. check VersionException
    validateVersion(property);
    
    // 4. check LockException
    validateLock(property);
  }

  protected void validateAccessDenied(PropertyData property) throws RepositoryException {
    NodeData parent = (NodeData) dataManager.getItemData(property.getParentIdentifier());
    
    if (!session.getAccessManager().hasPermission(
        parent.getACL(), PermissionType.READ, session.getUserID())) {
      throw new AccessDeniedException("Access denied "
          + session.getLocationFactory().createJCRPath(property.getQPath()).getAsString(false)
          + " for " + session.getUserID() + " (get item parent by id)");
    }
  }
  
  protected void validateConstraints(PropertyData property) throws RepositoryException {
  }
  
  protected void validateVersion(PropertyData property) throws RepositoryException {
  }
  
  protected void validateLock(PropertyData property) throws RepositoryException {
  }
  
  protected void validate(NodeData node) throws RepositoryException {
    // 1. check AccessDeniedException
    validateAccessDenied(node);
    
    // 2. check ReferentialIntegrityException - REFERENCE property target
    if (session.getWorkspace().getNodeTypeManager().isNodeType(Constants.MIX_REFERENCEABLE, node.getPrimaryTypeName(), node.getMixinTypeNames()))
      validateReferential(node);
        
    // 3. check ConstraintViolationException - NodeDefinition for mandatory/protected flags
    validateConstraints(node);
    
    // 4. check VersionException
    validateVersion(node);
    
    // 5. check LockException
    validateLock(node);
  }
  
  protected void validateAccessDenied(NodeData node) throws RepositoryException {
    if (!session.getAccessManager().hasPermission(
        node.getACL(), PermissionType.READ, session.getUserID())) {
      throw new AccessDeniedException("Access denied "
          + session.getLocationFactory().createJCRPath(node.getQPath()).getAsString(false)
          + " for " + session.getUserID() + " (get item by id)");
    }
  }
  
  protected void validateReferential(NodeData node) throws RepositoryException {
    
    List<PropertyData> refs = dataManager.getReferencesData(node.getIdentifier(), true);
    
    // A ReferentialIntegrityException will be thrown on save if this item or an item in its subtree 
    // is currently the target of a REFERENCE property located in this workspace but outside 
    // this item's subtree and the current Session has read access to that REFERENCE property.
    
    // An AccessDeniedException will be thrown on save if this item or an item in its subtree 
    // is currently the target of a REFERENCE property located in this workspace but outside 
    // this item's subtree and the current Session does not have read access to that REFERENCE property.
    
    for (PropertyData rpd: refs) {
      if (isRemoveDescendant(removedRoot)) {
        // on the tree(s), we have to remove REFERENCE property before the node
        entering(rpd, currentLevel);
      } else {
        NodeData refParent = (NodeData) dataManager.getItemData(rpd.getParentIdentifier());
        if (!session.getAccessManager().hasPermission(
            refParent.getACL(), PermissionType.READ, session.getUserID())) {
          throw new AccessDeniedException("Access denied " 
              + session.getLocationFactory().createJCRPath(rpd.getQPath()).getAsString(false)
              + " for " + session.getUserID() + " (get reference property parent by id)");
        } 

        throw new ReferentialIntegrityException("This node "
            + session.getLocationFactory().createJCRPath(node.getQPath()).getAsString(false)
            + " is currently the target of a REFERENCE property " 
            + session.getLocationFactory().createJCRPath(rpd.getQPath()).getAsString(false)
            + " located in this workspace. Session id: " + session.getUserID());        
      }
    }
  }
  
  protected boolean isRemoveDescendant(ItemData item) throws RepositoryException {
    return item.getQPath().isDescendantOf(removedRoot.getQPath(), false);
  }
  
  protected void validateConstraints(NodeData node) throws RepositoryException {
  }
  
  protected void validateVersion(NodeData node) throws RepositoryException {
  }
  
  protected void validateLock(NodeData node) throws RepositoryException {
  }  
  
  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    if (log.isDebugEnabled()) 
      log.debug("Entering property " + property.getQPath().getAsString());
    
    if (validate) {
      validate(property);
    }
    
    ItemState state = new ItemState(property, ItemState.DELETED, true, 
        ancestorToSave != null ? ancestorToSave : removedRoot.getQPath()); 
    
    if (!itemRemovedStates.contains(state))
      itemRemovedStates.add(state);
    else if (log.isDebugEnabled())
      // REFERENCE props usecase, see validateReferential(NodeData) 
      log.debug("A property " + property.getQPath().getAsString() + " is already listed for remove");
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    if (log.isDebugEnabled()) 
      log.debug("Entering node " + node.getQPath().getAsString());
    
    // this node is not taken in account
    if (level == 0) {
      removedRoot = node;
    }
    
    if (validate) {
      validate(node);
    }
    
    ItemState state = new ItemState(node, ItemState.DELETED, true, 
        ancestorToSave != null ? ancestorToSave : removedRoot.getQPath());
    itemRemovedStates.add(state);
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
  }

  public List<ItemState> getRemovedStates() {
    if (reversedItemRemovedStates == null) {
      Collections.reverse(itemRemovedStates);
      reversedItemRemovedStates = itemRemovedStates;
    }
    return reversedItemRemovedStates;
  }
}

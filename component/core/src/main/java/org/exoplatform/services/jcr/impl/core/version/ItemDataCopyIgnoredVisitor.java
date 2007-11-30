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

package org.exoplatform.services.jcr.impl.core.version;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.DefaultItemDataCopyVisitor;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 *
 * 14.12.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemDataCopyIgnoredVisitor extends DefaultItemDataCopyVisitor {

  private static Log log = ExoLogger.getLogger("jcr.ItemDataCopyIgnoredVisitor");
  
  protected final SessionChangesLog restoredChanges;
  
  public ItemDataCopyIgnoredVisitor(NodeData context, InternalQName destNodeName, 
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager dataManager, SessionChangesLog changes) {
    super(context, destNodeName, nodeTypeManager, dataManager, true); 
    this.restoredChanges = changes;
  }
  

  protected void entering(PropertyData property, int level) throws RepositoryException {
    
    if (level == 1 && (property.getQPath().getName().equals(Constants.JCR_BASEVERSION) ||
        property.getQPath().getName().equals(Constants.JCR_ISCHECKEDOUT)))
      // skip versionable specific props 
      return;
    
    if (curParent() == null) {
      NodeData existedParent = (NodeData) dataManager.getItemData(property.getParentIdentifier());
      
      PropertyDefinition pdef = ntManager.findPropertyDefinition(
          property.getQPath().getName(), 
          existedParent.getPrimaryTypeName(), 
          existedParent.getMixinTypeNames());

      if (pdef.getOnParentVersion() == OnParentVersionAction.IGNORE) {
        // parent is not exists as this copy context current parent
        // i.e. it's a IGNOREd property elsewhere at a versionable node descendant. 
        // So, we have to know that this parent WILL exists after restore
        ItemState contextState = restoredChanges.getItemState(property.getParentIdentifier());
        if (contextState != null && !contextState.isDeleted()) {        
          // the node can be stored as IGNOREd in restore set, check an action
        
          if (log.isDebugEnabled())
            log.debug("A property " + property.getQPath().getAsString() + " is IGNOREd");
          
          // set context current parent to existed in restore set
          parents.push((NodeData) contextState.getData());
          super.entering(property, level);
          parents.pop();
        }
      }
    } else {
      // copy as IGNOREd parent child, i.e. OnParentVersionAction is any
      if (log.isDebugEnabled())
        log.debug("A property " + property.getQPath().getAsString() + " is IGNOREd node descendant");
      super.entering(property, level);
    }
  }
  
  protected void entering(NodeData node, int level) throws RepositoryException {
    
    if (level == 0) {
      parents.pop(); // remove context parent (redo superclass constructor work) 
    } else if (level > 0) {
      if (curParent() == null) {
        NodeData existedParent = (NodeData) dataManager.getItemData(node.getParentIdentifier());
        NodeDefinition ndef = ntManager.findNodeDefinition(node.getQPath().getName(), 
            existedParent.getPrimaryTypeName(), existedParent.getMixinTypeNames());
        
        // the node can be stored as IGNOREd in restore set, check an action
        if (ndef.getOnParentVersion() == OnParentVersionAction.IGNORE) {
          // parent is not exists as this copy context current parent
          // i.e. it's a IGNOREd node elsewhere at a versionable node descendant. 
          // So, we have to know that this parent WILL exists after restore
          ItemState contextState = restoredChanges.getItemState(node.getParentIdentifier());
          if (contextState != null && !contextState.isDeleted()) {
            if (log.isDebugEnabled())
              log.debug("A node " + node.getQPath().getAsString() + " is IGNOREd");
            
            // set context current parent to existed in restore set
            parents.push((NodeData) contextState.getData());
            super.entering(node, level);
            NodeData thisNode = parents.pop(); // copied
            parents.pop(); // contextParent
            parents.push(thisNode);
            return;
          }
        }
      } else {
        // copy as IGNOREd parent child, i.e. OnParentVersionAction is any
        if (log.isDebugEnabled())
          log.debug("A node " + node.getQPath().getAsString() + " is IGNOREd node descendant");
        super.entering(node, level);
        return;
      }
    }
    parents.push(null); // skip this node as we hasn't parent in restore result
  }
  
  protected void leaving(NodeData node, int level) throws RepositoryException {
    if (parents.size()>0) 
      parents.pop();
  }
}
/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.version;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataCopyVisitor;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 *
 * 14.12.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ItemDataCopyIgnoredVisitor.java 13421 2007-03-15 10:46:47Z geaz $
 */
public class ItemDataCopyIgnoredVisitor extends ItemDataCopyVisitor {

  private static Log log = ExoLogger.getLogger("jcr.ItemDataCopyIgnoredVisitor");
  
  protected final SessionChangesLog restoredChanges;
  
  public ItemDataCopyIgnoredVisitor(NodeData context, InternalQName destNodeName, 
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager dataManager, SessionChangesLog changes) {
    super(context, destNodeName, nodeTypeManager, dataManager, true); //keepUUIDs = true
    this.restoredChanges = changes;
  }
  

  protected void entering(PropertyData property, int level) throws RepositoryException {
    
    if (level == 1 && (property.getQPath().getName().equals(Constants.JCR_BASEVERSION) ||
        property.getQPath().getName().equals(Constants.JCR_ISCHECKEDOUT)))
      // skip versionable specific props 
      return;
    
    if (curParent() == null) {
      NodeData existedParent = (NodeData) dataManager.getItemData(property.getParentUUID());
      
      PropertyDefinition pdef = ntManager.findPropertyDefinition(
          property.getQPath().getName(), 
          existedParent.getPrimaryTypeName(), 
          existedParent.getMixinTypeNames());

      if (pdef.getOnParentVersion() == OnParentVersionAction.IGNORE) {
        // parent is not exists as this copy context current parent
        // i.e. it's a IGNOREd property elsewhere at a versionable node descendant. 
        // So, we have to know that this parent WILL exists after restore
        //NodeData contextParent = findRestoredParent(property);
        //if (contextParent != null) {
        ItemState contextState = restoredChanges.getItemState(property.getParentUUID());
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
        NodeData existedParent = (NodeData) dataManager.getItemData(node.getParentUUID());
        NodeDefinition ndef = ntManager.findNodeDefinition(node.getQPath().getName(), 
            existedParent.getPrimaryTypeName(), existedParent.getMixinTypeNames());
        
        // the node can be stored as IGNOREd in restore set, check an action
        if (ndef.getOnParentVersion() == OnParentVersionAction.IGNORE) {
          // parent is not exists as this copy context current parent
          // i.e. it's a IGNOREd node elsewhere at a versionable node descendant. 
          // So, we have to know that this parent WILL exists after restore
          //NodeData contextParent = findRestoredParent(node);
          ItemState contextState = restoredChanges.getItemState(node.getParentUUID());
          if (contextState != null && !contextState.isDeleted()) {
          //if (contextParent != null) {
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
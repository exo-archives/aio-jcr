/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.audit;

import org.apache.commons.chain.Context;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class RemoveAuditableAction implements Action {

  public boolean execute(Context context) throws Exception {
    ItemImpl item = (ItemImpl) context.get("currentItem");
    if (item.isNode()) {
      NodeImpl node = (NodeImpl) item;
      NodeTypeManagerImpl ntManager = node.getSession().getWorkspace().getNodeTypeManager();
      // NodeData node = (NodeData) data;
      if (ntManager.isNodeType(AuditService.EXO_AUDITABLE,
          ((NodeData) node.getData()).getPrimaryTypeName(),
          ((NodeData) node.getData()).getMixinTypeNames())) {
        AuditService auditService = (AuditService) ((ExoContainer) context.get("exocontainer"))
        .getComponentInstanceOfType(AuditService.class);
        auditService.removeHistory(node);
        return true;
      }
    }
    return false;
  }

}

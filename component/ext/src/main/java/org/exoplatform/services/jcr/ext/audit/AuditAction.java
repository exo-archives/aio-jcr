/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.audit;

import javax.jcr.Node;

import org.apache.commons.chain.Context;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.impl.core.ItemImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class AuditAction implements Action {

  public boolean execute(Context ctx) throws Exception {

    ItemImpl item = (ItemImpl) ctx.get("currentItem");
    int event = (Integer) ctx.get("event");
    
    Node node;
    if(item.isNode())
      node = (Node)item;
    else
      node = item.getParent();

    AuditService auditService = (AuditService) ((ExoContainer) ctx.get("exocontainer"))
    .getComponentInstanceOfType(AuditService.class);
    if(!auditService.hasHistory(node))
      auditService.createHistory(node);
    auditService.addRecord(item, event);
    
    return false;

//    ItemImpl item = (ItemImpl) ctx.get("currentItem");
//    
//    int event = (Integer) ctx.get("event");
//    if (item != null) {
//      //Node Add. Creating history
//      AuditService auditService = (AuditService) ((ExoContainer) ctx.get("exocontainer"))
//      .getComponentInstanceOfType(AuditService.class);
//
//      if (event == ExtendedEvent.NODE_ADDED ) {
//        // NodeImpl node = (NodeImpl) ;
//        if (item instanceof Node && ((Node) item).canAddMixin("exo:auditable")) {
//          ((Node) item).addMixin("exo:auditable");
//          auditService.createHistory(item);
//        }
//      } 
//      auditService.addRecord(item, event);
//    }
//    return false;
  }

}

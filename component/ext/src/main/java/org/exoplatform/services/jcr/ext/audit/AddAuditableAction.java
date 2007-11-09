/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.audit;

import javax.jcr.Node;

import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class AddAuditableAction implements Action {

  public boolean execute(Context ctx) throws Exception {
    
    NodeImpl item = (NodeImpl) ctx.get("currentItem");
    //Create history
    //AuditService auditService = (AuditService) ((ExoContainer) ctx.get("exocontainer"))
    //.getComponentInstanceOfType(AuditService.class);
    if (item.canAddMixin("exo:auditable")) {
      ((Node) item).addMixin("exo:auditable");
    }
    
    return false;
  }

}

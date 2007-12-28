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

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
package org.exoplatform.services.jcr.usecases.action.info;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.chain.Context;
import org.exoplatform.services.jcr.observation.ExtendedEvent;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: CheckoutActionInfo.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class CheckoutActionInfo extends ActionInfo {

  @Override
  public void execute(Context ctx) throws RepositoryException {
    // TODO Auto-generated method stub
    Node node = (Node) ctx.get("node");
    if (node.canAddMixin("mix:versionable"))
      node.addMixin("mix:versionable");
    node.getSession().save();
    if (node.isCheckedOut())
      node.checkin();
    node.checkout();
  }

  @Override
  public int getEventType() {
    // TODO Auto-generated method stub
    return ExtendedEvent.CHECKOUT;
  }

  @Override
  public void tearDown(Context ctx) throws RepositoryException {
    // TODO Auto-generated method stub
    
    Node node = (Node) ctx.get("node");
    if(node.isCheckedOut()){
      node.checkin();
    }
    super.tearDown(ctx);
  }
  
  
}

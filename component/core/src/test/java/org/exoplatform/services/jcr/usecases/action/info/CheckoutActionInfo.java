/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.action.info;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.chain.Context;
import org.exoplatform.services.jcr.observation.ExtendedEvent;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: CheckoutActionInfo.java 13421 2007-03-15 10:46:47Z geaz $
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

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
 * @version $Id: UnLockActionInfo.java 13421 2007-03-15 10:46:47Z geaz $
 */
public class UnLockActionInfo extends ActionInfo {

  @Override
  public void execute(Context ctx) throws RepositoryException {
    // TODO Auto-generated method stub
    Node node = (Node) ctx.get("node");
    if (node.canAddMixin("mix:lockable")) {
      node.addMixin("mix:lockable");
      node.getSession().save();
    }
    if (!node.isLocked()){
      node.lock(true, true);
    }
    node.unlock();
    
  }

  @Override
  public int getEventType() {
    // TODO Auto-generated method stub
    return ExtendedEvent.UNLOCK;
  }
  public void tearDown(Context ctx) throws RepositoryException{
    Node node = (Node) ctx.get("node");
    if(node.isLocked())
      node.unlock();
  }

}

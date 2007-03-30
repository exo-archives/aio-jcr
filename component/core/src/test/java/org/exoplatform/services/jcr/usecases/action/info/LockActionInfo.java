/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.action.info;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.commons.chain.Context;
import org.exoplatform.services.jcr.observation.ExtendedEvent;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: LockActionInfo.java 13421 2007-03-15 10:46:47Z geaz $
 */
public class LockActionInfo extends ActionInfo {


  @Override
  public int getEventType() {
    // TODO Auto-generated method stub
    return ExtendedEvent.LOCK;
  }

  @Override
  public void execute(Context ctx) throws RepositoryException {
    Node node = (Node) ctx.get("node");
    if (node.canAddMixin("mix:lockable"))
      node.addMixin("mix:lockable");
    node.getSession().save();
    node.lock(true,true);
    //node.getSession().save();
  }
  public void tearDown(Context ctx) throws RepositoryException{
    Node node = (Node) ctx.get("node");
    if(node.isLocked())
      node.unlock();
  }
}

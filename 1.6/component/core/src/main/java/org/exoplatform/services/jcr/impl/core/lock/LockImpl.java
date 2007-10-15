/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.lock;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

import org.exoplatform.services.jcr.core.lock.ExtendedLock;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: LockImpl.java 13866 2007-03-28 13:39:28Z ksm $
 */

public class LockImpl implements ExtendedLock {
  private LockData    lockData;

  private SessionImpl session;

  public LockImpl(SessionImpl session, LockData lockData) {
    this.lockData = lockData;
    this.session = session;
  }

  public String getLockOwner() {
    return lockData.getOwner();
  }

  public String getLockToken() {
    return lockData.getLockToken(session.getId());
  }

  public boolean isLive() {
    return lockData.isLive();
  }

  public void refresh() throws LockException, RepositoryException {
    if (!isLive())
      throw new LockException("Lock is not live");
    lockData.refresh();
  }


  public Node getNode() {
    // TODO Auto-generated method stub
    try {
      return (Node) session.getTransientNodesManager().getItemByIdentifier(lockData.getNodeIdentifier(), true);
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    return null;
  }

  public boolean isDeep() {

    return lockData.isDeep();
  }

  public boolean isSessionScoped() {
    return lockData.isSessionScoped();
  }

  public long getTimeToDeath() {
    return lockData.getTimeToDeath();
  }

  protected void setTimeOut(long timeOut) {
    lockData.setTimeOut(timeOut);
  }
}

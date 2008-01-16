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
package org.exoplatform.services.jcr.impl.core.lock;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

import org.exoplatform.services.jcr.core.lock.ExtendedLock;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS.
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

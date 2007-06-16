/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.lock;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.services.jcr.access.SystemIdentity;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: LockData.java 13866 2007-03-28 13:39:28Z ksm $
 */

public class LockData {
  /**
   * The time of birth. From this time we start count the time of death. death =
   * birthday+TIME_OUT;
   */
  private long        birthday;

  /**
   * If isDeep is true then the lock applies to this node and all its descendant
   * nodes; if false, the lock applies only to this, the holding node.
   */
  private boolean     deep;

  private boolean live;

  /**
   * List of session id's which holds a lock tokens
   */
  private Set<String> lockHolders = new HashSet<String>();

  /**
   * A lock token is a string that uniquely identifies a particular lock and
   * acts as a “key” allowing a user to alter a locked node.
   */
  private String      lockToken;

  /**
   * Identifier of locked node
   */
  private String      nodeIdentifier;

  /**
   * The owner of the locked node
   */
  private String      owner;

  /**
   * If isSessionScoped is true then this lock will expire upon the expiration
   * of the current session (either through an automatic or explicit
   * Session.logout); if false, this lock does not expire until explicitly
   * unlocked or automatically unlocked due to a implementation-specific
   * limitation, such as a timeout
   */
  private boolean     sessionScoped;

  /**
   * <B>8.4.9 Timing Out</B> An implementation may unlock any lock at any time
   * due to implementation-specific criteria, such as time limits on locks.
   */
  private long  timeOut;

  public LockData(String nodeIdentifier,
      String lockToken,
      boolean deep,
      boolean sessionScoped,
      String owner,
      long timeOut) {
    this.nodeIdentifier = nodeIdentifier;
    this.lockToken = lockToken;
    this.deep = deep;
    this.sessionScoped = sessionScoped;
    this.owner = owner;
    this.timeOut = timeOut;
    this.live = true;
    birthday = System.currentTimeMillis()/1000;
  }

  public boolean addLockHolder(String sessionId) {
    return lockHolders.add(sessionId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    }
    if (obj instanceof LockData) {
      return hashCode() == obj.hashCode();
    }
    return false;
  }

  public int getLockHolderSize() {
    return lockHolders.size();
  }

  public String getLockToken(String sessionId) {
    if (isLockHolder(sessionId)) {
      return lockToken;
    }
    return null;
  }

  /**
   * @return the nodeIdentifier
   */
  public String getNodeIdentifier() {
    return nodeIdentifier;
  }

  public String getOwner() {
    return owner;
  }

  /**
   * @return The time to death in millis
   */
  public long getTimeToDeath() {
    return birthday + timeOut - System.currentTimeMillis()/1000;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode() + lockToken.hashCode();
  }

  public boolean isDeep() {
    return deep;
  }

  /**
   * @return the live
   */
  public boolean isLive() {
    return live;
  }

  public boolean isLockHolder(String sessionId) {
    return lockHolders.contains(sessionId) || SystemIdentity.SYSTEM.equals(sessionId);
  }

  public boolean isSessionScoped() {
    return sessionScoped;
  }

  public void refresh() {
    birthday = System.currentTimeMillis();
  }

  public boolean removeLockHolder(String sessionId) {
    return lockHolders.remove(sessionId);
  }

  /**
   * @param the live to set
   */
  public void setLive(boolean live) {
    this.live = live;
  }

  public void setLockToken(String lockToken) {
    this.lockToken = lockToken;
  }

  /**
   * @param nodeIdentifier the nodeIdentifier to set
   */
  public void setNodeIdentifier(String nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  protected long getTimeOut() {
    return timeOut;
  }

  protected void setTimeOut(long timeOut) {
    this.timeOut = timeOut;
  }
}

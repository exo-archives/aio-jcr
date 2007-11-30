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

import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public interface LockManager {
  /**
   * Invoked by a session to inform that a lock token has been added.
   * 
   * @param session session that has a added lock token
   * @param lt added lock token
   */
  public void addLockToken(String sessionId, String lt);

  public Lock addPendingLock(NodeImpl node, boolean isDeep, boolean isSessionScoped, long timeOut)
      throws LockException, RepositoryException;
  /**
   * Returns the Lock object that applies to a node. This may be either a lock
   * on this node itself or a deep lock on a node above this node.
   * 
   * @param node node
   * @return lock object
   * @throws LockException if this node is not locked
   * @see javax.jcr.Node#getLock
   */
  public LockImpl getLock(NodeImpl node) throws LockException, RepositoryException;

  public String[] getLockTokens(String sessionID);

  /**
   * Returns <code>true</code> if the node given holds a lock; otherwise
   * returns <code>false</code>.
   * 
   * @param node node
   * @return <code>true</code> if the node given holds a lock; otherwise
   *         returns <code>false</code>
   * @see javax.jcr.Node#holdsLock
   */
  public boolean holdsLock(NodeData node) throws RepositoryException;


  /**
   * Returns <code>true</code> if this node is locked either as a result of a
   * lock held by this node or by a deep lock on a node above this node;
   * otherwise returns <code>false</code>
   * 
   * @param node node
   * @return <code>true</code> if this node is locked either as a result of a
   *         lock held by this node or by a deep lock on a node above this node;
   *         otherwise returns <code>false</code>
   * @see javax.jcr.Node#isLocked
   */
  public boolean isLocked(NodeData node) ;

  
  /**
   * Returns <code>true</code> if the specified session holds a lock on the
   * given node; otherwise returns <code>false</code>. <p/> Note that
   * <code>isLockHolder(session, node)==true</code> implies
   * <code>holdsLock(node)==true</code>.
   * 
   * @param session session
   * @param node node
   * @return if the specified session holds a lock on the given node; otherwise
   *         returns <code>false</code>
   */
  public boolean isLockHolder(NodeImpl node) throws RepositoryException;


  /**
   * Invoked by a session to inform that a lock token has been removed.
   * 
   * @param session session that has a removed lock token
   * @param lt removed lock token
   */
  public void removeLockToken(String sessionId, String lt);


}

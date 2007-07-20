/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.lock;

import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

import org.picocontainer.Startable;

/**
 * Class for storing information about locks And will be notified about add and
 * removing a lock
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public interface LockPersister extends Startable {
  /**
   * Add lock information to the persistent storage
   * 
   * @param lock
   * @throws RepositoryException
   */
  void add(LockData lock) throws LockException;

  /**
   * Remove lock from persistent storage
   * 
   * @param lock
   * @throws RepositoryException
   */
  void remove(LockData lock) throws LockException;

  /**
   * Remove all locks from persistent storage
   * 
   * @throws RepositoryException
   */
  void removeAll() throws LockException;

}

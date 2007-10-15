/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core;

import java.security.AccessControlException;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL .<br/> The extension for JSR-170 standard
 * Node interface
 * 
 * @author Gennady Azarenkov
 * @version $Id: ExtendedNode.java 13730 2007-03-23 16:25:55Z ksm $
 */

public interface ExtendedNode extends Node {

  /**
   * Sets permission
   * 
   * @param permissions
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void setPermissions(Map<String, String[]> permissions) throws RepositoryException,
      AccessControlException;

  /**
   * @return Access Control List
   */
  AccessControlList getACL() throws RepositoryException;

  /**
   * Clears Access Control List
   * 
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void clearACL() throws RepositoryException, AccessControlException;

  /**
   * Removes permissions for perticular identity
   * 
   * @param identity
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void removePermission(String identity) throws RepositoryException, AccessControlException;

  /**
   * Sets permissions for particular identity
   * 
   * @param identity
   * @param permission
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void setPermission(String identity, String[] permission) throws RepositoryException,
      AccessControlException;

  /**
   * Checks if there are permission to perform some actions
   * 
   * @param actions
   * @throws AccessControlException if no such permissions found
   * @throws RepositoryException
   */
  void checkPermission(String actions) throws AccessControlException, RepositoryException;

  boolean isNodeType(InternalQName qName) throws RepositoryException;

  /**
   * Places a lock on this node.
   * 
   */
  Lock lock(boolean isDeep, long timeOut) throws UnsupportedRepositoryOperationException,
      LockException,
      AccessDeniedException,
      InvalidItemStateException,
      RepositoryException;

}
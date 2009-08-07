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
 * Created by The eXo Platform SAS.<br/>
 * The extension for JSR-170 standard Node interface.
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public interface ExtendedNode extends Node {

  /**
   * Sets permission.
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
   * Clears Access Control List.
   * 
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void clearACL() throws RepositoryException, AccessControlException;

  /**
   * Removes permissions for particular identity.
   * 
   * @param identity
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void removePermission(String identity) throws RepositoryException, AccessControlException;

  /**
   * Removes specified permission for particular identity.
   * 
   * @param identity
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void removePermission(String identity, String permission) throws RepositoryException,
                                                           AccessControlException;

  /**
   * Sets permissions for particular identity.
   * 
   * @param identity
   * @param permission
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void setPermission(String identity, String[] permission) throws RepositoryException,
                                                          AccessControlException;

  /**
   * Checks if there are permission to perform some actions.
   * 
   * @param actions
   * @throws AccessControlException
   *           if no such permissions found
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

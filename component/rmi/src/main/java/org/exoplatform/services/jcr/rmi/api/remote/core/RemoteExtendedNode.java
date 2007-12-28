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

package org.exoplatform.services.jcr.rmi.api.remote.core;

import java.security.AccessControlException;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;

public interface RemoteExtendedNode extends RemoteNode {

  /**
   * Sets permission for children
   * 
   * @param permissions
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void setChildrenPermissions(Map permissions) throws RepositoryException, AccessControlException;

  /**
   * @return Access Control List for children
   */
  AccessControlList getChildrenACL();

  /**
   * Clears children's Access Control List
   * 
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void clearChildrenACL() throws RepositoryException, AccessControlException;

  /**
   * Removes permissions for perticular identity
   * 
   * @param identity
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void removeChildrenPermission(String identity) throws RepositoryException, AccessControlException;

  /**
   * Sets permissions for particular identity
   * 
   * @param identity
   * @param permission
   * @throws RepositoryException
   * @throws AccessControlException
   */
  void setChildrenPermission(String identity, String[] permission) throws RepositoryException,
      AccessControlException;

  /**
   * Checks if there are permission to perform some actions
   * 
   * @param actions
   * @throws AccessControlException if no such permissions found
   * @throws RepositoryException
   */
  void checkChildrenPermission(String actions) throws AccessControlException, RepositoryException;

}

/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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

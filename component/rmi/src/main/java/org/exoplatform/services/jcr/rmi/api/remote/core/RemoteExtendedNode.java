/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
   * @throws AccessControlException
   *           if no such permissions found
   * @throws RepositoryException
   */
  void checkChildrenPermission(String actions) throws AccessControlException, RepositoryException;

}

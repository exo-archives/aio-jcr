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

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository;

/**
 * Remote version of the JCR {@org.exoplatform.services.jcr.core.ManageableRepository
 *  Repository} interface.
 */
public interface RemoteManageableRepository extends Remote, RemoteRepository {
  /**
   * Initializes workspace
   * 
   * @param workspaceName
   * @param rootNodeType
   * @throws RepositoryException
   */
  void initWorkspace(String workspaceName, String rootNodeType) throws RepositoryException;

  /**
   * @param workspaceName
   * @return true if workspace is initialized and false otherwice
   * @throws RepositoryException
   */
  boolean isWorkspaceInitialized(String workspaceName) throws RepositoryException;

  /**
   * @param workspaceName
   * @return the System session (session with SYSTEM identity)
   * @throws RepositoryException
   */
  Session getSystemSession(String workspaceName) throws RepositoryException;

  /**
   * @return array of workspace names
   */
  String[] getWorkspaceNames() throws RemoteException;

  /**
   * @return the node type manager
   */
  ExtendedNodeTypeManager getNodeTypeManager() throws RemoteException;

  /**
   * @return the namespace registry
   */
  NamespaceRegistry getNamespaceRegistry() throws RemoteException;

  /**
   * @return the configuration of this repository
   */
  RepositoryEntry getConfiguration() throws RemoteException;

}

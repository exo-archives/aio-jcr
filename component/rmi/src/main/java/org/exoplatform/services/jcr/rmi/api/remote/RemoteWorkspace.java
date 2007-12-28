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
package org.exoplatform.services.jcr.rmi.api.remote;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeTypeManager;

/**
 * Remote version of the JCR {@link javax.jcr.Workspace Workspace} interface.
 * Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerWorkspace ServerWorkspace}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientWorkspace ClientWorkspace}
 * adapters to provide transparent RMI access to remote workspaces.
 * <p>
 * Most of the methods in this interface are documented only with a reference to
 * a corresponding Workspace method. In these cases the remote object will
 * simply forward the method call to the underlying Workspace instance. Complex
 * return values like namespace registries and other objects are returned as
 * remote references to the corresponding remote interface. Simple return values
 * and possible exceptions are copied over the network to the client. RMI errors
 * are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.Workspace
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientWorkspace
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerWorkspace
 */
public interface RemoteWorkspace extends Remote {

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#getName() Workspace.getName()} method.
   * 
   * @return workspace name
   * @throws RemoteException on RMI errors
   */
  String getName() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#copy(String,String) Workspace.copy(String,String)}
   * method.
   * 
   * @param from source path
   * @param to destination path
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void copy(String from, String to) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#copy(String,String,String) Workspace.copy(String,String,String)}
   * method.
   * 
   * @param workspace source workspace
   * @param from source path
   * @param to destination path
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void copy(String workspace, String from, String to) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#clone(String,String,String,boolean) Workspace.clone(String,String,String,boolean)}
   * method.
   * 
   * @param workspace source workspace
   * @param from source path
   * @param to destination path
   * @param removeExisting flag to remove existing items
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void clone(String workspace, String from, String to, boolean removeExisting)
      throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#move(String,String) Workspace.move(String,String)}
   * method.
   * 
   * @param from source path
   * @param to destination path
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void move(String from, String to) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#getNodeTypeManager() Workspace.getNodeTypeManager()}
   * method.
   * 
   * @return node type manager
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteNodeTypeManager getNodeTypeManager() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#getNamespaceRegistry() Workspace.getNamespaceRegistry()}
   * method.
   * 
   * @return namespace registry
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteNamespaceRegistry getNamespaceRegistry() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#getQueryManager() Workspace.getQueryManager()}
   * method.
   * 
   * @return query manager
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteQueryManager getQueryManager() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#getObservationManager() Workspace.getObservationManager()}
   * method.
   * 
   * @return observation manager
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteObservationManager getObservationManager() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#getAccessibleWorkspaceNames() Workspace.getAccessibleWorkspaceNames()}
   * method.
   * 
   * @return accessible workspace names
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String[] getAccessibleWorkspaceNames() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Workspace#importXML(String,java.io.InputStream,int) Workspace.importXML(String,InputStream,int)}
   * method.
   * 
   * @param path node path
   * @param xml imported XML document
   * @param uuidBehaviour uuid behaviour flag
   * @throws IOException on IO errors
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void importXML(String path, byte[] xml, int uuidBehaviour) throws IOException,
      RepositoryException, RemoteException;

  /**
   * * Remote version of the
   * {@link javax.jcr.Workspace#restore(Version[] versions, boolean removeExisting) Workspace.restore(String[] versionuuids, boolean removeExisting)}
   * method.
   * 
   * @param versions The set of versionuudis to be restored
   * @param removeExisting governs what happens on UUID collision.
   * @throws ItemExistsException if <code>removeExisting</code> is
   *           <code>false</code> and a UUID collision occurs with a node
   *           being restored.
   * @throws UnsupportedRepositoryOperationException if one or more of the nodes
   *           to be restored is not versionable.
   * @throws VersionException if the set of versions to be restored is such that
   *           the original path location of one or more of the versions cannot
   *           be determined or if the <code>restore</code> would change the
   *           state of a existing verisonable node that is currently checked-in
   *           or if a root version (<code>jcr:rootVersion</code>) is among
   *           those being restored.
   * @throws LockException if a lock prevents the restore.
   * @throws InvalidItemStateException if this <code>Session</code> (not
   *           necessarily this <code>Node</code>) has pending unsaved
   *           changes.
   * @throws RepositoryException if another error occurs.
   */
  void restore(String[] versionUuids, boolean removeExisting) throws ItemExistsException,
      UnsupportedRepositoryOperationException, VersionException, LockException,
      InvalidItemStateException, RepositoryException, RemoteException;

}

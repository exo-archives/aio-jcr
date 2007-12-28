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
package org.exoplatform.services.jcr.rmi.impl.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteObservationManager;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteWorkspace;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeTypeManager;

/**
 * Remote adapter for the JCR {@link Workspace Workspace} interface. This class
 * makes a local workspace available as an RMI service using the
 * {@link RemoteWorkspace RemoteWorkspace} interface.
 * 
 * @see Workspace
 * @see RemoteWorkspace
 */
public class ServerWorkspace extends ServerObject implements RemoteWorkspace {

  /**
   * 
   */
  private static final long        serialVersionUID = 7047140615388378488L;

  /** The adapted local workspace. */
  private Workspace                workspace;

  /**
   * The remote observation manager for this workspace. This field is assigned
   * on demand by the first call to {@link #getObservationManager()}. The
   * assumption is that there is only one observation manager instance per
   * workspace and that each call to the
   * <code>Workspace.getObservationManager()</code> method of a single
   * workspace will allways return the same object.
   */
  private RemoteObservationManager remoteObservationManager;

  /**
   * Creates a remote adapter for the given local workspace.
   * 
   * @param workspace local workspace
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerWorkspace(Workspace workspace, RemoteAdapterFactory factory) throws RemoteException {
    super(factory);
    this.workspace = workspace;
  }

  /** {@inheritDoc} */
  public String getName() throws RemoteException {
    return workspace.getName();
  }

  /** {@inheritDoc} */
  public void copy(String from, String to) throws RepositoryException, RemoteException {
    try {
      workspace.copy(from, to);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void copy(String workspace, String from, String to) throws RepositoryException,
      RemoteException {
    try {
      this.workspace.copy(workspace, from, to);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void clone(String workspace, String from, String to, boolean removeExisting)
      throws RepositoryException, RemoteException {
    try {
      this.workspace.clone(workspace, from, to, removeExisting);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void move(String from, String to) throws RepositoryException, RemoteException {
    try {
      workspace.move(from, to);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteNodeTypeManager getNodeTypeManager() throws RepositoryException, RemoteException {

    try {
      NodeTypeManager manager = workspace.getNodeTypeManager();
      return getFactory().getRemoteNodeTypeManager(manager);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteNamespaceRegistry getNamespaceRegistry() throws RepositoryException, RemoteException {
    try {
      NamespaceRegistry registry = workspace.getNamespaceRegistry();
      return getFactory().getRemoteNamespaceRegistry(registry);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteQueryManager getQueryManager() throws RepositoryException, RemoteException {
    try {
      QueryManager queryManager = workspace.getQueryManager();
      return getFactory().getRemoteQueryManager(queryManager, workspace.getSession());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }

  }

  /** {@inheritDoc} */
  public RemoteObservationManager getObservationManager() throws RepositoryException,
      RemoteException {
    try {
      if (remoteObservationManager == null) {
        ObservationManager observationManager = workspace.getObservationManager();
        remoteObservationManager = getFactory().getRemoteObservationManager(observationManager);
      }
      return remoteObservationManager;
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }

  }

  /** {@inheritDoc} */
  public String[] getAccessibleWorkspaceNames() throws RepositoryException, RemoteException {
    try {
      return workspace.getAccessibleWorkspaceNames();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void importXML(String path, byte[] xml, int uuidBehaviour) throws IOException,
      RepositoryException, RemoteException {
    try {
      workspace.importXML(path, new ByteArrayInputStream(xml), uuidBehaviour);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void restore(String[] versionUuids, boolean removeExisting) throws ItemExistsException,
      UnsupportedRepositoryOperationException, VersionException, LockException,
      InvalidItemStateException, RepositoryException, RemoteException {

    if (versionUuids != null) {
      // restore original array
      Version[] versions = new Version[versionUuids.length];
      for (int i = 0; i < versionUuids.length; i++) {
        versions[i] = (Version) workspace.getSession().getNodeByUUID(versionUuids[i]);
      }
      workspace.restore(versions, removeExisting);
    }
  }

}

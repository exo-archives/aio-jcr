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

package org.exoplatform.services.jcr.rmi.api.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import javax.jcr.NamespaceRegistry;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteWorkspace;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeTypeManager;
import org.exoplatform.services.jcr.rmi.api.xml.WorkspaceImportContentHandler;
import org.xml.sax.ContentHandler;

/**
 * Local adapter for the JCR-RMI {@link RemoteWorkspace RemoteWorkspace} interface. This class makes
 * a remote workspace locally available using the JCR {@link Workspace Workspace} interface.
 * 
 * @see javax.jcr.Workspace
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteWorkspace
 */
public class ClientWorkspace extends ClientObject implements Workspace {

  /** The current session. */
  private Session            session;

  /** The adapted remote workspace. */
  private RemoteWorkspace    remote;

  /**
   * The adapted observation manager of this workspace. This field is set on the first call to the
   * {@link #getObservationManager()()} method assuming, that the observation manager instance is
   * not changing during the lifetime of a workspace instance, that is, each call to the server-side
   * <code>Workspace.getObservationManager()</code> allways returns the same object.
   */
  private ObservationManager observationManager;

  /**
   * Creates a client adapter for the given remote workspace.
   * 
   * @param session
   *          current session
   * @param remote
   *          remote workspace
   * @param factory
   *          local adapter factory
   */
  public ClientWorkspace(Session session, RemoteWorkspace remote, LocalAdapterFactory factory) {
    super(factory);
    this.session = session;
    this.remote = remote;
  }

  /**
   * Returns the current session without contacting the remote workspace. {@inheritDoc}
   */
  public Session getSession() {
    return session;
  }

  /** {@inheritDoc} */
  public String getName() {
    try {
      return remote.getName();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public void copy(String from, String to) throws RepositoryException {
    try {
      remote.copy(from, to);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void copy(String workspace, String from, String to) throws RepositoryException {
    try {
      remote.copy(workspace, from, to);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void move(String from, String to) throws RepositoryException {
    try {
      remote.move(from, to);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public QueryManager getQueryManager() throws RepositoryException {

    try {
      RemoteQueryManager manager = remote.getQueryManager();
      return getFactory().getQueryManager(session, manager);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }

  }

  /** {@inheritDoc} */
  public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
    try {
      RemoteNamespaceRegistry registry = remote.getNamespaceRegistry();
      return getFactory().getNamespaceRegistry(registry);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeTypeManager getNodeTypeManager() throws RepositoryException {
    try {
      RemoteNodeTypeManager manager = remote.getNodeTypeManager();
      return getFactory().getNodeTypeManager(manager);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public ObservationManager getObservationManager() throws RepositoryException {
    if (observationManager == null) {
      try {
        observationManager = getFactory().getObservationManager(this,
                                                                remote.getObservationManager());
      } catch (RemoteException ex) {
        throw new RemoteRepositoryException(ex);
      }
    }

    return observationManager;
  }

  /** {@inheritDoc} */
  public void clone(String workspace, String src, String dst, boolean removeExisting) throws RepositoryException {
    try {
      remote.clone(workspace, src, dst, removeExisting);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getAccessibleWorkspaceNames() throws RepositoryException {
    try {
      return remote.getAccessibleWorkspaceNames();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public ContentHandler getImportContentHandler(String path, int uuidBehaviour) throws RepositoryException,
                                                                               PathNotFoundException,
                                                                               ConstraintViolationException,
                                                                               VersionException {
    return new WorkspaceImportContentHandler(this, path, uuidBehaviour);

  }

  /** {@inheritDoc} */
  public void importXML(String path, InputStream xml, int uuidBehaviour) throws IOException,
                                                                        RepositoryException {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] bytes = new byte[4096];
      for (int n = xml.read(bytes); n != -1; n = xml.read(bytes)) {
        buffer.write(bytes, 0, n);
      }
      remote.importXML(path, buffer.toByteArray(), uuidBehaviour);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void restore(Version[] versions, boolean removeExisting) throws RepositoryException {
    if (versions != null) {
      String[] versionUuids = new String[versions.length];
      for (int i = 0; i < versions.length; i++) {
        versionUuids[i] = versions[i].getUUID();
      }
      try {
        remote.restore(versionUuids, removeExisting);
      } catch (RemoteException ex) {
        throw new RemoteRepositoryException(ex);
      }
    }
  }
}

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

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory;

/**
 * Remote adapter for the JCR
 * {@link javax.jcr.version.VersionHistory VersionHistory} interface. This class
 * makes a local version history available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory RemoteVersionHistory}
 * interface.
 * 
 * @see javax.jcr.version.VersionHistory
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory
 */
public class ServerVersionHistory extends ServerNode implements RemoteVersionHistory {

  /**
   * 
   */
  private static final long serialVersionUID = -7843679528744537165L;

  /** The adapted local version history. */
  private VersionHistory    versionHistory;

  /**
   * Creates a remote adapter for the given local version history.
   * 
   * @param versionHistory local version history
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerVersionHistory(VersionHistory versionHistory, RemoteAdapterFactory factory)
      throws RemoteException {
    super(versionHistory, factory);
    this.versionHistory = versionHistory;
  }

  /** {@inheritDoc} */
  public RemoteVersion getRootVersion() throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemoteVersion(versionHistory.getRootVersion());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteIterator getAllVersions() throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemoteVersionIterator(versionHistory.getAllVersions());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteVersion getVersion(String versionName) throws RepositoryException, RemoteException {
    try {
      Version version = versionHistory.getVersion(versionName);
      return getFactory().getRemoteVersion(version);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteVersion getVersionByLabel(String label) throws RepositoryException, RemoteException {
    try {
      Version version = versionHistory.getVersionByLabel(label);
      return getFactory().getRemoteVersion(version);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void addVersionLabel(String versionName, String label, boolean moveLabel)
      throws RepositoryException, RemoteException {
    try {
      versionHistory.addVersionLabel(versionName, label, moveLabel);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void removeVersionLabel(String label) throws RepositoryException, RemoteException {
    try {
      versionHistory.removeVersionLabel(label);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean hasVersionLabel(String label) throws RepositoryException, RemoteException {
    return versionHistory.hasVersionLabel(label);
  }

  /** {@inheritDoc} */
  public boolean hasVersionLabel(String versionUUID, String label) throws RepositoryException,
      RemoteException {
    try {
      Version version = getVersionByUUID(versionUUID);
      return versionHistory.hasVersionLabel(version, label);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getVersionLabels() throws RepositoryException, RemoteException {
    return versionHistory.getVersionLabels();
  }

  /** {@inheritDoc} */
  public String[] getVersionLabels(String versionUUID) throws RepositoryException, RemoteException {
    try {
      Version version = getVersionByUUID(versionUUID);
      return versionHistory.getVersionLabels(version);
    } catch (ClassCastException cce) {
      // we do not expect this here as nodes should be returned correctly
      throw getRepositoryException(new RepositoryException(cce));
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void removeVersion(String versionName) throws RepositoryException, RemoteException {
    try {
      versionHistory.removeVersion(versionName);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getVersionableUUID() throws RepositoryException, RemoteException {
    return versionHistory.getVersionableUUID();
  }
}

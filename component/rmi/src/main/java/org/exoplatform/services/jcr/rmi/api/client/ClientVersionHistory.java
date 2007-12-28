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
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory RemoteVersionHistory}
 * interface. This class makes a remote version history locally available using
 * the JCR {@link javax.jcr.version.VersionHistory VersionHistory} interface.
 * 
 * @see javax.jcr.version.VersionHistory
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory
 */
public class ClientVersionHistory extends ClientNode implements VersionHistory {

  /** The adapted remote version history. */
  private RemoteVersionHistory remote;

  /**
   * Creates a local adapter for the given remote version history.
   * 
   * @param session current session
   * @param remote remote version history
   * @param factory local adapter factory
   */
  public ClientVersionHistory(Session session, RemoteVersionHistory remote,
      LocalAdapterFactory factory) {
    super(session, remote, factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public Version getRootVersion() throws RepositoryException {
    try {
      return getFactory().getVersion(getSession(), remote.getRootVersion());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public VersionIterator getAllVersions() throws RepositoryException {
    try {
      return getFactory().getVersionIterator(getSession(), remote.getAllVersions());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Version getVersion(String versionName) throws VersionException, RepositoryException {
    try {
      return getFactory().getVersion(getSession(), remote.getVersion(versionName));
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Version getVersionByLabel(String label) throws RepositoryException {
    try {
      return getFactory().getVersion(getSession(), remote.getVersionByLabel(label));
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void addVersionLabel(String versionName, String label, boolean moveLabel)
      throws VersionException, RepositoryException {
    try {
      remote.addVersionLabel(versionName, label, moveLabel);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void removeVersionLabel(String label) throws VersionException, RepositoryException {
    try {
      remote.removeVersionLabel(label);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean hasVersionLabel(String label) throws RepositoryException {
    try {
      return remote.hasVersionLabel(label);
    } catch (RemoteException ex) {
      // grok the exception and assume label is missing
      return false;
    }
  }

  /** {@inheritDoc} */
  public boolean hasVersionLabel(Version version, String label) throws VersionException,
      RepositoryException {
    try {
      String versionUUID = version.getUUID();
      return remote.hasVersionLabel(versionUUID, label);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getVersionLabels() throws RepositoryException {
    try {
      return remote.getVersionLabels();
    } catch (RemoteException ex) {
      // grok the exception and return an empty array
      return new String[0];
    }
  }

  /** {@inheritDoc} */
  public String[] getVersionLabels(Version version) throws VersionException, RepositoryException {
    try {
      String versionUUID = version.getUUID();
      return remote.getVersionLabels(versionUUID);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void removeVersion(String versionName) throws UnsupportedRepositoryOperationException,
      VersionException, RepositoryException {
    try {
      remote.removeVersion(versionName);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getVersionableUUID() throws RepositoryException {
    try {
      return remote.getVersionableUUID();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }
}

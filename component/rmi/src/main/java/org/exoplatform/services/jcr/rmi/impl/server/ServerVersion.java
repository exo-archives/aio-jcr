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
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory;

/**
 * Remote adapter for the JCR {@link javax.jcr.version.Version Version}
 * interface. This class makes a local version available as an RMI service using
 * the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion RemoteVersion}
 * interface.
 * 
 * @see javax.jcr.version.Version
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion
 */
public class ServerVersion extends ServerNode implements RemoteVersion {

  /**
   * 
   */
  private static final long serialVersionUID = -7400170340122244982L;

  /** The adapted local version. */
  private Version           version;

  /**
   * Creates a remote adapter for the given local version.
   * 
   * @param version local version
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerVersion(Version version, RemoteAdapterFactory factory) throws RemoteException {
    super(version, factory);
    this.version = version;
  }

  /**
   * Utility method for creating an array of remote references for local
   * versions. The remote references are created using the remote adapter
   * factory.
   * <p>
   * A <code>null</code> input is treated as an empty array.
   * 
   * @param versions local version array
   * @return remote version array
   * @throws RemoteException on RMI errors
   */
  private RemoteVersion[] getRemoteVersionArray(Version[] versions) throws RemoteException {
    if (versions != null) {
      RemoteVersion[] remotes = new RemoteVersion[versions.length];
      for (int i = 0; i < remotes.length; i++) {
        remotes[i] = getFactory().getRemoteVersion(versions[i]);
      }
      return remotes;
    } else {
      return new RemoteVersion[0]; // for safety
    }
  }

  /** {@inheritDoc} */
  public Calendar getCreated() throws RepositoryException {
    try {
      return version.getCreated();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteVersion[] getSuccessors() throws RepositoryException, RemoteException {
    try {
      return getRemoteVersionArray(version.getSuccessors());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteVersion[] getPredecessors() throws RepositoryException, RemoteException {
    try {
      return getRemoteVersionArray(version.getPredecessors());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteVersionHistory getContainingHistory() throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemoteVersionHistory(version.getContainingHistory());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }
}

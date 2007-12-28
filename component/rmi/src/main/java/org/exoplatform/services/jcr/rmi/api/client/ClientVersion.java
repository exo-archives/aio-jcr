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
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion RemoteVersion}
 * interface. This class makes a remote version locally available using the JCR
 * {@link javax.jcr.version.Version Version} interface.
 * 
 * @see javax.jcr.version.Version
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion
 */
public class ClientVersion extends ClientNode implements Version {

  /** The adapted remote version. */
  private RemoteVersion remote;

  /**
   * Creates a local adapter for the given remote version.
   * 
   * @param session current session
   * @param remote remote version
   * @param factory local adapter factory
   */
  public ClientVersion(Session session, RemoteVersion remote, LocalAdapterFactory factory) {
    super(session, remote, factory);
    this.remote = remote;
  }

  /**
   * Utility method for creating a version array for an array of remote
   * versions. The versions in the returned array are created using the local
   * adapter factory.
   * <p>
   * A <code>null</code> input is treated as an empty array.
   * 
   * @param remotes remote versions
   * @return local version array
   */
  private Version[] getVersionArray(RemoteVersion[] remotes) {
    if (remotes != null) {
      Version[] versions = new Version[remotes.length];
      for (int i = 0; i < remotes.length; i++) {
        versions[i] = getFactory().getVersion(getSession(), remotes[i]);
      }
      return versions;
    } else {
      return new Version[0]; // for safety
    }
  }

  /** {@inheritDoc} */
  public Calendar getCreated() throws RepositoryException {
    try {
      return remote.getCreated();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Version[] getSuccessors() throws RepositoryException {
    try {
      return getVersionArray(remote.getSuccessors());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Version[] getPredecessors() throws RepositoryException {
    try {
      return getVersionArray(remote.getPredecessors());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public VersionHistory getContainingHistory() throws RepositoryException {
    try {
      return getFactory().getVersionHistory(getSession(), remote.getContainingHistory());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }
}

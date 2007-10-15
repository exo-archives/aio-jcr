/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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

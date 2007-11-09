/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteSession;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository RemoteRepository}
 * inteface. This class makes a remote repository locally available using the
 * JCR {@link javax.jcr.Repository Repository} interface.
 * 
 * @see javax.jcr.Repository
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository
 */
public class ClientRepository extends ClientObject implements Repository {

  /** The adapted remote repository. */
  private RemoteRepository remote;

  /**
   * Creates a client adapter for the given remote repository.
   * 
   * @param remote remote repository
   * @param factory local adapter factory
   */
  public ClientRepository(RemoteRepository remote, LocalAdapterFactory factory) {
    super(factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public String getDescriptor(String name) {
    try {
      return remote.getDescriptor(name);
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getDescriptorKeys() {
    try {
      return remote.getDescriptorKeys();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login() throws RepositoryException {
    try {
      RemoteSession session = remote.login();
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login(String workspace) throws RepositoryException {
    try {
      RemoteSession session = remote.login(workspace);
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login(Credentials credentials) throws RepositoryException {
    try {
      RemoteSession session = remote.login(credentials);
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login(Credentials credentials, String workspace) throws RepositoryException {
    try {
      RemoteSession session = remote.login(credentials, workspace);
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

}

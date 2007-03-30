/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.AccessControlException;

import javax.jcr.Credentials;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteItem;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteSession;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteWorkspace;

/**
 * Remote adapter for the JCR {@link javax.jcr.Session Session} interface. This
 * class makes a local session available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteSession RemoteSession}
 * interface.
 * 
 * @see javax.jcr.Session
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteSession
 */
public class ServerSession extends ServerObject implements RemoteSession {

  /**
   * 
   */
  private static final long serialVersionUID = -1507070576822020330L;

  /** The adapted local session. */
  private Session           session;

  /**
   * The server workspace for this session. This field is assigned on demand by
   * the first call to {@link #getWorkspace()}. The assumption is that there is
   * only one workspace instance per session and that each call to the
   * <code>Session.getWorkspace()</code> method of a single session will
   * allways return the same object.
   */
  private RemoteWorkspace   remoteWorkspace;

  /**
   * Creates a remote adapter for the given local session.
   * 
   * @param session local session
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */

  private LocationFactory   locationFactory;

  public ServerSession(Session session, RemoteAdapterFactory factory) throws RemoteException {
    super(factory);
    this.session = session;
    this.locationFactory = new LocationFactory((NamespaceAccessor) session);
  }

  /** {@inheritDoc} */
  public String getUserID() throws RemoteException {
    return session.getUserID();
  }

  /** {@inheritDoc} */
  public Object getAttribute(String name) throws RemoteException {
    return session.getAttribute(name);
  }

  /** {@inheritDoc} */
  public String[] getAttributeNames() throws RemoteException {
    return session.getAttributeNames();
  }

  /** {@inheritDoc} */
  public RemoteSession impersonate(Credentials credentials) throws RepositoryException,
      RemoteException {
    try {
      Session newSession = session.impersonate(credentials);
      return getFactory().getRemoteSession(newSession);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteWorkspace getWorkspace() throws RemoteException {
    if (remoteWorkspace == null) {
      remoteWorkspace = getFactory().getRemoteWorkspace(session.getWorkspace());
    }

    return remoteWorkspace;
  }

  /** {@inheritDoc} */
  public void checkPermission(String path, String actions) throws AccessControlException,
      RepositoryException, RemoteException {
    session.checkPermission(path, actions);
  }

  /** {@inheritDoc} */
  public String getNamespacePrefix(String uri) throws RepositoryException, RemoteException {
    try {
      return session.getNamespacePrefix(uri);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getNamespacePrefixes() throws RepositoryException, RemoteException {
    try {
      return session.getNamespacePrefixes();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getNamespaceURI(String prefix) throws RepositoryException, RemoteException {
    try {
      return session.getNamespaceURI(prefix);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void setNamespacePrefix(String prefix, String uri) throws RepositoryException,
      RemoteException {
    try {
      session.setNamespacePrefix(prefix, uri);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean itemExists(String path) throws RepositoryException, RemoteException {
    return session.itemExists(path);
  }

  /** {@inheritDoc} */
  public RemoteNode getNodeByUUID(String uuid) throws RepositoryException, RemoteException {
    try {
      return getRemoteNode(session.getNodeByUUID(uuid));
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteNode getRootNode() throws RepositoryException, RemoteException {
    try {
      return getRemoteNode(session.getRootNode());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteItem getItem(String path) throws RepositoryException, RemoteException {
    try {
      return getRemoteItem(session.getItem(path));
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean hasPendingChanges() throws RepositoryException, RemoteException {
    try {
      return session.hasPendingChanges();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void move(String from, String to) throws RepositoryException, RemoteException {
    try {
      session.move(from, to);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void save() throws RepositoryException, RemoteException {
    try {
      session.save();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void refresh(boolean keepChanges) throws RepositoryException, RemoteException {
    try {
      session.refresh(keepChanges);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void logout() throws RemoteException {
    session.logout();
  }

  /** {@inheritDoc} */
  public boolean isLive() throws RemoteException {
    return session.isLive();
  }

  /** {@inheritDoc} */
  public void importXML(String path, byte[] xml, int mode) throws IOException, RepositoryException,
      RemoteException {
    try {
      session.importXML(path, new ByteArrayInputStream(xml), mode);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void addLockToken(String token) throws RemoteException {
    session.addLockToken(token);
  }

  /** {@inheritDoc} */
  public String[] getLockTokens() throws RemoteException {
    return session.getLockTokens();
  }

  /** {@inheritDoc} */
  public void removeLockToken(String token) throws RemoteException {
    session.removeLockToken(token);
  }

  /** {@inheritDoc} */
  public byte[] exportDocumentView(String path, boolean binaryAsLink, boolean noRecurse)
      throws IOException, RepositoryException, RemoteException {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      session.exportDocumentView(path, buffer, binaryAsLink, noRecurse);
      return buffer.toByteArray();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public byte[] exportSystemView(String path, boolean binaryAsLink, boolean noRecurse)
      throws IOException, RepositoryException, RemoteException {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      session.exportSystemView(path, buffer, binaryAsLink, noRecurse);
      return buffer.toByteArray();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  // public JCRName getJCRName(String path) {
  //    
  // // if (session instanceof SessionImpl){
  // // ((SessionImpl)session).getJCRName(path)
  // // }
  // return null;
  // TODO Auto-generated method stub

  // return this.locationFactory.;

  // }

  public String[] getAllNamespacePrefixes() throws RepositoryException, RemoteException {
    if (session instanceof SessionImpl) {
      return ((SessionImpl) session).getAllNamespacePrefixes();
    }
    return null;
  }

  public String getNamespacePrefixByURI(String uri) throws NamespaceException, RepositoryException,
      RemoteException {
    if (session instanceof SessionImpl) {
      return ((SessionImpl) session).getNamespacePrefixByURI(uri);
    }
    return null;
  }

  public String getNamespaceURIByPrefix(String prefix) throws NamespaceException,
      RepositoryException, RemoteException {
    if (session instanceof SessionImpl) {
      return ((SessionImpl) session).getNamespacePrefixByURI(prefix);
    }
    return null;
  }

}

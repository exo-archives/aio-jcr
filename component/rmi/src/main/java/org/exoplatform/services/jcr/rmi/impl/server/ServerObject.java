/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.MergeException;
import javax.jcr.NamespaceException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteItem;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Base class for remote adapters. The purpose of this class is to centralize
 * the handling of the RemoteAdapterFactory instance used to instantiate new
 * server adapters.
 */
public class ServerObject extends UnicastRemoteObject {

  private static final long    serialVersionUID = 8603930858205371875L;

  /** Factory for creating server adapters. */
  private RemoteAdapterFactory factory;

  /**
   * Creates a basic server adapter that uses the given factory to create new
   * adapters.
   * 
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  protected ServerObject(RemoteAdapterFactory factory) throws RemoteException {
    this.factory = factory;
  }

  /**
   * Returns the remote adapter factory used to create new adapters.
   * 
   * @return remote adapter factory
   */
  protected RemoteAdapterFactory getFactory() {
    return factory;
  }

  /**
   * Returns a cleaned version of the given exception. In some cases the
   * underlying repository implementation may throw exceptions that are either
   * unserializable, use exception subclasses that are only locally available,
   * contain references to unserializable or only locally available classes.
   * This method returns a cleaned version of such an exception. The returned
   * exception contains only the message string from the original exception, and
   * uses the public JCR exception class that most specifically matches the
   * original exception.
   * 
   * @param ex the original exception
   * @return clean exception
   */
  protected RepositoryException getRepositoryException(RepositoryException ex) {
    if (ex instanceof AccessDeniedException) {
      return new AccessDeniedException(ex.getMessage());
    } else if (ex instanceof ConstraintViolationException) {
      return new ConstraintViolationException(ex.getMessage());
    } else if (ex instanceof InvalidItemStateException) {
      return new InvalidItemStateException(ex.getMessage());
    } else if (ex instanceof InvalidQueryException) {
      return new InvalidQueryException(ex.getMessage());
    } else if (ex instanceof InvalidSerializedDataException) {
      return new InvalidSerializedDataException(ex.getMessage());
    } else if (ex instanceof ItemExistsException) {
      return new ItemExistsException(ex.getMessage());
    } else if (ex instanceof ItemNotFoundException) {
      return new ItemNotFoundException(ex.getMessage());
    } else if (ex instanceof LockException) {
      return new LockException(ex.getMessage());
    } else if (ex instanceof LoginException) {
      return new LoginException(ex.getMessage());
    } else if (ex instanceof MergeException) {
      return new MergeException(ex.getMessage());
    } else if (ex instanceof NamespaceException) {
      return new NamespaceException(ex.getMessage());
    } else if (ex instanceof NoSuchNodeTypeException) {
      return new NoSuchNodeTypeException(ex.getMessage());
    } else if (ex instanceof NoSuchWorkspaceException) {
      return new NoSuchWorkspaceException(ex.getMessage());
    } else if (ex instanceof PathNotFoundException) {
      return new PathNotFoundException(ex.getMessage());
    } else if (ex instanceof ReferentialIntegrityException) {
      return new ReferentialIntegrityException(ex.getMessage());
    } else if (ex instanceof UnsupportedRepositoryOperationException) {
      return new UnsupportedRepositoryOperationException(ex.getMessage());
    } else if (ex instanceof ValueFormatException) {
      return new ValueFormatException(ex.getMessage());
    } else if (ex instanceof VersionException) {
      return new VersionException(ex.getMessage());
    } else {
      return new RepositoryException(ex.getMessage());
    }
  }

  /**
   * Utility method for creating a remote reference for a local item. Unlike the
   * factory method for creating remote item references, this method introspects
   * the type of the local item and returns the corresponding node, property, or
   * item remote reference using the remote adapter factory.
   * <p>
   * If the <code>item</code>, this method calls the
   * {@link #getRemoteNode(Node)} to return the correct remote type.
   * 
   * @param item local node, property, or item
   * @return remote node, property, or item reference
   * @throws RemoteException on RMI errors
   */
  protected RemoteItem getRemoteItem(Item item) throws RemoteException {
    if (item instanceof Property) {
      return factory.getRemoteProperty((Property) item);
    } else if (item instanceof Node) {
      return getRemoteNode((Node) item);
    } else {
      return factory.getRemoteItem(item);
    }
  }

  /**
   * Utility method for creating a remote reference for a local node. Unlike the
   * factory method for creating remote node references, this method introspects
   * the type of the local node and returns the corresponding node, version, or
   * version history remote reference using the remote adapter factory.
   * 
   * @param node local version, versionhistory, or normal node
   * @return remote node, property, or item reference
   * @throws RemoteException on RMI errors
   */
  protected RemoteNode getRemoteNode(Node node) throws RemoteException {
    if (node instanceof Version) {
      return factory.getRemoteVersion((Version) node);
    } else if (node instanceof VersionHistory) {
      return factory.getRemoteVersionHistory((VersionHistory) node);
    } else {
      return factory.getRemoteNode(node);
    }
  }

  /**
   * Utility method for creating an array of remote references for local node
   * types. The remote references are created using the remote adapter factory.
   * <p>
   * A <code>null</code> input is treated as an empty array.
   * 
   * @param types local node type array
   * @return remote node type array
   * @throws RemoteException on RMI errors
   */
  protected RemoteNodeType[] getRemoteNodeTypeArray(NodeType[] types) throws RemoteException {
    if (types != null) {
      RemoteNodeType[] remotes = new RemoteNodeType[types.length];
      for (int i = 0; i < types.length; i++) {
        remotes[i] = factory.getRemoteNodeType(types[i]);
      }
      return remotes;
    } else {
      return new RemoteNodeType[0]; // for safety
    }
  }
}

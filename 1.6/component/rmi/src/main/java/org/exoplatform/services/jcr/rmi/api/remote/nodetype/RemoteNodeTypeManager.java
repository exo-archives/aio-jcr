/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.remote.nodetype;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;

/**
 * Remote version of the JCR
 * {@link javax.jcr.nodetype.NodeTypeManager NodeTypeManager} interface. Used by
 * the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerNodeTypeManager ServerNodeTypeManager}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientNodeTypeManager ClientNodeTypeManager}
 * adapters to provide transparent RMI access to remote node type managers.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding NodeTypeManager method. The remote object will simply forward
 * the method call to the underlying NodeTypeManager instance. Arguments and
 * possible exceptions are copied over the network. Complex
 * {@link javax.jcr.nodetype.NodeType NodeType} values are returned as remote
 * references to the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeType RemoteNodeType}
 * interface. Iterator values are transmitted as object arrays. RMI errors are
 * signalled with RemoteExceptions.
 */
public interface RemoteNodeTypeManager extends Remote {

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.NodeTypeManager#getNodeType(String) NodeTypeManager.getNodeType(String)}
   * method.
   * 
   * @param name node type name
   * @return node type
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteNodeType getNodeType(String name) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.NodeTypeManager#getAllNodeTypes() NodeTypeManager.getAllNodeTypes()}
   * method.
   * 
   * @return all node types
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteIterator getAllNodeTypes() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.NodeTypeManager#getPrimaryNodeTypes() NodeTypeManager.getPrimaryNodeTypes()}
   * method.
   * 
   * @return primary node types
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteIterator getPrimaryNodeTypes() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.NodeTypeManager#getMixinNodeTypes() NodeTypeManager.getMixinNodeTypes()}
   * method.
   * 
   * @return mixin node types
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteIterator getMixinNodeTypes() throws RepositoryException, RemoteException;

}

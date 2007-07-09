/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server.iterator;

import java.rmi.RemoteException;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;

/**
 * A ServerIterator for iterating node types.
 */
public class ServerNodeTypeIterator extends ServerIterator {

  /**
   * Creates a ServerNodeTypeIterator instance.
   * 
   * @param iterator local node type iterator
   * @param factory remote adapter factory
   * @param maxBufferSize maximum size of the element buffer
   * @throws RemoteException on RMI errors
   */
  public ServerNodeTypeIterator(NodeTypeIterator iterator, RemoteAdapterFactory factory,
      int maxBufferSize) throws RemoteException {
    super(iterator, factory, maxBufferSize);
  }

  /**
   * Creates and returns a remote adapter for the given node type.
   * 
   * @param object local object
   * @return remote adapter
   * @throws RemoteException on RMI errors
   * @see ServerIterator#getRemoteObject(Object)
   */
  protected Object getRemoteObject(Object object) throws RemoteException {
    return getFactory().getRemoteNodeType((NodeType) object);
  }

}

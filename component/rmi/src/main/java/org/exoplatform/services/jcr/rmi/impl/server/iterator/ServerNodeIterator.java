/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server.iterator;

import java.rmi.RemoteException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;

/**
 * A ServerIterator for iterating nodes.
 */
public class ServerNodeIterator extends ServerIterator {

  /**
   * Creates a ServerNodeIterator instance.
   * 
   * @param iterator local node iterator
   * @param factory remote adapter factory
   * @param maxBufferSize maximum size of the element buffer
   * @throws RemoteException on RMI errors
   */
  public ServerNodeIterator(NodeIterator iterator, RemoteAdapterFactory factory, int maxBufferSize)
      throws RemoteException {
    super(iterator, factory, maxBufferSize);
  }

  /**
   * Creates and returns a remote adapter for the given node.
   * 
   * @param object local object
   * @return remote adapter
   * @throws RemoteException on RMI errors
   * @see ServerIterator#getRemoteObject(Object)
   */
  protected Object getRemoteObject(Object object) throws RemoteException {
    return getRemoteNode((Node) object);
  }

}

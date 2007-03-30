/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client.iterator;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * A ClientIterator for iterating remote node types.
 */
public class ClientNodeTypeIterator extends ClientIterator implements NodeTypeIterator {

  /**
   * Creates a ClientNodeTypeIterator instance.
   * 
   * @param iterator remote iterator
   * @param factory local adapter factory
   */
  public ClientNodeTypeIterator(RemoteIterator iterator, LocalAdapterFactory factory) {
    super(iterator, factory);
  }

  /**
   * Creates and returns a local adapter for the given remote node.
   * 
   * @param remote remote referecne
   * @return local adapter
   * @see ClientIterator#getObject(Object)
   */
  protected Object getObject(Object remote) {
    return getFactory().getNodeType((RemoteNodeType) remote);
  }

  /**
   * Returns the next node type in this iteration.
   * 
   * @return next node type
   * @see NodeTypeIterator#nextNodeType()
   */
  public NodeType nextNodeType() {
    return (NodeType) next();
  }

}

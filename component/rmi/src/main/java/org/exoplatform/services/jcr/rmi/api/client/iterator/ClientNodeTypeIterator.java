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

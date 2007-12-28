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
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Remote adapter for the JCR
 * {@link javax.jcr.nodetype.NodeDefinition NodeDefinition} interface. This
 * class makes a local node definition available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeDefinition RemoteNodeDefinition}
 * interface.
 * 
 * @see javax.jcr.nodetype.NodeDefinition
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeDefinition
 */
public class ServerNodeDefinition extends ServerItemDefinition implements RemoteNodeDefinition {

  /**
   * 
   */
  private static final long serialVersionUID = -7478196402820559688L;

  /** The adapted node definition. */
  private NodeDefinition    def;

  /**
   * Creates a remote adapter for the given local node definition.
   * 
   * @param def local node definition
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerNodeDefinition(NodeDefinition def, RemoteAdapterFactory factory)
      throws RemoteException {
    super(def, factory);
    this.def = def;
  }

  /** {@inheritDoc} */
  public RemoteNodeType[] getRequiredPrimaryTypes() throws RemoteException {
    return getRemoteNodeTypeArray(def.getRequiredPrimaryTypes());
  }

  /** {@inheritDoc} */
  public RemoteNodeType getDefaultPrimaryType() throws RemoteException {
    NodeType nt = def.getDefaultPrimaryType();
    if (nt == null) {
      return null;
    } else {
      return getFactory().getRemoteNodeType(def.getDefaultPrimaryType());
    }
  }

  /** {@inheritDoc} */
  public boolean allowsSameNameSiblings() throws RemoteException {
    return def.allowsSameNameSiblings();
  }

}

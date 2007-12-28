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
package org.exoplatform.services.jcr.rmi.api.remote.nodetype;

import java.rmi.RemoteException;

/**
 * Remote version of the JCR
 * {@link javax.jcr.nodetype.NodeDefinition NodeDefinition} interface. Used by
 * the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerNodeDefinition ServerNodeDefinition}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientNodeDefinition ClientNodeDefinition}
 * adapters to provide transparent RMI access to remote node definitions.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding NodeDef method. The remote object will simply forward the
 * method call to the underlying NodeDef instance. Return values and possible
 * exceptions are copied over the network. Complex
 * {@link javax.jcr.nodetype.NodeType NodeType} return values are returned as
 * remote references to the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeType RemoteNodeType}
 * interface. RMI errors are signalled with RemoteExceptions.
 */
public interface RemoteNodeDefinition extends RemoteItemDefinition {

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.NodeDefinition#getRequiredPrimaryTypes() NodeDef.getRequiredPrimaryTypes()}
   * method.
   * 
   * @return required primary node types
   * @throws RemoteException on RMI errors
   */
  RemoteNodeType[] getRequiredPrimaryTypes() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.NodeDefinition#getDefaultPrimaryType() NodeDef.getDefaultPrimaryType()}
   * method.
   * 
   * @return default primary node type
   * @throws RemoteException on RMI errors
   */
  RemoteNodeType getDefaultPrimaryType() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.NodeDefinition#allowsSameNameSiblings() NodeDef.allowSameNameSibs()}
   * method.
   * 
   * @return <code>true</code> if same name siblings are allowed,
   *         <code>false</code> otherwise
   * @throws RemoteException on RMI errors
   */
  boolean allowsSameNameSiblings() throws RemoteException;

}

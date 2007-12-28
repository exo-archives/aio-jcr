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
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeDefinition RemoteNodeDefinition}
 * inteface. This class makes a remote node definition locally available using
 * the JCR {@link javax.jcr.nodetype.NodeDefinition NodeDef} interface.
 * 
 * @see javax.jcr.nodetype.NodeDefinition
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeDefinition
 */
public class ClientNodeDefinition extends ClientItemDefinition implements NodeDefinition {

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    } else if (obj instanceof ClientNodeDefinition) {
      ClientNodeDefinition cnodedef = (ClientNodeDefinition) obj;

      return this.allowsSameNameSiblings()
          && cnodedef.allowsSameNameSiblings()
          && this.getDefaultPrimaryType().equals(cnodedef.getDefaultPrimaryType())
          && equalsNodeTypeArray(this.getRequiredPrimaryTypes(), cnodedef.getRequiredPrimaryTypes());

    }
    return false;
  }

  /** The adapted remote node definition. */
  private RemoteNodeDefinition remote;

  /**
   * Creates a local adapter for the given remote node definition.
   * 
   * @param remote remote node definition
   * @param factory local adapter factory
   */
  public ClientNodeDefinition(RemoteNodeDefinition remote, LocalAdapterFactory factory) {
    super(remote, factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public NodeType[] getRequiredPrimaryTypes() {
    try {
      return getNodeTypeArray(remote.getRequiredPrimaryTypes());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeType getDefaultPrimaryType() {
    try {
      RemoteNodeType nt = remote.getDefaultPrimaryType();
      if (nt == null) {
        return null;
      } else {
        return getFactory().getNodeType(nt);
      }
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean allowsSameNameSiblings() {
    try {
      return remote.allowsSameNameSiblings();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  public static boolean equalsNodeTypeArray(NodeType[] a, NodeType[] b) {
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    if (a.length != b.length) {
      return false;
    } else {
      boolean equal = true;
      for (int i = 0; i < a.length; i++) {
        if (!a[i].equals(b[i])) {
          equal = false;
        }
      }
      return equal;
    }
  }
}

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

import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteItemDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteItemDefinition RemoteItemDefinition}
 * inteface. This class makes a remote item definition locally available using
 * the JCR {@link javax.jcr.nodetype.ItemDefinition ItemDef} interface. Used
 * mainly as the base class for the
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientPropertyDefinition ClientPropertyDefinition}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientNodeDefinition ClientNodeDefinition}
 * adapters.
 * 
 * @see javax.jcr.nodetype.ItemDefinition
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteItemDefinition
 */
public class ClientItemDefinition extends ClientObject implements ItemDefinition {

  /** The adapted remote item definition. */
  private RemoteItemDefinition remote;

  /**
   * Creates a local adapter for the given remote item definition.
   * 
   * @param remote remote item definition
   * @param factory local adapter factory
   */
  public ClientItemDefinition(RemoteItemDefinition remote, LocalAdapterFactory factory) {
    super(factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public NodeType getDeclaringNodeType() {
    try {
      RemoteNodeType nt = remote.getDeclaringNodeType();
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
  public String getName() {
    try {
      return remote.getName();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isAutoCreated() {
    try {
      return remote.isAutoCreated();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isMandatory() {
    try {
      return remote.isMandatory();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public int getOnParentVersion() {
    try {
      return remote.getOnParentVersion();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isProtected() {
    try {
      return remote.isProtected();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

}

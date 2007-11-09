/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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

/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteItemDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Remote adapter for the JCR
 * {@link javax.jcr.nodetype.ItemDefinition ItemDefinition} interface. This
 * class makes a local item definition available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteItemDefinition RemoteItemDefinition}
 * interface. Used mainly as the base class for the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerPropertyDefinition ServerPropertyDefinition}
 * and
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerNodeDefinition ServerNodeDefinition}
 * adapters.
 * 
 * @see javax.jcr.nodetype.ItemDefinition
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteItemDefinition
 */
public class ServerItemDefinition extends ServerObject implements RemoteItemDefinition {

  /**
   * 
   */
  private static final long serialVersionUID = -8671830741893187674L;

  /** The adapted local item definition. */
  private ItemDefinition    def;

  /**
   * Creates a remote adapter for the given local item definition.
   * 
   * @param def local item definition
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerItemDefinition(ItemDefinition def, RemoteAdapterFactory factory)
      throws RemoteException {
    super(factory);
    this.def = def;
  }

  /** {@inheritDoc} */
  public RemoteNodeType getDeclaringNodeType() throws RemoteException {
    NodeType nt = def.getDeclaringNodeType();
    if (nt == null) {
      return null;
    } else {
      return getFactory().getRemoteNodeType(nt);
    }
  }

  /** {@inheritDoc} */
  public String getName() throws RemoteException {
    return def.getName();
  }

  /** {@inheritDoc} */
  public boolean isAutoCreated() throws RemoteException {
    return def.isAutoCreated();
  }

  /** {@inheritDoc} */
  public boolean isMandatory() throws RemoteException {
    return def.isMandatory();
  }

  /** {@inheritDoc} */
  public int getOnParentVersion() throws RemoteException {
    return def.getOnParentVersion();
  }

  /** {@inheritDoc} */
  public boolean isProtected() throws RemoteException {
    return def.isProtected();
  }

}

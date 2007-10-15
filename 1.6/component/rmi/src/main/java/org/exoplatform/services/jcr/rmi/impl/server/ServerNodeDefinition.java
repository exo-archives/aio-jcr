/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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

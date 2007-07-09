/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;
import org.exoplatform.services.jcr.rmi.api.value.SerialValueFactory;

/**
 * Remote adapter for the JCR
 * {@link javax.jcr.nodetype.PropertyDefinition PropertyDefinition} interface.
 * This class makes a local property definition available as an RMI service
 * using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemotePropertyDefinition RemotePropertyDefinition}
 * interface.
 * 
 * @see javax.jcr.nodetype.PropertyDefinition
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemotePropertyDefinition
 */
public class ServerPropertyDefinition extends ServerItemDefinition implements
    RemotePropertyDefinition {

  /**
   * 
   */
  private static final long  serialVersionUID = -3097650792611944422L;

  /** The adapted local property definition. */
  private PropertyDefinition def;

  /**
   * Creates a remote adapter for the given local property definition.
   * 
   * @param def local property definition
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerPropertyDefinition(PropertyDefinition def, RemoteAdapterFactory factory)
      throws RemoteException {
    super(def, factory);
    this.def = def;
  }

  /** {@inheritDoc} */
  public int getRequiredType() throws RemoteException {
    return def.getRequiredType();
  }

  /** {@inheritDoc} */
  public String[] getValueConstraints() throws RemoteException {
    return def.getValueConstraints();
  }

  /** {@inheritDoc} */
  public Value[] getDefaultValues() throws RemoteException {
    return SerialValueFactory.makeSerialValueArray(def.getDefaultValues());
  }

  /** {@inheritDoc} */
  public boolean isMultiple() throws RemoteException {
    return def.isMultiple();
  }

}

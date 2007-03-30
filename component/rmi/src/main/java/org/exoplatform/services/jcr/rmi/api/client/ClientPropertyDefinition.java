/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemotePropertyDefinition RemotePropertyDefinition}
 * inteface. This class makes a remote property definition locally available
 * using the JCR {@link javax.jcr.nodetype.PropertyDefinition PropertyDef}
 * interface.
 * 
 * @see javax.jcr.nodetype.PropertyDefinition
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemotePropertyDefinition
 */
public class ClientPropertyDefinition extends ClientItemDefinition implements PropertyDefinition {

  /** The adapted remote property. */
  private RemotePropertyDefinition remote;

  /**
   * Creates a local adapter for the given remote property definition.
   * 
   * @param remote remote property definition
   * @param factory local adapter factory
   */
  public ClientPropertyDefinition(RemotePropertyDefinition remote, LocalAdapterFactory factory) {
    super(remote, factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public int getRequiredType() {
    try {
      return remote.getRequiredType();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getValueConstraints() {
    try {
      return remote.getValueConstraints();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public Value[] getDefaultValues() {
    try {
      return remote.getDefaultValues();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isMultiple() {
    try {
      return remote.isMultiple();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

}

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

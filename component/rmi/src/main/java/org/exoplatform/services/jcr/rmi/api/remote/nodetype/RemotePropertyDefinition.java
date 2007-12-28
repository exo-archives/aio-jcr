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

import javax.jcr.Value;

/**
 * Remote version of the JCR
 * {@link javax.jcr.nodetype.PropertyDefinition PropertyDefinition} interface.
 * Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerPropertyDefinition ServerPropertyDefinition}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientPropertyDefinition ClientPropertyDefinition}
 * adapters to provide transparent RMI access to remote property definitions.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding PropertyDef method. The remote object will simply forward the
 * method call to the underlying PropertyDef instance. Return values and
 * possible exceptions are copied over the network. RMI errors are signalled
 * with RemoteExceptions.
 * <p>
 * Note that the returned Value objects must be serializable and implemented
 * using classes available on both the client and server side. The
 * {@link org.exoplatform.services.jcr.rmi.api.value.SerialValueFactory SerialValueFactory}
 * class provides two convenience methods to satisfy this requirement.
 */
public interface RemotePropertyDefinition extends RemoteItemDefinition {
  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.PropertyDefinition#getRequiredType() PropertyDefinition.getRequiredType()}
   * method.
   * 
   * @return required type
   * @throws RemoteException on RMI errors
   */
  int getRequiredType() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.PropertyDefinition#getValueConstraints() PropertyDefinition.getValueConstraints()}
   * method.
   * 
   * @return value constraints
   * @throws RemoteException on RMI errors
   */
  String[] getValueConstraints() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.PropertyDefinition#getDefaultValues() PropertyDefinition.getDefaultValues()}
   * method.
   * 
   * @return default values
   * @throws RemoteException on RMI errors
   */
  Value[] getDefaultValues() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.PropertyDefinition#isMultiple() PropertyDefinition.isMultiple()}
   * method.
   * 
   * @return <code>true</code> if the property is multi-valued,
   *         <code>false</code> otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isMultiple() throws RemoteException;

}

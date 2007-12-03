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
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;

/**
 * Remote version of the JCR {@link javax.jcr.Property Property} interface.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding Property method. The remote object will simply forward the
 * method call to the underlying Property instance. Argument and return values,
 * as well as possible exceptions, are copied over the network.
 * <p>
 * Note that only the generic getValue and setValue methods are included in this
 * interface. Clients should implement the type-specific value getters and
 * setters wrapping using the generic methods. Note also that the Value objects
 * must be serializable and implemented using classes available on both the
 * client and server side.
 */
public interface RemoteProperty extends RemoteItem {

  /**
   * Remote version of the
   * {@link javax.jcr.Property#getValue() Property.getValue()} method.
   * 
   * @return property value
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  Value getValue() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Property#getValues() Property.getValues()} method.
   * 
   * @return property values
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  Value[] getValues() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Property#setValue(Value) Property.setValue(Value)} method.
   * 
   * @param value property value
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void setValue(Value value) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Property#setValue(Value[]) Property.setValue(Value[])}
   * method.
   * 
   * @param values property values
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void setValue(Value[] values) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Property#getLength() Property.getLength()} method.
   * 
   * @return value length
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  long getLength() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Property#getLengths() Property.getLengths()} method.
   * 
   * @return value lengths
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  long[] getLengths() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Property#getDefinition() Property.getDefinition()} method.
   * 
   * @return property definition
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemotePropertyDefinition getDefinition() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Property#getType() Property.getType()} method.
   * 
   * @return property type
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  int getType() throws RepositoryException, RemoteException;

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

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

package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemotePropertyDefinition
 * RemotePropertyDefinition} inteface. This class makes a remote property definition locally
 * available using the JCR {@link javax.jcr.nodetype.PropertyDefinition PropertyDef} interface.
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
   * @param remote
   *          remote property definition
   * @param factory
   *          local adapter factory
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

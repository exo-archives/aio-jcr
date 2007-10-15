/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Remote version of the JCR {@link javax.jcr.query.Row Row} interface. Used by
 * the {@link org.exoplatform.services.jcr.rmi.impl.server.ServerRow ServerRow}
 * and {@link org.exoplatform.services.jcr.rmi.api.client.ClientRow ClientRow}
 * adapter base classes to provide transparent RMI access to remote items.
 * <p>
 * RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.query.Row
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientRow
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerRow
 */
public interface RemoteRow extends Remote {

  /**
   * @see javax.jcr.query.Row#getValues()
   * @return row values
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  Value[] getValues() throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.Row#getValue(String)
   * @param propertyName property name
   * @return identified value
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  Value getValue(String propertyName) throws RepositoryException, RemoteException;
}

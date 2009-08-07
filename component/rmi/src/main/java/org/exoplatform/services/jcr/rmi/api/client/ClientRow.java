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

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRow;

/**
 * Local adapter for the JCR-RMI {@link RemoteRow RemoteRow} inteface. This class makes a remote
 * query row locally available using the JCR {@link Row Row} interface.
 * 
 * @see javax.jcr.query.Row Row
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteRow
 */
public class ClientRow implements Row {

  /** The remote query row. */
  private RemoteRow remote;

  /**
   * Creates a client adapter for the given remote query row.
   * 
   * @param remote
   *          remote query row
   */
  public ClientRow(RemoteRow remote) {
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public Value[] getValues() throws RepositoryException {
    try {
      return remote.getValues();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Value getValue(String s) throws RepositoryException {
    try {
      return remote.getValue(s);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }
}

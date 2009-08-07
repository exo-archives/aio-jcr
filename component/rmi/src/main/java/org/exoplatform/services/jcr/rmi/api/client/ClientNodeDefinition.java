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

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeDefinition RemoteNodeDefinition}
 * inteface. This class makes a remote node definition locally available using the JCR
 * {@link javax.jcr.nodetype.NodeDefinition NodeDef} interface.
 * 
 * @see javax.jcr.nodetype.NodeDefinition
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeDefinition
 */
public class ClientNodeDefinition extends ClientItemDefinition implements NodeDefinition {

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    } else if (obj instanceof ClientNodeDefinition) {
      ClientNodeDefinition cnodedef = (ClientNodeDefinition) obj;

      return this.allowsSameNameSiblings()
          && cnodedef.allowsSameNameSiblings()
          && this.getDefaultPrimaryType().equals(cnodedef.getDefaultPrimaryType())
          && equalsNodeTypeArray(this.getRequiredPrimaryTypes(), cnodedef.getRequiredPrimaryTypes());

    }
    return false;
  }

  /** The adapted remote node definition. */
  private RemoteNodeDefinition remote;

  /**
   * Creates a local adapter for the given remote node definition.
   * 
   * @param remote
   *          remote node definition
   * @param factory
   *          local adapter factory
   */
  public ClientNodeDefinition(RemoteNodeDefinition remote, LocalAdapterFactory factory) {
    super(remote, factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public NodeType[] getRequiredPrimaryTypes() {
    try {
      return getNodeTypeArray(remote.getRequiredPrimaryTypes());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeType getDefaultPrimaryType() {
    try {
      RemoteNodeType nt = remote.getDefaultPrimaryType();
      if (nt == null) {
        return null;
      } else {
        return getFactory().getNodeType(nt);
      }
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean allowsSameNameSiblings() {
    try {
      return remote.allowsSameNameSiblings();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  public static boolean equalsNodeTypeArray(NodeType[] a, NodeType[] b) {
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    if (a.length != b.length) {
      return false;
    } else {
      boolean equal = true;
      for (int i = 0; i < a.length; i++) {
        if (!a[i].equals(b[i])) {
          equal = false;
        }
      }
      return equal;
    }
  }
}

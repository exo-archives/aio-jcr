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
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;
import org.exoplatform.services.jcr.rmi.api.value.SerialValueFactory;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeType RemoteNodeType}
 * inteface. This class makes a remote node type locally available using the JCR
 * {@link javax.jcr.nodetype.NodeType NodeType} interface.
 * 
 * @see javax.jcr.nodetype.NodeType
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeType
 */
public class ClientNodeType extends ClientObject implements NodeType {

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    } else if (obj instanceof ClientNodeType) {
      ClientNodeType cnodetype = (ClientNodeType) obj;
      return this.getName().equals(cnodetype.getName());
    }

    return false;
  }

  /** The adapted remote node type. */
  private RemoteNodeType remote;

  /**
   * Creates a local adapter for the given remote node type.
   * 
   * @param remote remote node type
   * @param factory local adapter factory
   */
  public ClientNodeType(RemoteNodeType remote, LocalAdapterFactory factory) {
    super(factory);
    this.remote = remote;
  }

  /**
   * Utility method for creating an array of local node definition adapters for
   * an array of remote node definitions. The node definition adapters are
   * created using the local adapter factory.
   * <p>
   * A <code>null</code> input is treated as an empty array.
   * 
   * @param remotes remote node definitions
   * @return local node definition array
   */
  private NodeDefinition[] getNodeDefArray(RemoteNodeDefinition[] remotes) {
    if (remotes != null) {
      NodeDefinition[] defs = new NodeDefinition[remotes.length];
      for (int i = 0; i < remotes.length; i++) {
        defs[i] = getFactory().getNodeDef(remotes[i]);
      }
      return defs;
    } else {
      return new NodeDefinition[0]; // for safety
    }
  }

  /**
   * Utility method for creating an array of local property definition adapters
   * for an array of remote property definitions. The property definition
   * adapters are created using the local adapter factory.
   * <p>
   * A <code>null</code> input is treated as an empty array.
   * 
   * @param remotes remote property definitions
   * @return local property definition array
   */
  protected PropertyDefinition[] getPropertyDefArray(RemotePropertyDefinition[] remotes) {
    if (remotes != null) {
      PropertyDefinition[] defs = new PropertyDefinition[remotes.length];
      for (int i = 0; i < remotes.length; i++) {
        defs[i] = getFactory().getPropertyDef(remotes[i]);
      }
      return defs;
    } else {
      return new PropertyDefinition[0]; // for safety
    }
  }

  /** {@inheritDoc} */
  public String getName() {
    try {
      return remote.getName();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isMixin() {
    try {
      return remote.isMixin();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean hasOrderableChildNodes() {
    try {
      return remote.hasOrderableChildNodes();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeType[] getSupertypes() {
    try {
      return getNodeTypeArray(remote.getSupertypes());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeType[] getDeclaredSupertypes() {
    try {
      return getNodeTypeArray(remote.getDeclaredSupertypes());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isNodeType(String type) {
    try {
      return remote.isNodeType(type);
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public PropertyDefinition[] getPropertyDefinitions() {
    try {
      return getPropertyDefArray(remote.getPropertyDefs());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public PropertyDefinition[] getDeclaredPropertyDefinitions() {
    try {
      return getPropertyDefArray(remote.getDeclaredPropertyDefs());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeDefinition[] getChildNodeDefinitions() {
    try {
      return getNodeDefArray(remote.getChildNodeDefs());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeDefinition[] getDeclaredChildNodeDefinitions() {
    try {
      return getNodeDefArray(remote.getDeclaredChildNodeDefs());
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean canSetProperty(String name, Value value) {
    try {
      return remote.canSetProperty(name, SerialValueFactory.makeSerialValue(value));
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }

  }

  /** {@inheritDoc} */
  public boolean canSetProperty(String name, Value[] values) {
    try {
      Value[] serials = SerialValueFactory.makeSerialValueArray(values);
      return remote.canSetProperty(name, serials);
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }

  }

  /** {@inheritDoc} */
  public boolean canAddChildNode(String name) {
    try {
      return remote.canAddChildNode(name);
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean canAddChildNode(String name, String type) {
    try {
      return remote.canAddChildNode(name, type);
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean canRemoveItem(String name) {
    try {
      return remote.canRemoveItem(name);
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getPrimaryItemName() {
    try {
      return remote.getPrimaryItemName();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }
}

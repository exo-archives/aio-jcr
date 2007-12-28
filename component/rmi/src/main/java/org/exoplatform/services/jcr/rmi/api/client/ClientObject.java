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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteItem;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Base class for client adapter objects. The only purpose of this class is to
 * centralize the handling of the local adapter factory used by the client
 * adapters to instantiate new adapters.
 */
public class ClientObject {

  /** Local adapter factory. */
  private LocalAdapterFactory factory;

  /**
   * Creates a basic client adapter that uses the given factory to create new
   * adapters.
   * 
   * @param factory local adapter factory
   */
  protected ClientObject(LocalAdapterFactory factory) {
    this.factory = factory;
  }

  /**
   * Returns the local adapter factory used to create new adapters.
   * 
   * @return local adapter factory
   */
  protected LocalAdapterFactory getFactory() {
    return factory;
  }

  /**
   * Utility method to create a local adapter for a remote item. This method
   * introspects the remote reference to determine whether to instantiate a
   * {@link javax.jcr.Property}, a {@link Node Node}, or an {@link Item Item}
   * adapter using the local adapter factory.
   * <p>
   * If the remote item is a {@link RemoteNode}, this method delegates to
   * {@link #getNode(Session, RemoteNode)}.
   * 
   * @param session current session
   * @param remote remote item
   * @return local property, node, or item adapter
   */
  protected Item getItem(Session session, RemoteItem remote) {
    if (remote instanceof RemoteProperty) {
      return factory.getProperty(session, (RemoteProperty) remote);
    } else if (remote instanceof RemoteNode) {
      return getNode(session, (RemoteNode) remote);
    } else {
      return factory.getItem(session, remote);
    }
  }

  /**
   * Utility method to create a local adapter for a remote node. This method
   * introspects the remote reference to determine whether to instantiate a
   * {@link Node Node}, a
   * {@link javax.jcr.version.VersionHistory VersionHistory}, or a
   * {@link Version Version} adapter using the local adapter factory.
   * 
   * @param session current session
   * @param remote remote node
   * @return local node, version, or version history adapter
   */
  protected Node getNode(Session session, RemoteNode remote) {
    if (remote instanceof RemoteVersion) {
      return factory.getVersion(session, (RemoteVersion) remote);
    } else if (remote instanceof RemoteVersionHistory) {
      return factory.getVersionHistory(session, (RemoteVersionHistory) remote);
    } else {
      return factory.getNode(session, remote);
    }
  }

  /**
   * Utility method for creating an array of local node type adapters for an
   * array of remote node types. The node type adapters are created using the
   * local adapter factory.
   * <p>
   * A <code>null</code> input is treated as an empty array.
   * 
   * @param remotes remote node types
   * @return local node type array
   */
  protected NodeType[] getNodeTypeArray(RemoteNodeType[] remotes) {
    if (remotes != null) {
      NodeType[] types = new NodeType[remotes.length];
      for (int i = 0; i < remotes.length; i++) {
        types[i] = factory.getNodeType(remotes[i]);
      }
      return types;
    } else {
      return new NodeType[0]; // for safety
    }
  }

}

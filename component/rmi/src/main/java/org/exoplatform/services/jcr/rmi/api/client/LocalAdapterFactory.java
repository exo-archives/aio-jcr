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
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteItem;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteLock;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteObservationManager;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryResult;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRow;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteSession;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersionHistory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteWorkspace;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteItemDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeTypeManager;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;

/**
 * Factory interface for creating local adapters for remote references. This
 * interface defines how remote JCR-RMI references are adapted back to the
 * normal JCR interfaces. The adaption mechanism can be modified (for example to
 * add extra features) by changing the local adapter factory used by the
 * repository client.
 * <p>
 * Note that the
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientObject ClientObject}
 * base class provides a number of utility methods designed to work with a local
 * adapter factory. Adapter implementations may want to inherit that
 * functionality by subclassing from ClientObject.
 * 
 * @see org.exoplatform.services.jcr.rmi.impl.server.RemoteAdapterFactory
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientAdapterFactory
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientObject
 */
public interface LocalAdapterFactory {

  /**
   * Factory method for creating a local adapter for a remote repository.
   * 
   * @param remote remote repository
   * @return local repository adapter
   */
  Repository getRepository(RemoteRepository remote);

  /**
   * Factory method for creating a local adapter for a remote session.
   * 
   * @param repository current repository
   * @param remote remote session
   * @return local session adapter
   */
  Session getSession(Repository repository, RemoteSession remote);

  /**
   * Factory method for creating a local adapter for a remote workspace.
   * 
   * @param session current session
   * @param remote remote workspace
   * @return local workspace adapter
   */
  Workspace getWorkspace(Session session, RemoteWorkspace remote);

  /**
   * Factory method for creating a local adapter for a remote observation
   * manager.
   * 
   * @param workspace current workspace
   * @param remote remote observation manager
   * @return local observation manager adapter
   */
  ObservationManager getObservationManager(Workspace workspace, RemoteObservationManager remote);

  /**
   * Factory method for creating a local adapter for a remote namespace
   * registry.
   * 
   * @param remote remote namespace registry
   * @return local namespace registry adapter
   */
  NamespaceRegistry getNamespaceRegistry(RemoteNamespaceRegistry remote);

  /**
   * Factory method for creating a local adapter for a remote node type manager.
   * 
   * @param remote remote node type manager
   * @return local node type manager adapter
   */
  NodeTypeManager getNodeTypeManager(RemoteNodeTypeManager remote);

  /**
   * Factory method for creating a local adapter for a remote item. Note that
   * before calling this method, the client may want to introspect the remote
   * item reference to determine whether to use the
   * {@link #getNode(Session, RemoteNode) getNode} or
   * {@link #getProperty(Session, RemoteProperty) getProperty} method instead,
   * as the adapter returned by this method will only cover the basic
   * {@link Item Item} interface.
   * 
   * @param session current session
   * @param remote remote item
   * @return local item adapter
   */
  Item getItem(Session session, RemoteItem remote);

  /**
   * Factory method for creating a local adapter for a remote property.
   * 
   * @param session current session
   * @param remote remote property
   * @return local property adapter
   */
  Property getProperty(Session session, RemoteProperty remote);

  /**
   * Factory method for creating a local adapter for a remote node.
   * 
   * @param session current session
   * @param remote remote node
   * @return local node adapter
   */
  Node getNode(Session session, RemoteNode remote);

  /**
   * Factory method for creating a local adapter for a remote version.
   * 
   * @param session current session
   * @param remote remote version
   * @return local version adapter
   */
  Version getVersion(Session session, RemoteVersion remote);

  /**
   * Factory method for creating a local adapter for a remote version history.
   * 
   * @param session current session
   * @param remote remote version history
   * @return local version history adapter
   */
  VersionHistory getVersionHistory(Session session, RemoteVersionHistory remote);

  /**
   * Factory method for creating a local adapter for a remote node type.
   * 
   * @param remote remote node type
   * @return local node type adapter
   */
  NodeType getNodeType(RemoteNodeType remote);

  /**
   * Factory method for creating a local adapter for a remote item definition.
   * Note that before calling this method, the client may want to introspect the
   * remote item definition to determine whether to use the
   * {@link #getNodeDef(RemoteNodeDefinition) getNodeDef} or
   * {@link #getPropertyDef(RemotePropertyDefinition) getPropertyDef} method
   * instead, as the adapter returned by this method will only cover the
   * {@link ItemDefinition ItemDef} base interface.
   * 
   * @param remote remote item definition
   * @return local item definition adapter
   */
  ItemDefinition getItemDef(RemoteItemDefinition remote);

  /**
   * Factory method for creating a local adapter for a remote node definition.
   * 
   * @param remote remote node definition
   * @return local node definition adapter
   */
  NodeDefinition getNodeDef(RemoteNodeDefinition remote);

  /**
   * Factory method for creating a local adapter for a remote property
   * definition.
   * 
   * @param remote remote property definition
   * @return local property definition adapter
   */
  PropertyDefinition getPropertyDef(RemotePropertyDefinition remote);

  /**
   * Factory method for creating a local adapter for a remote lock.
   * 
   * @param node current node
   * @param remote remote lock
   * @return local lock adapter
   */
  Lock getLock(Node node, RemoteLock remote);

  /**
   * Factory method for creating a local adapter for a remote query manager.
   * 
   * @param session current session
   * @param remote remote query manager
   * @return local query manager adapter
   */
  QueryManager getQueryManager(Session session, RemoteQueryManager remote);

  /**
   * Factory method for creating a local adapter for a remote query.
   * 
   * @param session current session
   * @param remote remote query
   * @return local query adapter
   */
  Query getQuery(Session session, RemoteQuery remote);

  /**
   * Factory method for creating a local adapter for a remote query result.
   * 
   * @param session current session
   * @param remote remote query result
   * @return local query result adapter
   */
  QueryResult getQueryResult(Session session, RemoteQueryResult remote);

  /**
   * Factory method for creating a local adapter for a remote query row.
   * 
   * @param remote remote query row
   * @return local query row adapter
   */
  Row getRow(RemoteRow remote);

  /**
   * Factory method for creating a local adapter for a remote node iterator.
   * 
   * @param session current session
   * @param remote remote node iterator
   * @return local node iterator adapter
   */
  NodeIterator getNodeIterator(Session session, RemoteIterator remote);

  /**
   * Factory method for creating a local adapter for a remote property iterator.
   * 
   * @param session current session
   * @param remote remote property iterator
   * @return local property iterator adapter
   */
  PropertyIterator getPropertyIterator(Session session, RemoteIterator remote);

  /**
   * Factory method for creating a local adapter for a remote version iterator.
   * 
   * @param session current session
   * @param remote remote version iterator
   * @return local version iterator adapter
   */
  VersionIterator getVersionIterator(Session session, RemoteIterator remote);

  /**
   * Factory method for creating a local adapter for a remote node type
   * iterator.
   * 
   * @param remote remote node type iterator
   * @return local node type iterator adapter
   */
  NodeTypeIterator getNodeTypeIterator(RemoteIterator remote);

  /**
   * Factory method for creating a local adapter for a remote row iterator.
   * 
   * @param remote remote row iterator
   * @return local row iterator adapter
   */
  RowIterator getRowIterator(RemoteIterator remote);
}

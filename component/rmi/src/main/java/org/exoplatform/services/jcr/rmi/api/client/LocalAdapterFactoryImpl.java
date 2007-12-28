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

import org.exoplatform.services.jcr.rmi.api.client.iterator.ClientNodeIterator;
import org.exoplatform.services.jcr.rmi.api.client.iterator.ClientNodeTypeIterator;
import org.exoplatform.services.jcr.rmi.api.client.iterator.ClientPropertyIterator;
import org.exoplatform.services.jcr.rmi.api.client.iterator.ClientRowIterator;
import org.exoplatform.services.jcr.rmi.api.client.iterator.ClientVersionIterator;
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
 * Default implementation of the
 * {@link org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory LocalAdapterFactory}
 * interface. This factory uses the client adapters defined in this package as
 * the default adapter implementations. Subclasses can easily override or extend
 * the default adapters by implementing the corresponding factory methods.
 */
public class LocalAdapterFactoryImpl implements LocalAdapterFactory {

  /**
   * Creates and returns a {@link ClientRepository ClientRepository} instance.
   * {@inheritDoc}
   */
  public Repository getRepository(RemoteRepository remote) {
    return new ClientRepository(remote, this);
  }

  /**
   * Creates and returns a {@link ClientSession ClientSession} instance.
   * {@inheritDoc}
   */
  public Session getSession(Repository repository, RemoteSession remote) {
    return new ClientSession(repository, remote, this);
  }

  /**
   * Creates and returns a {@link ClientWorkspace ClientWorkspace} instance.
   * {@inheritDoc}
   */
  public Workspace getWorkspace(Session session, RemoteWorkspace remote) {
    return new ClientWorkspace(session, remote, this);
  }

  /**
   * Creates and returns a
   * {@link ClientObservationManager ClientObservationManager} instance.
   * {@inheritDoc}
   */
  public ObservationManager getObservationManager(Workspace workspace,
      RemoteObservationManager remote) {
    return new ClientObservationManager(workspace, remote);
  }

  /**
   * Creates and returns a
   * {@link ClientNamespaceRegistry ClientClientNamespaceRegistry} instance.
   * {@inheritDoc}
   */
  public NamespaceRegistry getNamespaceRegistry(RemoteNamespaceRegistry remote) {
    return new ClientNamespaceRegistry(remote, this);
  }

  /**
   * Creates and returns a {@link ClientNodeTypeManager ClienNodeTypeManager}
   * instance. {@inheritDoc}
   */
  public NodeTypeManager getNodeTypeManager(RemoteNodeTypeManager remote) {
    return new ClientNodeTypeManager(remote, this);
  }

  /**
   * Creates and returns a {@link ClientItem ClientItem} instance. {@inheritDoc}
   */
  public Item getItem(Session session, RemoteItem remote) {
    return new ClientItem(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientProperty ClientProperty} instance.
   * {@inheritDoc}
   */
  public Property getProperty(Session session, RemoteProperty remote) {
    return new ClientProperty(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientNode ClientNode} instance. {@inheritDoc}
   */
  public Node getNode(Session session, RemoteNode remote) {
    return new ClientNode(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientVersion ClientVersion} instance.
   * {@inheritDoc}
   */
  public Version getVersion(Session session, RemoteVersion remote) {
    return new ClientVersion(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientVersionHistory ClientVersionHistory}
   * instance. {@inheritDoc}
   */
  public VersionHistory getVersionHistory(Session session, RemoteVersionHistory remote) {
    return new ClientVersionHistory(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientNodeType ClientNodeType} instance.
   * {@inheritDoc}
   */
  public NodeType getNodeType(RemoteNodeType remote) {
    return new ClientNodeType(remote, this);
  }

  /**
   * Creates and returns a {@link ClientItemDefinition ClientItemDefinition}
   * instance. {@inheritDoc}
   */
  public ItemDefinition getItemDef(RemoteItemDefinition remote) {
    return new ClientItemDefinition(remote, this);
  }

  /**
   * Creates and returns a {@link ClientNodeDefinition ClientNodeDefinition}
   * instance. {@inheritDoc}
   */
  public NodeDefinition getNodeDef(RemoteNodeDefinition remote) {
    return new ClientNodeDefinition(remote, this);
  }

  /**
   * Creates and returns a
   * {@link ClientPropertyDefinition ClientPropertyDefinition} instance.
   * {@inheritDoc}
   */
  public PropertyDefinition getPropertyDef(RemotePropertyDefinition remote) {
    return new ClientPropertyDefinition(remote, this);
  }

  /**
   * Creates and returns a {@link ClientLock ClientLock} instance. {@inheritDoc}
   */
  public Lock getLock(Node node, RemoteLock remote) {
    return new ClientLock(node, remote, this);
  }

  /**
   * Creates and returns a {@link ClientQueryManager ClientQueryManager}
   * instance. {@inheritDoc}
   */
  public QueryManager getQueryManager(Session session, RemoteQueryManager remote) {
    return new ClientQueryManager(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientQuery ClientQuery} instance.
   * {@inheritDoc}
   */
  public Query getQuery(Session session, RemoteQuery remote) {
    return new ClientQuery(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientQueryResult ClientQueryResult} instance.
   * {@inheritDoc}
   */
  public QueryResult getQueryResult(Session session, RemoteQueryResult remote) {
    return new ClientQueryResult(session, remote, this);
  }

  /**
   * Creates and returns a {@link ClientRow ClientRow} instance. {@inheritDoc}
   */
  public Row getRow(RemoteRow remote) {
    return new ClientRow(remote);
  }

  /**
   * Creates and returns a {@link ClientNodeIterator} instance. {@inheritDoc}
   */
  public NodeIterator getNodeIterator(Session session, RemoteIterator remote) {
    return new ClientNodeIterator(remote, session, this);
  }

  /**
   * Creates and returns a {@link ClientPropertyIterator} instance.
   * {@inheritDoc}
   */
  public PropertyIterator getPropertyIterator(Session session, RemoteIterator remote) {
    return new ClientPropertyIterator(remote, session, this);
  }

  /**
   * Creates and returns a {@link ClientVersionIterator} instance. {@inheritDoc}
   */
  public VersionIterator getVersionIterator(Session session, RemoteIterator remote) {
    return new ClientVersionIterator(remote, session, this);
  }

  /**
   * Creates and returns a {@link ClientNodeTypeIterator} instance.
   * {@inheritDoc}
   */
  public NodeTypeIterator getNodeTypeIterator(RemoteIterator remote) {
    return new ClientNodeTypeIterator(remote, this);
  }

  /**
   * Creates and returns a {@link ClientRowIterator} instance. {@inheritDoc}
   */
  public RowIterator getRowIterator(RemoteIterator remote) {
    return new ClientRowIterator(remote, this);
  }

}

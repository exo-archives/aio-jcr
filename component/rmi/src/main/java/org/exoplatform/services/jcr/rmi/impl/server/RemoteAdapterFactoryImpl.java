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
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteEventCollection;
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
import org.exoplatform.services.jcr.rmi.api.remote.iterator.ArrayIterator;
import org.exoplatform.services.jcr.rmi.api.remote.iterator.BufferIterator;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteItemDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeDefinition;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeTypeManager;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;
import org.exoplatform.services.jcr.rmi.impl.server.iterator.ServerNodeIterator;
import org.exoplatform.services.jcr.rmi.impl.server.iterator.ServerNodeTypeIterator;
import org.exoplatform.services.jcr.rmi.impl.server.iterator.ServerPropertyIterator;
import org.exoplatform.services.jcr.rmi.impl.server.iterator.ServerRowIterator;
import org.exoplatform.services.jcr.rmi.impl.server.iterator.ServerVersionIterator;

/**
 * Default implementation of the
 * {@link RemoteAdapterFactory RemoteAdapterFactory} interface. This factory
 * uses the server adapters defined in this package as the default adapter
 * implementations. Subclasses can override or extend the default adapters by
 * implementing the corresponding factory methods.
 * <p>
 * The <code>bufferSize</code> property can be used to configure the size of
 * the buffer used by iterators to speed up iterator traversal over the network.
 */
public class RemoteAdapterFactoryImpl implements RemoteAdapterFactory {

  /** The default iterator buffer size. */
  private static final int DEFAULT_BUFFER_SIZE = 100;

  /** The buffer size of iterators created by this factory. */
  private int              bufferSize;

  /**
   * Creates a server adapter factory with the default iterator buffer size.
   */
  public RemoteAdapterFactoryImpl() {
    bufferSize = DEFAULT_BUFFER_SIZE;
  }

  /**
   * Returns the iterator buffer size.
   * 
   * @return iterator buffer size
   */
  public int getBufferSize() {
    return bufferSize;
  }

  /**
   * Sets the iterator buffer size.
   * 
   * @param bufferSize iterator buffer size
   */
  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * Creates a {@link ServerRepository ServerRepository} instance. {@inheritDoc}
   */
  public RemoteRepository getRemoteRepository(Repository repository) throws RemoteException {
    return new ServerRepository(repository, this);
  }

  /**
   * Creates a {@link ServerSession ServerSession} instance. {@inheritDoc}
   */
  public RemoteSession getRemoteSession(Session session) throws RemoteException {
    return new ServerSession(session, this);
  }

  /**
   * Creates a {@link ServerWorkspace ServerWorkspace} instance. {@inheritDoc}
   */
  public RemoteWorkspace getRemoteWorkspace(Workspace workspace) throws RemoteException {
    return new ServerWorkspace(workspace, this);
  }

  /**
   * Creates a {@link ServerObservationManager ServerObservationManager}
   * instance. {@inheritDoc}
   */
  public RemoteObservationManager getRemoteObservationManager(ObservationManager observationManager)
      throws RemoteException {
    return new ServerObservationManager(observationManager, this);
  }

  /**
   * Creates a {@link ServerNamespaceRegistry ServerNamespaceRegistry} instance.
   * {@inheritDoc}
   */
  public RemoteNamespaceRegistry getRemoteNamespaceRegistry(NamespaceRegistry registry)
      throws RemoteException {
    return new ServerNamespaceRegistry(registry, this);
  }

  /**
   * Creates a {@link ServerNodeTypeManager ServerNodeTypeManager} instance.
   * {@inheritDoc}
   */
  public RemoteNodeTypeManager getRemoteNodeTypeManager(NodeTypeManager manager)
      throws RemoteException {
    return new ServerNodeTypeManager(manager, this);
  }

  /**
   * Creates a {@link ServerItem ServerItem} instance. {@inheritDoc}
   */
  public RemoteItem getRemoteItem(Item item) throws RemoteException {
    return new ServerItem(item, this);
  }

  /**
   * Creates a {@link ServerProperty ServerProperty} instance. {@inheritDoc}
   */
  public RemoteProperty getRemoteProperty(Property property) throws RemoteException {
    return new ServerProperty(property, this);
  }

  /**
   * Creates a {@link ServerNode ServerNode} instance. {@inheritDoc}
   */
  public RemoteNode getRemoteNode(Node node) throws RemoteException {
    return new ServerNode(node, this);
  }

  /**
   * Creates a {@link ServerVersion ServerVersion} instance. {@inheritDoc}
   */
  public RemoteVersion getRemoteVersion(Version version) throws RemoteException {
    return new ServerVersion(version, this);
  }

  /**
   * Creates a {@link ServerVersionHistory ServerVersionHistory} instance.
   * {@inheritDoc}
   */
  public RemoteVersionHistory getRemoteVersionHistory(VersionHistory versionHistory)
      throws RemoteException {
    return new ServerVersionHistory(versionHistory, this);
  }

  /**
   * Creates a {@link ServerNodeType ServerNodeType} instance. {@inheritDoc}
   */
  public RemoteNodeType getRemoteNodeType(NodeType type) throws RemoteException {
    return new ServerNodeType(type, this);
  }

  /**
   * Creates a {@link ServerItemDefinition ServerItemDefinition} instance.
   * {@inheritDoc}
   */
  public RemoteItemDefinition getRemoteItemDefinition(ItemDefinition def) throws RemoteException {
    return new ServerItemDefinition(def, this);
  }

  /**
   * Creates a {@link ServerNodeDefinition ServerNodeDefinition} instance.
   * {@inheritDoc}
   */
  public RemoteNodeDefinition getRemoteNodeDefinition(NodeDefinition def) throws RemoteException {
    return new ServerNodeDefinition(def, this);
  }

  /**
   * Creates a {@link ServerPropertyDefinition ServerPropertyDefinition}
   * instance. {@inheritDoc}
   */
  public RemotePropertyDefinition getRemotePropertyDefinition(PropertyDefinition def)
      throws RemoteException {
    return new ServerPropertyDefinition(def, this);
  }

  /**
   * Creates a {@link ServerLock ServerLock} instance. {@inheritDoc}
   */
  public RemoteLock getRemoteLock(Lock lock) throws RemoteException {
    return new ServerLock(lock, this);
  }

  /**
   * Creates a {@link ServerQueryManager ServerQueryManager} instance.
   * {@inheritDoc}
   */
  public RemoteQueryManager getRemoteQueryManager(QueryManager manager, Session session)
      throws RemoteException {
    return new ServerQueryManager(manager, this, session);
  }

  /**
   * Creates a {@link ServerQuery ServerQuery} instance. {@inheritDoc}
   */
  public RemoteQuery getRemoteQuery(Query query) throws RemoteException {
    return new ServerQuery(query, this);
  }

  /**
   * Creates a {@link ServerQueryResult ServerQueryResult} instance.
   * {@inheritDoc}
   */
  public RemoteQueryResult getRemoteQueryResult(QueryResult result) throws RemoteException {
    return new ServerQueryResult(result, this);
  }

  /**
   * Creates a {@link ServerQueryResult ServerQueryResult} instance.
   * {@inheritDoc}
   */
  public RemoteRow getRemoteRow(Row row) throws RemoteException {
    return new ServerRow(row, this);
  }

  /**
   * Creates a {@link ServerEventCollection ServerEventCollection} instances.
   * {@inheritDoc}
   */
  public RemoteEventCollection getRemoteEvent(long listenerId, EventIterator events)
      throws RemoteException {
    RemoteEventCollection.RemoteEvent[] remoteEvents;
    if (events != null) {
      List eventList = new ArrayList();
      while (events.hasNext()) {
        try {
          Event event = events.nextEvent();
          eventList.add(new ServerEventCollection.ServerEvent(event.getType(), event.getPath(),
              event.getUserID()));
        } catch (RepositoryException re) {
          throw new RemoteException(re.getMessage(), re);
        }
      }
      remoteEvents = (RemoteEventCollection.RemoteEvent[]) eventList
          .toArray(new RemoteEventCollection.RemoteEvent[eventList.size()]);
    } else {
      remoteEvents = new RemoteEventCollection.RemoteEvent[0]; // for safety
    }

    return new ServerEventCollection(listenerId, remoteEvents);
  }

  /**
   * Optimizes the given remote iterator for transmission across the network.
   * This method retrieves the first set of elements from the iterator by
   * calling {@link RemoteIterator#nextObjects()} and then asks for the total
   * size of the iterator. If the size is unkown or greater than the length of
   * the retrieved array, then the elements, the size, and the remote iterator
   * reference are wrapped into a {@link BufferIterator} instance that gets
   * passed over the network. If the retrieved array of elements contains all
   * the elements in the iterator, then the iterator instance is discarded and
   * just the elements are wrapped into a {@link ArrayIterator} instance to be
   * passed to the client.
   * <p>
   * Subclasses can override this method to provide alternative optimizations.
   * 
   * @param remote remote iterator
   * @return optimized remote iterator
   * @throws RemoteException on RMI errors
   */
  protected RemoteIterator optimizeIterator(RemoteIterator remote) throws RemoteException {
    Object[] elements = remote.nextObjects();
    long size = remote.getSize();
    if (size == -1 || (elements != null && size > elements.length)) {
      return new BufferIterator(elements, size, remote);
    } else {
      return new ArrayIterator(elements);
    }
  }

  /**
   * Creates a {@link ServerNodeIterator} instance. {@inheritDoc}
   */
  public RemoteIterator getRemoteNodeIterator(NodeIterator iterator) throws RemoteException {
    return optimizeIterator(new ServerNodeIterator(iterator, this, bufferSize));
  }

  /**
   * Creates a {@link ServerPropertyIterator} instance. {@inheritDoc}
   */
  public RemoteIterator getRemotePropertyIterator(PropertyIterator iterator) throws RemoteException {
    return optimizeIterator(new ServerPropertyIterator(iterator, this, bufferSize));
  }

  /**
   * Creates a {@link ServerVersionIterator} instance. {@inheritDoc}
   */
  public RemoteIterator getRemoteVersionIterator(VersionIterator iterator) throws RemoteException {
    return optimizeIterator(new ServerVersionIterator(iterator, this, bufferSize));
  }

  /**
   * Creates a {@link ServerNodeTypeIterator} instance. {@inheritDoc}
   */
  public RemoteIterator getRemoteNodeTypeIterator(NodeTypeIterator iterator) throws RemoteException {
    return optimizeIterator(new ServerNodeTypeIterator(iterator, this, bufferSize));
  }

  /**
   * Creates a {@link ServerRowIterator} instance. {@inheritDoc}
   */
  public RemoteIterator getRemoteRowIterator(RowIterator iterator) throws RemoteException {
    return optimizeIterator(new ServerRowIterator(iterator, this, bufferSize));
  }

}

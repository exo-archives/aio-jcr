/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.services.jcr.impl.core.query;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;



/**
 * Acts as a global entry point to execute queries and index nodes.
 */
public class SearchManager implements  Startable {

  /**
   * Logger instance for this class
   */
  private static Log log = ExoLogger.getLogger("jcr.SearchManager");

  /**
   * Namespace URI for xpath functions
   */
  // @todo this is not final! What should we use?

  /**
   * The time when the query handler was last accessed.
   */
  private long lastAccess = System.currentTimeMillis();

  /**
   * Fully qualified name of the query implementation class.
   * This class must extend {@link org.apache.jackrabbit.core.query.AbstractQueryImpl}!
   */
  private final String queryImplClassName = "org.exoplatform.services.jcr.impl.core.query.QueryImpl";

  /**
   * Namespace mappings to internal prefixes
   */
  private LocationFactory sysLocationFactory;

  /**
   * QueryHandler where query execution is delegated to
   */
  private QueryHandler handler;

  //private WorkspaceEntry wsInfo;

  private final WorkspacePersistentDataManager wsDataManager;

  /**
   * Weakly references all {@link javax.jcr.query.Query} instances created
   * by this <code>SearchManager</code>.
   * If this map is empty and this search manager had been idle for at least
   * {@link #idleTime} seconds, then the query handler is shut down.
   */
   private final Map activeQueries = Collections.synchronizedMap(new WeakHashMap() {

   });

  /**
   * Creates a new <code>SearchManager</code>.
   * @param config the search configuration.
   * @param ntReg the node type registry.
   * @param itemMgr the shared item state manager.
   * @throws RepositoryException
   */
  public SearchManager(WorkspacePersistentDataManager wsDataManager,
                       NamespaceRegistry nsReg,
                       QueryHandler handler,
                       LocationFactory sysLocationFactory) throws RepositoryException {

    this.wsDataManager = wsDataManager;
    this.sysLocationFactory = sysLocationFactory;
    this.handler = handler;
  }

  /**
   * Closes this <code>SearchManager</code> and also closes the
   * configured in {@link SearchConfig}.
   */
  public void close() {
    try {
      handler.close();
    }
    catch (IOException e) {
      log.error("Exception closing QueryHandler.", e);
    }
  }

  /**
   * Creates a query object that can be executed on the workspace.
   *
   * @param session   the session of the user executing the query.
   * @param itemMgr   the item manager of the user executing the query. Needed
   *                  to return <code>Node</code> instances in the result set.
   * @param statement the actual query statement.
   * @param language  the syntax of the query statement.
   * @return a <code>Query</code> instance to execute.
   * @throws InvalidQueryException if the query is malformed or the
   *                               <code>language</code> is unknown.
   * @throws RepositoryException   if any other error occurs.
   */
  public Query createQuery(SessionImpl session,
                           String statement,
                           String language)
                    throws InvalidQueryException, RepositoryException {
    AbstractQueryImpl query = createQueryInstance();
    query.init(session, handler, statement, language);
    return query;
  }

  /**
   * Creates a query object from a node that can be executed on the workspace.
   *
   * @param session the session of the user executing the query.
   * @param itemMgr the item manager of the user executing the query. Needed
   *                to return <code>Node</code> instances in the result set.
   * @param node a node of type nt:query.
   * @return a <code>Query</code> instance to execute.
   * @throws InvalidQueryException if <code>absPath</code> is not a valid
   *                               persisted query (that is, a node of type nt:query)
   * @throws RepositoryException   if any other error occurs.
   */
  public Query createQuery(SessionImpl session,
                           Node node)
                    throws InvalidQueryException, RepositoryException {
    ensureInitialized();
    AbstractQueryImpl query = createQueryInstance();
    query.init(session, handler, node);
    return query;
  }

  ////// ------------------------- Startable -------------------------

  public void start() {
    //    wsDataManager.addItemPersistenceListener(this);
  }

  public void stop() {
    close();
  }

  //--------------- ItemPersistenceListener ----------------------------

//  public void onSaveItems(ItemDataChangesLog changesLog) {
//    // nodes that need to be removed from the index.
//    Set removedNodes = new HashSet();
//    // nodes that need to be added to the index.
//    Set addedNodes = new HashSet();
//
//    List <ItemState> itemStates = changesLog.getAllStates();
//    Iterator items = itemStates.iterator();
//    while (items.hasNext()) {
//      ItemState itemState = (ItemState) items.next();
//
//      //if(itemImpl.getPath().startsWith("/jcr:system"))
//      //  continue;
//
//      if (itemState.isNode()) {
//        if (itemState.getState() == ItemState.ADDED
//            || itemState.getState() == ItemState.AUTO_ADDED) {
//          addedNodes.add(itemState.getData());
//          //traverseAddedNode((NodeData)itemState.getData(), addedNodes);
//        } else if (itemState.getState() == ItemState.DELETED
//            || itemState.getState() == ItemState.AUTO_DELETED) {
//          removedNodes.add(itemState.getData().getUUID());
//        }
//      } else {
//        String parentUUID = itemState.getData().getParentUUID();
//
//        if (!hasUUID(itemStates, parentUUID)) {
//          removedNodes.add(parentUUID);
//          try {
//            addedNodes.add(wsDataManager.getItemData(parentUUID));
//          } catch (RepositoryException e) {
//            log.error("Error indexing node (addNode: " + parentUUID + ").", e);
//          }
//        }
//      }
//    }
//    try {
//      handler.updateNodes(removedNodes.iterator(), addedNodes.iterator());
//    } catch (RepositoryException e) {
//      log.error("Error indexing node.", e);
//    } catch (IOException e) {
//      log.error("Error indexing node.", e);
//    } catch (Throwable e) {
//      e.printStackTrace();
//      log.error("Error indexing node.", e);
//    }
//  }
//
//  private boolean hasUUID(List itemStates, String uuid) {
//    for(int i=0; i<itemStates.size(); i++) {
//      if(((ItemState)itemStates.get(i)).getData().getUUID().equals(uuid))
//        return true;
//    }
//    return false;
//  }

   /**
     * Creates a new instance of an {@link AbstractQueryImpl} which is not
     * initialized.
     *
     * @return an new query instance.
     * @throws RepositoryException if an error occurs while creating a new query
     *                             instance.
     */
    protected AbstractQueryImpl createQueryInstance() throws RepositoryException {
        try {
            Object obj = Class.forName(queryImplClassName).newInstance();
            if (obj instanceof AbstractQueryImpl) {
                // track query instances
                activeQueries.put(obj, null);
                return (AbstractQueryImpl) obj;
            } else {
                throw new IllegalArgumentException(queryImplClassName
                        + " is not of type " + AbstractQueryImpl.class.getName());
            }
        } catch (Throwable t) {
            throw new RepositoryException("Unable to create query: " + t.toString());
        }
    }

    //------------------------< internal >--------------------------------------

    /**
     * Initializes the query handler.
     *
     * @throws RepositoryException if the query handler cannot be initialized.
     */
    private void initializeQueryHandler() throws RepositoryException {
        // initialize query handler
        try {
            //handler = (QueryHandler) config.newInstance();

//            QueryHandlerContext context
//                    = new QueryHandlerContext(wsDataManager, sysLocationFactory);
            handler.init();
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    /**
     * Shuts down the query handler. If the query handler is already shut down
     * this method does nothing.
     *
     * @throws IOException if an error occurs while shutting down the query
     *                     handler.
     */
    private synchronized void shutdownQueryHandler() throws IOException {
        if (handler != null) {
            handler.close();
            handler = null;
        }
    }

    /**
     * Ensures that the query handler is initialized and updates the last
     * access to the current time.
     *
     * @throws RepositoryException if the query handler cannot be initialized.
     */
    private synchronized void ensureInitialized() throws RepositoryException {
        lastAccess = System.currentTimeMillis();
        if (handler == null) {
            initializeQueryHandler();
        }
    }
}
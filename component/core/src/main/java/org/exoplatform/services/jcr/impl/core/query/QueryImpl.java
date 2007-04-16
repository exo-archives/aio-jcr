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

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Implements the {@link Query} interface.
 */
public class QueryImpl extends AbstractQueryImpl {

    /**
     * The session of the user executing this query
     */
    private SessionImpl session;

    /**
     * The query statement
     */
    private String statement;

    /**
     * The syntax of the query statement
     */
    private String language;

    /**
     * The actual query implementation that can be executed
     */
    private ExecutableQuery query;

    /**
     * The node where this query is persisted. Only set when this is a persisted
     * query.
     */
    private Node node;

    /**
     * The query handler for this query.
     */
    private QueryHandler handler;

    /**
     * Flag indicating whether this query is initialized.
     */
    private boolean initialized = false;


    /**
     * @inheritDoc
     */
    public void init(SessionImpl session,
                     QueryHandler handler,
                     String statement,
                     String language) throws InvalidQueryException {

        checkNotInitialized();
        this.session = session;
        this.statement = statement;
        this.language = language;
        this.query = handler.createExecutableQuery(session, statement, language);
        initialized = true;
    }

    /**
     * @inheritDoc
     */
    public void init(SessionImpl session,
                     QueryHandler handler,
                     Node node)
            throws InvalidQueryException, RepositoryException {
        checkNotInitialized();
        this.session = session;
        this.node = node;
        this.handler = handler;

        try {
            //if (!node.isNodeType(QName.NT_QUERY.toJCRName(session.getNamespaceResolver()))) {
            //Constants.NT_QUERY
            if (!node.isNodeType("nt:query")) {
                throw new InvalidQueryException("node is not of type nt:query");
            }
            statement = node.getProperty("jcr:statement").getString();
            language = node.getProperty("jcr:language").getString();
            query = handler.createExecutableQuery(session, statement, language);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        initialized = true;
    }

    /**
     * This method simply forwards the <code>execute</code> call to the
     * {@link ExecutableQuery} object returned by
     * {@link QueryHandler#createExecutableQuery}.
     * {@inheritDoc}
     */
    public QueryResult execute() throws RepositoryException {
        return query.execute();
    }

    /**
     * {@inheritDoc}
     */
    public String getStatement() {
        return statement;
    }

    /**
     * {@inheritDoc}
     */
    public String getLanguage() {
        return language;
    }

    /**
     * {@inheritDoc}
     */
    public String getStoredQueryPath()
            throws ItemNotFoundException, RepositoryException {
        if (node == null) {
            throw new ItemNotFoundException("not a persistent query");
        }
        return node.getPath();
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.Query#storeAsNode(java.lang.String)
     */
    public Node storeAsNode(String absPath)
            throws ItemExistsException,
            PathNotFoundException,
            VersionException,
            ConstraintViolationException,
            LockException,
            UnsupportedRepositoryOperationException,
            RepositoryException {

      // [PN] 21.12.06
      
      JCRPath path = session.getLocationFactory().parseAbsPath(absPath);
      QPath qpath = path.getInternalPath();
      //String parentPath = path.makeParentPath().getAsString(false);
      //String nodeName = path.getName().getAsString();

      //NodeImpl parent = (NodeImpl)session.getItem(parentPath);
      NodeData rootData = (NodeData) session.getTransientNodesManager().getItemData(Constants.ROOT_UUID);
      NodeImpl parent = (NodeImpl) session.getTransientNodesManager().getItem(rootData,qpath.makeParentPath(), false);
      if (parent == null)
        throw new PathNotFoundException("Parent not found for " + path.getAsString(false));

      // validate as on parent child node
      parent.validateChildNode(qpath.getName(), Constants.NT_QUERY);
      
      //NodeImpl queryNode = parent.createChildNode(nodeName, "nt:query", false, true);
      NodeData queryData = TransientNodeData.createNodeData((NodeData) parent.getData(), qpath.getName(), Constants.NT_QUERY); 
      NodeImpl queryNode = (NodeImpl) session.getTransientNodesManager().update(ItemState.createAddedState(queryData), false);
      
      queryNode.addAutoCreatedItems(Constants.NT_QUERY);
      // set properties
      // Value[] vals = new Value[] {session.getValueFactory().createValue(language)};
      // queryNode.createChildProperty("jcr:language", vals, PropertyType.STRING);
      TransientValueData value = new TransientValueData(language); 
      TransientPropertyData jcrLanguage = TransientPropertyData.createPropertyData(
          queryData, Constants.JCR_LANGUAGE, PropertyType.STRING, false, value);
      session.getTransientNodesManager().update(ItemState.createAddedState(jcrLanguage), false);
      
      // vals = new Value[] {session.getValueFactory().createValue(statement)};
      // queryNode.createChildProperty("jcr:statement", vals, PropertyType.STRING);
      value = new TransientValueData(statement); 
      TransientPropertyData jcrStatement = TransientPropertyData.createPropertyData(
          queryData, Constants.JCR_STATEMENT, PropertyType.STRING, false, value);
      session.getTransientNodesManager().update(ItemState.createAddedState(jcrStatement), false);

      // NOTE: for save stored node need save() on parent or on session (6.6.10 The Query Object)
      node = queryNode;
      return node;
    }

    //-----------------------------< internal >---------------------------------

    /**
     * Checks if this query is not yet initialized and throws an
     * <code>IllegalStateException</code> if it is already initialized.
     */
    protected void checkNotInitialized() {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }
    }

    /**
     * Checks if this query is initialized and throws an
     * <code>IllegalStateException</code> if it is not yet initialized.
     */
    protected void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
    }
}


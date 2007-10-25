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
package org.exoplatform.services.jcr.impl.core.query.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.lucene.search.Query;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.query.AndQueryNode;
import org.exoplatform.services.jcr.impl.core.query.DefaultQueryNodeVisitor;
import org.exoplatform.services.jcr.impl.core.query.ExecutableQuery;
import org.exoplatform.services.jcr.impl.core.query.LocationStepQueryNode;
import org.exoplatform.services.jcr.impl.core.query.NodeTypeQueryNode;
import org.exoplatform.services.jcr.impl.core.query.NotQueryNode;
import org.exoplatform.services.jcr.impl.core.query.OrQueryNode;
import org.exoplatform.services.jcr.impl.core.query.OrderQueryNode;
import org.exoplatform.services.jcr.impl.core.query.PathQueryNode;
import org.exoplatform.services.jcr.impl.core.query.QueryParser;
import org.exoplatform.services.jcr.impl.core.query.QueryRootNode;
import org.exoplatform.services.jcr.impl.core.query.TextsearchQueryNode;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements the {@link ExecutableQuery} interface.
 */
class QueryImpl implements ExecutableQuery {

  /**
   * The logger instance for this class
   */
  private static Log                 log           = ExoLogger.getLogger("jcr.QueryImpl");

  /**
   * Represents a query that selects all nodes. E.g. in XPath: //*
   */
  private static final QueryRootNode ALL_NODES     = new QueryRootNode();

  static {
    PathQueryNode pathNode = new PathQueryNode(ALL_NODES);
    pathNode.addPathStep(new LocationStepQueryNode(pathNode, null, true));
    pathNode.setAbsolute(true);
    ALL_NODES.setLocationNode(pathNode);
  }

  /**
   * The root node of the query tree
   */
  private final QueryRootNode        root;

  /**
   * The session of the user executing this query
   */
  private final SessionImpl          session;

  /**
   * The item manager of the user executing this query
   */
  // private final ItemManager itemMgr;
  /**
   * The actual search index
   */
  private final SearchIndex          index;

  /**
   * The property type registry for type lookup.
   */
  // private final PropertyTypeRegistry propReg;
  /**
   * If <code>true</code> the default ordering of the result nodes is in
   * document order.
   */
  private boolean                    documentOrder = true;

  /**
   * Creates a new query instance from a query string.
   * 
   * @param session the session of the user executing this query.
   * @param itemMgr the item manager of the session executing this query.
   * @param index the search index.
   * @param propReg the property type registry.
   * @param statement the query statement.
   * @param language the syntax of the query statement.
   * @throws InvalidQueryException if the query statement is invalid according
   *           to the specified <code>language</code>.
   */
  public QueryImpl(SessionImpl session, SearchIndex index, String statement, String language)
      throws InvalidQueryException {
    this.session = session;
    this.index = index;
    // parse query according to language
    // build query tree
    log.debug("Query Statement: " + statement);
    this.root = QueryParser.parse(statement, language, session.getLocationFactory());
  }

  /**
   * Executes this query and returns a <code>{@link QueryResult}</code>.
   * 
   * @return a <code>QueryResult</code>
   * @throws RepositoryException if an error occurs
   */
  public QueryResult execute() throws RepositoryException {
    LocationFactory locFactory = session.getLocationFactory();

    if (log.isDebugEnabled()) {
      log.debug("Executing query: \n" + root.dump());
    }

    // check for special query
    if (ALL_NODES.equals(root)) {
      return new WorkspaceTraversalResult(session, new InternalQName[] { Constants.JCR_PATH });
    }

    Query query;
    // Whilecard check. Whilecard couldn't be first in fulltext query
    // because of lucene parser.
    try {
      // build lucene query
      query = LuceneQueryBuilder.createQuery(root, session, index.getSysLocationFactory(), index.getAnalyzer());
    } catch (Exception e) {
      log.error(e);
      throw new RepositoryException(e);
    }
    OrderQueryNode orderNode = root.getOrderNode();
    OrderQueryNode.OrderSpec[] orderSpecs;
    if (orderNode != null) {
      orderSpecs = orderNode.getOrderSpecs();
    } else {
      orderSpecs = new OrderQueryNode.OrderSpec[0];
    }
    InternalQName[] orderProperties = new InternalQName[orderSpecs.length];
    boolean[] ascSpecs = new boolean[orderSpecs.length];
    for (int i = 0; i < orderSpecs.length; i++) {
      orderProperties[i] = orderSpecs[i].getProperty();
      ascSpecs[i] = orderSpecs[i].isAscending();
    }

    List identifiers;
    List scores;

    // execute it
    QueryHits result = null;
    try {
      result = index.executeQuery(query, orderProperties, ascSpecs);
      identifiers = new ArrayList(result.length());
      scores = new ArrayList(result.length());

      for (int i = 0; i < result.length(); i++) {
        String identifier = result.doc(i).get(FieldNames.UUID);
        // check access
        identifiers.add(identifier);
        scores.add(new Float(result.score(i)));
      }
    } catch (IOException e) {
      log.error("Exception while executing query: ", e);
      identifiers = Collections.EMPTY_LIST;
      scores = Collections.EMPTY_LIST;
    } finally {
      if (result != null) {
        try {
          result.close();
        } catch (IOException e) {
          log.warn("Unable to close query result: " + e);
        }
      }
    }
    // get select properties
    List selectProps = new ArrayList();
    selectProps.addAll(Arrays.asList(root.getSelectProperties()));
    if (selectProps.size() == 0) {
      // use node type constraint
      LocationStepQueryNode[] steps = root.getLocationNode().getPathSteps();
      final InternalQName[] ntName = new InternalQName[1];
      steps[steps.length - 1].acceptOperands(new DefaultQueryNodeVisitor() {
        public Object visit(NodeTypeQueryNode node, Object data) {
          ntName[0] = node.getValue();
          return data;
        }
      }, null);
      if (ntName[0] == null) {
        ntName[0] = Constants.NT_BASE;
      }
      NodeType nt = ((NodeTypeManagerImpl) session.getWorkspace().getNodeTypeManager()).getNodeType(ntName[0]);

      PropertyDefinition[] propDefs = nt.getPropertyDefinitions();
      for (int i = 0; i < propDefs.length; i++) {
        if (!propDefs[i].isMultiple()) {
          InternalQName qname = locFactory.parseJCRName(propDefs[i].getName()).getInternalName();
          selectProps.add(qname);
        }
      }
    }

    // add jcr:path
    selectProps.add(Constants.JCR_PATH);

    // add jcr:score if necessary
    selectProps.add(Constants.JCR_SCORE);
    return new QueryResultImpl(
    // itemMgr,
        session, (String[]) identifiers.toArray(new String[identifiers.size()]), (Float[]) scores
            .toArray(new Float[scores.size()]), (InternalQName[]) selectProps.toArray(new InternalQName[selectProps
            .size()]), orderNode == null && documentOrder);

  }

  /**
   * If set <code>true</code> the result nodes will be in document order per
   * default (if no order by clause is specified). If set to <code>false</code>
   * the result nodes are returned in whatever sequence the index has stored the
   * nodes. That sequence is stable over multiple invocations of the same query,
   * but will change when nodes get added or removed from the index. <p/> The
   * default value for this property is <code>true</code>.
   * 
   * @return the current value of this property.
   */
  public boolean getRespectDocumentOrder() {
    return documentOrder;
  }

  /**
   * Sets a new value for this property.
   * 
   * @param documentOrder if <code>true</code> the result nodes are in
   *          document order per default.
   * @see #getRespectDocumentOrder()
   */
  public void setRespectDocumentOrder(boolean documentOrder) {
    this.documentOrder = documentOrder;
  }

  // -----------------------------< internal >---------------------------------

  /**
   * Returns <code>true</code> if <code>node</code> has a
   * {@link org.apache.jackrabbit.core.query.TextsearchQueryNode} somewhere down
   * the query tree; <code>false</code> otherwise.
   * 
   * @param node the path node.
   * @return <code>true</code> if the query tree contains a textsearch node,
   *         <code>false</code> otherwise.
   */
  private static boolean hasTextsearchNode(PathQueryNode node) {
    final boolean[] textsearch = new boolean[1];
    node.acceptOperands(new DefaultQueryNodeVisitor() {
      public Object visit(OrQueryNode node, Object data) {
        return node.acceptOperands(this, data);
      }

      public Object visit(AndQueryNode node, Object data) {
        return node.acceptOperands(this, data);
      }

      public Object visit(NotQueryNode node, Object data) {
        return node.acceptOperands(this, data);
      }

      public Object visit(TextsearchQueryNode node, Object data) {
        textsearch[0] = true;
        return data;
      }

      public Object visit(LocationStepQueryNode node, Object data) {
        return node.acceptOperands(this, data);
      }
    }, null);
    return textsearch[0];
  }

  /**
   * @return Returns the root.
   */
  public QueryRootNode getRoot() {
    return root;
  }
}

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

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements the <code>javax.jcr.query.QueryResult</code> interface.
 */
class QueryResultImpl implements QueryResult {

    /**
     * The logger instance for this class
     */
    private static Log log = ExoLogger.getLogger("jcr.QueryResultImpl");

    /**
     * The item manager of the session executing the query
     */
    //private final SessionDataManager itemMgr;

    /**
     * The identifiers of the result nodes
     */
    private final String[] identifiers;

    /**
     * The scores of the result nodes
     */
    private final Float[] scores;

    /**
     * The select properties
     */
    private final InternalQName[] selectProps;

    /**
     * The namespace resolver of the session executing the query
     */
    //private final LocationFactory locationFactory;

    private final SessionImpl session;

    /**
     * If <code>true</code> nodes are returned in document order.
     */
    private final boolean docOrder;

    /**
     * Creates a new query result.
     *
     * @param itemMgr     the item manager of the session executing the query.
     * @param identifiers the identifiers of the result nodes.
     * @param scores      the score values of the result nodes.
     * @param selectProps the select properties of the query.
     * @param resolver    the namespace resolver of the session executing the query.
     * @param docOrder    if <code>true</code> the result is returned in document
     *  order.
     */
    public QueryResultImpl(
        SessionImpl session,
        String[] identifiers,
        Float[] scores,
        InternalQName[] selectProps,
        boolean docOrder) {
        this.identifiers = identifiers;
        this.scores = scores;
        this.selectProps = selectProps;
        this.docOrder = docOrder;
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getColumnNames() throws RepositoryException {
        //try {
            String[] propNames = new String[selectProps.length];
            for (int i = 0; i < selectProps.length; i++) {
                //propNames[i] = selectProps[i].toJCRName(resolver);
              propNames[i] = session.getLocationFactory().createJCRName(selectProps[i]).getAsString();
            }
            return propNames;
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getNodes() throws RepositoryException {
        return getNodeIterator();
    }

    /**
     * {@inheritDoc}
     */
    public RowIterator getRows() throws RepositoryException {
        return new RowIteratorImpl(getNodeIterator(), selectProps, session);
    }

    /**
     * Creates a node iterator over the result nodes.
     * @return a node iterator over the result nodes.
     */
    private ScoreNodeIterator getNodeIterator() {
      log.debug("getNodeIterator() "+docOrder+" "+identifiers.length+" "+scores.length);
        if (docOrder) {
            return new DocOrderNodeIteratorImpl(session.getTransientNodesManager(), identifiers, scores);
        } else {
            return new NodeIteratorImpl(session.getTransientNodesManager(), identifiers, scores);
        }
    }
}

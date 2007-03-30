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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.LogService;

/**
 * This class implements the {@link QueryManager} interface.
 */
public class QueryManagerImpl implements QueryManager {

    /**
     * Defines all supported query languages
     */
    private static final String[] SUPPORTED_QUERIES = new String[]{
        Query.SQL, Query.XPATH
    };

    /**
     * List of all supported query languages
     */
    private static final List SUPPORTED_QUERIES_LIST
            = Collections.unmodifiableList(Arrays.asList(SUPPORTED_QUERIES));

    /**
     * The <code>Session</code> for this QueryManager.
     */
    private final SessionImpl session;

    /**
     * The <code>SearchManager</code> holding the search index.
     */
    private final SearchManager searchMgr;

    /**
     * Creates a new <code>QueryManagerImpl</code> for the passed
     * <code>session</code>
     *
     * @param session
     * @param searchMgr
     */
    public QueryManagerImpl(SessionImpl session,
                            SearchManager searchMgr,
                            LogService logService) {
        this.session = session;
        this.searchMgr = searchMgr;
    }

    /**
     * {@inheritDoc}
     */
    public Query createQuery(String statement, String language)
            throws InvalidQueryException, RepositoryException {
        return searchMgr.createQuery(session, statement, language);
    }

    /**
     * {@inheritDoc}
     */
    public Query getQuery(Node node)
            throws InvalidQueryException, RepositoryException {

        return searchMgr.createQuery(session, node);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getSupportedQueryLanguages() throws RepositoryException {
        return (String[]) SUPPORTED_QUERIES_LIST.toArray(new String[SUPPORTED_QUERIES.length]);
    }
}

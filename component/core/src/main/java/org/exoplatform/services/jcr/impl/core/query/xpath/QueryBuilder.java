/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
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
package org.exoplatform.services.jcr.impl.core.query.xpath;

import org.exoplatform.services.jcr.impl.core.query.QueryTreeBuilder;
import org.exoplatform.services.jcr.impl.core.query.QueryRootNode;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
//import org.apache.jackrabbit.name.NamespaceResolver;

import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;

/**
 * Implements the XPath query tree builder.
 */
public class QueryBuilder implements QueryTreeBuilder {

    /**
     * @inheritDoc
     */
    public QueryRootNode createQueryTree(String statement,
                                         LocationFactory locationFactory)
            throws InvalidQueryException {
        return XPathQueryBuilder.createQuery(statement, locationFactory);
    }

    /**
     * @inheritDoc
     */
    public boolean canHandle(String language) {
        return Query.XPATH.equals(language);
    }

    /**
     * This builder supports {@link Query#XPATH}.
     * @inheritDoc
     */
    public String[] getSupportedLanguages() {
        return new String[]{Query.XPATH};
    }

    /**
     * @inheritDoc
     */
    public String toString(QueryRootNode root, LocationFactory locationFactory)
            throws InvalidQueryException {
        return XPathQueryBuilder.toString(root, locationFactory);
    }
}

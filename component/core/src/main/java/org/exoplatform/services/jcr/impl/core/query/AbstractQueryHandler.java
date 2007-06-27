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
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * Implements default behaviour for some methods of {@link QueryHandler}.
 */
public abstract class AbstractQueryHandler implements QueryHandler {

    /**
     * This default implementation calls the individual {@link #deleteNode(String)}
     * and {@link #addNode(org.apache.jackrabbit.core.state.NodeState)} methods
     * for each entry in the iterators. First the nodes to remove are processed
     * then the nodes to add.
     *
     * @param remove uuids of nodes to remove.
     * @param add NodeStates to add.
     * @throws RepositoryException if an error occurs while indexing a node.
     * @throws IOException if an error occurs while updating the index.
     */
    public synchronized void updateNodes(Iterator remove, Iterator add)
            throws RepositoryException, IOException {
        while (remove.hasNext()) {
            deleteNode((String) remove.next());
        }
        while (add.hasNext()) {
            addNode((NodeImpl) add.next());
        }
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exoplatform.services.jcr.rmi.api.client.iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;

/**
 * A ClientIterator for iterating remote nodes.
 */
public class ClientNodeIterator extends ClientIterator implements NodeIterator {

    /** The current session. */
    private final Session session;

    /**
     * Creates a ClientNodeIterator instance.
     *
     * @param iterator      remote iterator
     * @param session       current session
     * @param factory       local adapter factory
     */
    public ClientNodeIterator(
            RemoteIterator iterator, Session session,
            LocalAdapterFactory factory) {
        super(iterator, factory);
        this.session = session;
    }

    /**
     * Creates and returns a local adapter for the given remote node.
     *
     * @param remote remote referecne
     * @return local adapter
     * @see ClientIterator#getObject(Object)
     */
    protected Object getObject(Object remote) {
        return getNode(session, (RemoteNode) remote);
    }

    /**
     * Returns the next node in this iteration.
     *
     * @return next node
     * @see NodeIterator#nextNode()
     */
    public Node nextNode() {
        return (Node) next();
    }

}

/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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

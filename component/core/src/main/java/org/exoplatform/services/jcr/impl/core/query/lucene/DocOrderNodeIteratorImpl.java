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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements a NodeIterator that returns the nodes in document order.
 */
class DocOrderNodeIteratorImpl implements ScoreNodeIterator {

    /** Logger instance for this class */
    private static Log log = ExoLogger.getLogger("jcr.DocOrderNodeIteratorImpl");

    /** A node iterator with ordered nodes */
    private NodeIteratorImpl orderedNodes;

    /** The identifiers of the nodes in the result set */
    protected String[] identifiers;

    /** The score values for the nodes in the result set */
    protected Float[] scores;

    /** ItemManager to turn Identifiers into Node instances */
//    protected final ItemManager itemMgr;
    protected final SessionDataManager itemMgr;

    /**
     * Creates a <code>DocOrderNodeIteratorImpl</code> that orders the nodes
     * with <code>identifiers</code> in document order.
     * @param itemMgr the item manager of the session executing the query.
     * @param identifiers the identifiers of the nodes.
     * @param scores the score values of the nodes.
     */
    DocOrderNodeIteratorImpl(final SessionDataManager itemMgr, String[] identifiers, Float[] scores) {
        this.itemMgr = itemMgr;
        this.identifiers = identifiers;
        this.scores = scores;
    }

    /**
     * {@inheritDoc}
     */
    public Object next() {
        return nextNodeImpl();
    }

    /**
     * {@inheritDoc}
     */
    public Node nextNode() {
        return nextNodeImpl();
    }

    /**
     * {@inheritDoc}
     */
    public NodeImpl nextNodeImpl() {
        initOrderedIterator();
        return orderedNodes.nextNodeImpl();
    }

    /**
     * @throws UnsupportedOperationException always.
     */
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * {@inheritDoc}
     */
    public void skip(long skipNum) {
        initOrderedIterator();
        orderedNodes.skip(skipNum);
    }

    /**
     * Returns the number of nodes in this iterator.
     * </p>
     * Note: The number returned by this method may differ from the number
     * of nodes actually returned by calls to hasNext() / getNextNode()! This
     * is because this iterator works on a lazy instantiation basis and while
     * iterating over the nodes some of them might have been deleted in the
     * meantime. Those will not be returned by getNextNode(). As soon as an
     * invalid node is detected, the size of this iterator is adjusted.
     *
     * @return the number of node in this iterator.
     */
    public long getSize() {
        if (orderedNodes != null) {
            return orderedNodes.getSize();
        } else {
            return identifiers.length;
        }
    }

    /**
     * {@inheritDoc}
     */
    public long getPosition() {
        initOrderedIterator();
        return orderedNodes.getPosition();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        initOrderedIterator();
        return orderedNodes.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    public float getScore() {
        initOrderedIterator();
        return orderedNodes.getScore();
    }

    //------------------------< internal >--------------------------------------

    /**
     * Initializes the NodeIterator in document order
     */
    private void initOrderedIterator() {

        if (orderedNodes != null) {
            return;
        }
        long time = System.currentTimeMillis();
        ScoreNode[] nodes = new ScoreNode[identifiers.length];
        for (int i = 0; i < identifiers.length; i++) {
            nodes[i] = new ScoreNode(identifiers[i], scores[i]);
        }

        final List invalidIdentifiers = new ArrayList(2);

        do {
            if (invalidIdentifiers.size() > 0) {
                // previous sort run was not successful -> remove failed uuids
                ScoreNode[] tmp = new ScoreNode[nodes.length - invalidIdentifiers.size()];
                int newIdx = 0;
                for (int i = 0; i < nodes.length; i++) {
                    if (!invalidIdentifiers.contains(nodes[i].identifier)) {
                        tmp[newIdx++] = nodes[i];
                    }
                }
                nodes = tmp;
                invalidIdentifiers.clear();
            }

            try {
                // sort the identifiers
                Arrays.sort(nodes, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        ScoreNode n1 = (ScoreNode) o1;
                        ScoreNode n2 = (ScoreNode) o2;
                        try {
                            NodeImpl node1;
                            try {
//                                node1 = (NodeImpl) itemMgr.getItem(new NodeId(n1.uuid));
                              node1 = (NodeImpl) itemMgr.getItemByIdentifier(n1.identifier, true);
                              if(node1 == null)
                                throw new RepositoryException("Node not found for "+n1.identifier);
                            } catch (RepositoryException e) {
                                log.warn("Node " + n1.identifier + " does not exist anymore: " + e);
                                // node does not exist anymore
                                invalidIdentifiers.add(n1.identifier);
                                throw new SortFailedException();
                            }
                            NodeImpl node2;
                            try {
//                                node2 = (NodeImpl) itemMgr.getItem(new NodeId(n2.uuid));
                              node2 = (NodeImpl) itemMgr.getItemByIdentifier(n2.identifier, true);
                              if(node2 == null)
                                throw new RepositoryException("Node not found for "+n2.identifier);
                            } catch (RepositoryException e) {
                                log.warn("Node " + n2.identifier + " does not exist anymore: " + e);
                                // node does not exist anymore
                                invalidIdentifiers.add(n2.identifier);
                                throw new SortFailedException();
                            }
//                            Path.PathElement[] path1 = node1.getPrimaryPath().getElements();
//                            Path.PathElement[] path2 = node2.getPrimaryPath().getElements();
                            QPathEntry[] path1 = node1.getLocation().getInternalPath().getEntries();
                            QPathEntry[] path2 = node2.getLocation().getInternalPath().getEntries();

                            // find nearest common ancestor
                            int commonDepth = 0; // root
                            while (path1.length > commonDepth && path2.length > commonDepth) {
                                if (path1[commonDepth].equals(path2[commonDepth])) {
                                    commonDepth++;
                                } else {
                                    break;
                                }
                            }
                            // path elements at last depth were equal
                            commonDepth--;

                            // check if either path is an ancestor of the other
                            if (path1.length - 1 == commonDepth) {
                                // path1 itself is ancestor of path2
                                return -1;
                            }
                            if (path2.length - 1 == commonDepth) {
                                // path2 itself is ancestor of path1
                                return 1;
                            }
                            // get common ancestor node
                            NodeImpl commonNode = (NodeImpl) node1.getAncestor(commonDepth);

                            // move node1/node2 to the commonDepth + 1
                            // node1 and node2 then will be child nodes of commonNode
                            node1 = (NodeImpl) node1.getAncestor(commonDepth + 1);
                            node2 = (NodeImpl) node2.getAncestor(commonDepth + 1);
                            //log.error("\tCommon node: " + commonNode.getPath()+" "+commonDepth);

                            for (NodeIterator it = commonNode.getNodes(); it.hasNext();) {
                                Node child = it.nextNode();
                                //log.debug("\tChild: " + child.getPath()+" ");
                                if (child.isSame(node1)) {
                                    return -1;
                                } else if (child.isSame(node2)) {
                                    return 1;
                                }
                            }
                            log.error("Internal error: unable to determine document order of nodes:");
                            log.error("\tNode1: " + node1.getPath());
                            log.error("\tNode2: " + node2.getPath());
                        } catch (RepositoryException e) {
                            log.error("Exception while sorting nodes in document order: " + e.toString(), e);
                        }
                        // if we get here something went wrong
                        // remove both identifiers from array
                        invalidIdentifiers.add(n1.identifier);
                        invalidIdentifiers.add(n2.identifier);
                        // terminate sorting
                        throw new SortFailedException();
                    }
                });
            } catch (SortFailedException e) {
                // retry
            }

        } while (invalidIdentifiers.size() > 0);

        // resize identifiers and scores array if we had to remove some identifiers
        if (identifiers.length != nodes.length) {
            identifiers = new String[nodes.length];
            scores = new Float[nodes.length];
        }

        for (int i = 0; i < nodes.length; i++) {
            identifiers[i] = nodes[i].identifier;
            scores[i] = nodes[i].score;
        }
        if (log.isDebugEnabled()) {
            log.debug("" + identifiers.length + " node(s) ordered in " + (System.currentTimeMillis() - time) + " ms");
        }
        orderedNodes = new NodeIteratorImpl(itemMgr, identifiers, scores);
    }

    /**
     * Simple helper class that associates a score with each node identifier.
     */
    private static final class ScoreNode {

        final String identifier;

        final Float score;

        ScoreNode(String identifier, Float score) {
            this.identifier = identifier;
            this.score = score;
        }
    }

    /**
     * Indicates that sorting failed.
     */
    private static final class SortFailedException extends RuntimeException {
    }
}

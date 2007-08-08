/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.query.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 *
 * Implements a NodeIterator that returns the nodes in document order.
 * Nodes will ordered using NodeData location depth.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
class DocOrderNodeDataIteratorImpl implements ScoreNodeIterator {

    /** Logger instance for this class */
    private static Log log = ExoLogger.getLogger("jcr.DocOrderNodeDataIteratorImpl");

    /** A node iterator with ordered nodes */
    private NodeIteratorImpl orderedNodes;

    /** The identifiers of the nodes in the result set */
    protected String[] identifiers;

    /** The score values for the nodes in the result set */
    protected Float[] scores;

    /** ItemManager to turn Identifiers into Node instances */
//    protected final ItemManager itemMgr;
    protected final SessionDataManager dataManager;
    
    /**
     * Creates a <code>DocOrderNodeIteratorImpl</code> that orders the nodes
     * with <code>identifiers</code> in document order.
     * @param itemMgr the item manager of the session executing the query.
     * @param identifiers the identifiers of the nodes.
     * @param scores the score values of the nodes.
     */
    DocOrderNodeDataIteratorImpl(final SessionDataManager dataManager, String[] identifiers, Float[] scores) {
        this.dataManager = dataManager;
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

        final List<String> invalidIdentifiers = new ArrayList<String>(2);
        
        /** Cache for Nodes obtainer during the order (comparator work) */
        final Map<String, NodeData> lcache = new HashMap<String, NodeData>();
        
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
                Arrays.sort(nodes, new Comparator<ScoreNode>() {

                    private NodeData getNodeData(String id) throws RepositoryException {
                      NodeData node = lcache.get(id);
                      if (node == null) {
                        node = (NodeData) dataManager.getItemData(id);
                        if (node != null)
                          lcache.put(id, node);
                        return node;
                      } else
                        return node;
                    }
                    
                    public int compare(ScoreNode n1, ScoreNode n2) {
                        try {
                            NodeData node1;
                            try {
                              node1 = getNodeData(n1.identifier);
                              if(node1 == null)
                                throw new RepositoryException("Node not found for "+n1.identifier);
                            } catch (RepositoryException e) {
                                log.warn("Node " + n1.identifier + " does not exist anymore: " + e);
                                // node does not exist anymore
                                invalidIdentifiers.add(n1.identifier);
                                throw new SortFailedException();
                            }
                            
                            NodeData node2;
                            try {
                              node2 = getNodeData(n2.identifier);
                              if(node2 == null)
                                throw new RepositoryException("Node not found for "+n2.identifier);
                            } catch (RepositoryException e) {
                                log.warn("Node " + n2.identifier + " does not exist anymore: " + e);
                                // node does not exist anymore
                                invalidIdentifiers.add(n2.identifier);
                                throw new SortFailedException();
                            }
                            
                            QPath path1 = node1.getQPath();
                            QPath path2 = node2.getQPath();
                            
                            QPathEntry[] pentries1 = path1.getEntries();
                            QPathEntry[] pentries2 = path2.getEntries();

                            // find nearest common ancestor
                            int commonDepth = 0; // root
                            while (pentries1.length > commonDepth && pentries2.length > commonDepth) {
                                if (pentries1[commonDepth].equals(pentries2[commonDepth])) {
                                    commonDepth++;
                                } else {
                                    break;
                                }
                            }
                            
                            // path elements at last depth were equal
                            commonDepth--;

                            // check if either path is an ancestor of the other
                            if (pentries1.length - 1 == commonDepth) {
                                // path1 itself is ancestor of path2
                                return -1;
                            }
                            
                            if (pentries2.length - 1 == commonDepth) {
                                // path2 itself is ancestor of path1
                                return 1;
                            }
                            
                            return node1.getOrderNumber() - node2.getOrderNumber();
                        } catch (SortFailedException e) {
                          throw e;
                        } catch (Exception e) {
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
        orderedNodes = new NodeIteratorImpl(dataManager, identifiers, scores);
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

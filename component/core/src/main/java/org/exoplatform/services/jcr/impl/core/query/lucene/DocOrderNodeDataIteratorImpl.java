/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.query.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 *
 * Implements a NodeIterator that returns the nodes in document order.
 * Nodes will ordered using NodeData location depth and siblings order number.
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
class DocOrderNodeDataIteratorImpl implements ScoreNodeIterator {

    /** Logger instance for this class */
    private static Log log = ExoLogger.getLogger("jcr.DocOrderNodeDataIteratorImpl");

    /** A node iterator with ordered nodes */
    private boolean ordered = false;

    /** The identifiers of the nodes in the result set */
    protected String[] identifiers;

    /** The score values for the nodes in the result set */
    protected Float[] scores;

    /** ItemManager to turn Identifiers into Node instances */
    protected final SessionDataManager dataManager;

    /** Current position in the identifier array */
    protected int ipos = -1;

    /** Invalid IDs count. Obtained durong the fetch if cached=true*/
    protected int invalid = 0;

    /** Reference to the next node instance */
    private NodeImpl inext;

    /** Tells if the iterator will cache nodes during the order,
     * I.e. it will return nodes with state actual on order time,
     * not on fetch time.*/
    private final boolean cached;

    private SessionImpl session;

    /**
     * Indicates that sorting failed.
     */
    private static final class SortFailedException extends RuntimeException {
    }

    /**
     * Creates a <code>DocOrderNodeDataIteratorImpl</code> that orders the nodes
     * with <code>identifiers</code> in document order.
     * @param itemMgr the item manager of the session executing the query.
     * @param identifiers the identifiers of the nodes.
     * @param scores the score values of the nodes.
     */
    DocOrderNodeDataIteratorImpl(final SessionDataManager dataManager, String[] identifiers, Float[] scores) {
        this.dataManager = dataManager;
        this.session = null;
        this.identifiers = identifiers;
        this.scores = scores;
        this.cached = true;
    }

    DocOrderNodeDataIteratorImpl(final SessionDataManager dataManager, String[] identifiers, Float[] scores, boolean cached) {
      this.dataManager = dataManager;
      this.session = null;
      this.identifiers = identifiers;
      this.scores = scores;
      this.cached = cached;
    }

    DocOrderNodeDataIteratorImpl(final SessionImpl session, String[] identifiers, Float[] scores) {
      this.dataManager = session.getTransientNodesManager();
      this.session = session;
      this.identifiers = identifiers;
      this.scores = scores;
      this.cached = true;
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

        if (inext == null) {
          throw new NoSuchElementException();
        }

        NodeImpl n = inext;
        fetchNext();
        return n;
    }

    /**
     * @throws UnsupportedOperationException always.
     */
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * Skip a number of <code>Node</code>s in this iterator.
     * @param skipNum the non-negative number of <code>Node</code>s to skip
     * @throws NoSuchElementException
     *          if skipped past the last <code>Node</code> in this iterator.
     */
    public void skip(long skipNum) {
      initOrderedIterator();

      if (skipNum < 0) {
        throw new IllegalArgumentException("skipNum must not be negative");
      }
      if ((ipos + skipNum) > identifiers.length) {
          throw new NoSuchElementException();
      }
      if (skipNum > 0) {
          ipos += skipNum - 1;
          fetchNext();
      }
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
      return identifiers.length - invalid;
    }

    /**
     * {@inheritDoc}
     */
    public long getPosition() {
        initOrderedIterator();

        return ipos - invalid;
    }

    /**
     * Returns <code>true</code> if there is another <code>Node</code>
     * available; <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is another <code>Node</code>
     *  available; <code>false</code> otherwise.
     */
    public boolean hasNext() {
        initOrderedIterator();

        return inext != null;
    }

    /**
     * Returns the score of the node returned by {@link #nextNode()}. In other
     * words, this method returns the score value of the next <code>Node</code>.
     * @return the score of the node returned by {@link #nextNode()}.
     * @throws NoSuchElementException if there is no next node.
     */
    public float getScore() {
        initOrderedIterator();

        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return scores[ipos].floatValue();
    }

    //------------------------< internal >--------------------------------------

    /**
     * Initializes the NodeIterator in document order
     */
    private void initOrderedIterator() {

        if (ordered)
          return;

        long time = System.currentTimeMillis();
        ScoreNode[] nodes = new ScoreNode[identifiers.length];
        for (int i = 0; i < identifiers.length; i++) {
            nodes[i] = new ScoreNode(identifiers[i], scores[i]);
        }

        final Set<String> invalidIdentifiers = new LinkedHashSet<String>(2);

        /** Cache for Nodes obtainer during the order (comparator work) */
        final Map<String, NodeData> lcache = new HashMap<String, NodeData>();

        try {

          final DataManager pmanager = session != null ?
              (DataManager) session.getContainer().getComponentInstanceOfType(CacheableWorkspaceDataManager.class) :
              dataManager.getTransactManager().getStorageDataManager();

          do {
              if (invalidIdentifiers.size() > 0) {
                  // previous sort run was not successful -> remove failed uuids
                
                  // [PN] 05.11.07 use Lits instead array
                  final List<ScoreNode> tmp = new ArrayList<ScoreNode>();
                  //ScoreNode[] tmp = new ScoreNode[nodes.length - invalidIdentifiers.size()];
                  //int newIdx = 0;
                  for (int i = 0; i < nodes.length; i++) {
                    if (nodes[i] != null) {
                      if (!invalidIdentifiers.contains(nodes[i].identifier)) {
                        //tmp[newIdx++] = nodes[i];
                        tmp.add(nodes[i]);
                      }
                    } else {
                      log.warn("Invalid identifiers set contains null ScoreNode, skiped");
                    }
                  }
                  nodes = tmp.toArray(new ScoreNode[tmp.size()]);
                  invalidIdentifiers.clear();
              }

              try {
                  // sort the identifiers
                  Arrays.sort(nodes, new Comparator<ScoreNode>() {

                      private NodeData getNode(String id) throws RepositoryException {
                        NodeData node = lcache.get(id);
                        if (node == null) {
                          node = (NodeData) pmanager.getItemData(id);
                          if (node != null)
                            lcache.put(id, node);
                          return node;
                        } else
                          return node;
                      }

                      public int compare(final ScoreNode n1, final ScoreNode n2) {
                          try {
                              NodeData ndata1;
                              try {
                                ndata1 = getNode(n1.identifier);
                                if(ndata1 == null)
                                  throw new RepositoryException("Node not found for "+n1.identifier);
                              } catch (RepositoryException e) {
                                  //log.warn("Node " + n1.identifier + " does not exist anymore: " + e);
                                  // node does not exist anymore
                                  invalidIdentifiers.add(n1.identifier);
                                  throw new SortFailedException();
                              }

                              NodeData ndata2;
                              try {
                                ndata2 = getNode(n2.identifier);
                                if(ndata2 == null)
                                  throw new RepositoryException("Node not found for "+n2.identifier);
                              } catch (RepositoryException e) {
                                  //log.warn("Node " + n2.identifier + " does not exist anymore: " + e);
                                  // node does not exist anymore
                                  invalidIdentifiers.add(n2.identifier);
                                  throw new SortFailedException();
                              }

                              QPath path1 = ndata1.getQPath();
                              QPath path2 = ndata2.getQPath();

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

                              return ndata1.getOrderNumber() - ndata2.getOrderNumber();
                          } catch (SortFailedException e) {
                            throw e;
                          } catch (Exception e) {
                            log.error("Exception while sorting nodes in document order: " + e.toString(), e);
                          }
                          
                          // if we get here something went wrong
                          // remove both identifiers from array
                          if (n1 != null)
                            invalidIdentifiers.add(n1.identifier);
                          else
                            log.warn("Null ScoreNode n1 will not be added into invalid identifiers set");
                          if (n2 != null)
                            invalidIdentifiers.add(n2.identifier);
                          else
                            log.warn("Null ScoreNode n2 will not be added into invalid identifiers set");
                          
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

        } finally {   
          session = null;
          lcache.clear();
        }

        fetchNext();
        ordered = true;
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

    protected void fetchNext() {
      // reset
      inext = null;
      while (inext == null && (ipos + 1) < identifiers.length) {
        try {
          //inext = getNode(identifiers[ipos + 1]);
          inext = (NodeImpl) dataManager.getItemByIdentifier(identifiers[ipos + 1], true);
        } catch (RepositoryException e) {
          log.warn("Exception retrieving Node with UUID: " + identifiers[ipos + 1] + ": " + e, e);
          invalid++;
        }
        ipos++;
      }

      if (identifiers.length == 0)
        ipos = identifiers.length;
      else if ((ipos + 1) == identifiers.length && inext == null)
        ipos++;
    }
}

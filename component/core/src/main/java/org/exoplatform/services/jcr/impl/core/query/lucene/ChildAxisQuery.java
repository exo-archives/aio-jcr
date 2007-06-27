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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Weight;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.query.LocationStepQueryNode;
import org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy;

/**
 * Implements a lucene <code>Query</code> which returns the child nodes of the
 * nodes selected by another <code>Query</code>.
 */
class ChildAxisQuery extends Query {

    /**
     * The item state manager containing persistent item states.
     */
    private final WorkspaceStorageDataManagerProxy itemMgr;

    /**
     * The context query
     */
    private final Query contextQuery;

    /**
     * The nameTest to apply on the child axis, or <code>null</code> if all
     * child nodes should be selected.
     */
    private final String nameTest;

    /**
     * The context position for the selected child node, or
     * {@link LocationStepQueryNode#NONE} if no position is specified.
     */
    private final int position;

    /**
     * The scorer of the context query
     */
    private Scorer contextScorer;

    /**
     * The scorer of the name test query
     */
    private Scorer nameTestScorer;

    /**
     * Creates a new <code>ChildAxisQuery</code> based on a <code>context</code>
     * query.
     *
     * @param itemMgr the item state manager.
     * @param context the context for this query.
     * @param nameTest a name test or <code>null</code> if any child node is
     * selected.
     */
    ChildAxisQuery(WorkspaceStorageDataManagerProxy itemMgr, Query context, String nameTest) {
        this(itemMgr, context, nameTest, LocationStepQueryNode.NONE);
    }

    /**
     * Creates a new <code>ChildAxisQuery</code> based on a <code>context</code>
     * query.
     *
     * @param itemMgr the item state manager.
     * @param context the context for this query.
     * @param nameTest a name test or <code>null</code> if any child node is
     * selected.
     * @param position the context position of the child node to select. If
     * <code>position</code> is {@link LocationStepQueryNode#NONE}, the context
     * position of the child node is not checked.
     */
    ChildAxisQuery(WorkspaceStorageDataManagerProxy itemMgr, Query context, String nameTest, int position) {
        this.itemMgr = itemMgr;
        this.contextQuery = context;
        this.nameTest = nameTest;
        this.position = position;
    }

    /**
     * Creates a <code>Weight</code> instance for this query.
     *
     * @param searcher the <code>Searcher</code> instance to use.
     * @return a <code>ChildAxisWeight</code>.
     */
    protected Weight createWeight(Searcher searcher) {
        return new ChildAxisWeight(searcher);
    }

    /**
     * Always returns 'ChildAxisQuery'.
     *
     * @param field the name of a field.
     * @return 'ChildAxisQuery'.
     */
    public String toString(String field) {
        return "ChildAxisQuery";
    }

    //-------------------< ChildAxisWeight >------------------------------------

    /**
     * The <code>Weight</code> implementation for this <code>ChildAxisQuery</code>.
     */
    private class ChildAxisWeight implements Weight {

        /**
         * The searcher in use
         */
        private final Searcher searcher;

        /**
         * Creates a new <code>ChildAxisWeight</code> instance using
         * <code>searcher</code>.
         *
         * @param searcher a <code>Searcher</code> instance.
         */
        private ChildAxisWeight(Searcher searcher) {
            this.searcher = searcher;
        }

        /**
         * Returns this <code>ChildAxisQuery</code>.
         *
         * @return this <code>ChildAxisQuery</code>.
         */
        public Query getQuery() {
            return ChildAxisQuery.this;
        }

        /**
         * {@inheritDoc}
         */
        public float getValue() {
            return 1.0f;
        }

        /**
         * {@inheritDoc}
         */
        public float sumOfSquaredWeights() throws IOException {
            return 1.0f;
        }

        /**
         * {@inheritDoc}
         */
        public void normalize(float norm) {
        }

        /**
         * Creates a scorer for this <code>ChildAxisQuery</code>.
         *
         * @param reader a reader for accessing the index.
         * @return a <code>ChildAxisScorer</code>.
         * @throws IOException if an error occurs while reading from the index.
         */
        public Scorer scorer(IndexReader reader) throws IOException {
            contextScorer = contextQuery.weight(searcher).scorer(reader);
            if (nameTest != null) {
                nameTestScorer = new TermQuery(new Term(FieldNames.LABEL, nameTest)).weight(searcher).scorer(reader);
            }
            return new ChildAxisScorer(searcher.getSimilarity(), reader);
        }

        /**
         * {@inheritDoc}
         */
        public Explanation explain(IndexReader reader, int doc) throws IOException {
            return new Explanation();
        }
    }

    //----------------------< ChildAxisScorer >---------------------------------

    /**
     * Implements a <code>Scorer</code> for this <code>ChildAxisQuery</code>.
     */
    private class ChildAxisScorer extends Scorer {

        /**
         * An <code>IndexReader</code> to access the index.
         */
        private final IndexReader reader;

        /**
         * BitSet storing the id's of selected documents
         */
        private final BitSet hits;

        /**
         * List of Identifierss of selected nodes
         */
        private List identifiers = null;

        /**
         * The next document id to return
         */
        private int nextDoc = -1;

        /**
         * Creates a new <code>ChildAxisScorer</code>.
         *
         * @param similarity the <code>Similarity</code> instance to use.
         * @param reader     for index access.
         */
        protected ChildAxisScorer(Similarity similarity, IndexReader reader) {
            super(similarity);
            this.reader = reader;
            this.hits = new BitSet(reader.maxDoc());
        }

        /**
         * {@inheritDoc}
         */
        public boolean next() throws IOException {
            calculateChildren();
            nextDoc = hits.nextSetBit(nextDoc + 1);
            return nextDoc > -1;
        }

        /**
         * {@inheritDoc}
         */
        public int doc() {
            return nextDoc;
        }

        /**
         * {@inheritDoc}
         */
        public float score() throws IOException {
            return 1.0f;
        }

        /**
         * {@inheritDoc}
         */
        public boolean skipTo(int target) throws IOException {
            nextDoc = hits.nextSetBit(target);
            return nextDoc > -1;
        }

        /**
         * {@inheritDoc}
         *
         * @throws UnsupportedOperationException this implementation always
         *                                       throws an <code>UnsupportedOperationException</code>.
         */
        public Explanation explain(int doc) throws IOException {
            throw new UnsupportedOperationException();
        }

        private void calculateChildren() throws IOException {
            if (identifiers == null) {
                identifiers = new ArrayList();
                contextScorer.score(new HitCollector() {
                    public void collect(int doc, float score) {
                        hits.set(doc);
                    }
                });

                // collect nameTest hits
                final BitSet nameTestHits = new BitSet();
                if (nameTestScorer != null) {
                    nameTestScorer.score(new HitCollector() {
                        public void collect(int doc, float score) {
                            nameTestHits.set(doc);
                        }
                    });
                }

                // read the identifiers of the context nodes
                for (int i = hits.nextSetBit(0); i >= 0; i = hits.nextSetBit(i + 1)) {
                    String identifier = reader.document(i).get(FieldNames.UUID);
                    identifiers.add(identifier);
                }

                // collect the doc ids of all child nodes. we reuse the existing
                // bitset.
                hits.clear();
                TermDocs docs = reader.termDocs();
                try {
                    for (Iterator it = identifiers.iterator(); it.hasNext();) {
                        docs.seek(new Term(FieldNames.PARENT, (String) it.next()));
                        while (docs.next()) {
                            hits.set(docs.doc());
                        }
                    }
                } finally {
                    docs.close();
                }
                // filter out the child nodes that do not match the name test
                // if there is any name test at all.
                if (nameTestScorer != null) {
                    hits.and(nameTestHits);
                }

                // filter by index
                if (position != LocationStepQueryNode.NONE) {
                    for (int i = hits.nextSetBit(0); i >= 0; i = hits.nextSetBit(i + 1)) {
                        Document node = reader.document(i);
                        String parentIdentifier = node.get(FieldNames.PARENT);
                        String identifier = node.get(FieldNames.UUID);
                        try {
                            //NodeState state = (NodeState) itemMgr.getItemState(new NodeId(parentUUID));
                            NodeData state = (NodeData) itemMgr.getItemData(parentIdentifier);
                            if (nameTest == null) {
                                // only select this node if it is the child at
                                // specified position
                                if (position == LocationStepQueryNode.LAST) {
                                    // only select last
                                    //List childNodes = state.getChildNodeEntries();
                                    //List childNodes = state.getChildNodes();
                                    List c = getOrderedChildNodes(state);
                                    if (c.size() == 0
//                                            || !((NodeState.ChildNodeEntry)
                                                || !((NodeData) 
                                                c.get(c.size() - 1))
                                                .getIdentifier().equals(identifier)) {
                                        hits.flip(i);
                                    }
                                } else {
//                                    List childNodes = state.getChildNodeEntries();
                                    //List childNodes = state.getChildNodes();
                                    List c = getOrderedChildNodes(state);
                                    if (position < 1
                                            || c.size() < position
                                            || !((NodeData) 
//                                            || !((NodeState.ChildNodeEntry) 
                                                c.get(position - 1))
                                                .getIdentifier().equals(identifier)) {
                                        hits.flip(i);
                                    }
                                }
                            } else {
                                // select the node when its index is equal to
                                // specified position
                                if (position == LocationStepQueryNode.LAST) {
                                    // only select last
//                                    NodeState.ChildNodeEntry entry =
//                                            state.getChildNodeEntry(uuid);
                                    NodeData entry = null;
                                    List c = getOrderedChildNodes(state);
                                    for(Iterator it = c.iterator(); it.hasNext();) {
                                      NodeData d = (NodeData) it.next();
                                      if(d.getIdentifier().equals(identifier))
                                        entry = d;
                                    }

                                    if (entry == null) {
                                        // no such child node, probably deleted meanwhile
                                        hits.flip(i);
                                    } else {
                                        // only use the last one
                                        InternalQName name = entry.getQPath().getName();
//                                        List childNodes = state.getChildNodeEntries(name);
                                        List childNodes = new ArrayList();
                                        c = getOrderedChildNodes(state);
                                        for(Iterator it = c.iterator(); it.hasNext();) {
                                          NodeData d = (NodeData) it.next();
                                          if(d.getQPath().getName().equals(name))
                                            childNodes.add(d);
                                        }
                                        
                                        if (childNodes.size() == 0
  //                                              || !((NodeState.ChildNodeEntry)
                                                  || !((NodeData)
                                                    childNodes.get(childNodes.size() - 1))
                                                    .getIdentifier().equals(identifier)) {
                                            hits.flip(i);
                                        }
                                    }
                                } else {
//                                    NodeState.ChildNodeEntry entry =
//                                            state.getChildNodeEntry(uuid);
                                  NodeData entry = null;
                                  List c = getOrderedChildNodes(state);
                                  for(Iterator it = c.iterator(); it.hasNext();) {
                                    
                                    NodeData d = (NodeData) it.next();
                                    if(d.getIdentifier().equals(identifier))
                                      entry = d;
                                  }

                                    if (entry == null) {
                                        // no such child node, probably has been deleted meanwhile
                                        hits.flip(i);
                                    } else {
                                        if (entry.getQPath().getIndex() != position) {
                                            hits.flip(i);
                                        }
                                    }
                                }
                            }
                        } catch (RepositoryException e) {
                            // ignore this node, probably has been deleted meanwhile
                            hits.flip(i);
                        }
                    }
                }
            }
        }
        private List getOrderedChildNodes(NodeData node) throws RepositoryException {
          List c = itemMgr.getChildNodesData(node);
          Collections.sort(c, new NodeDatasOrderComparator());
          return c;
        }

    }
    public static class NodeDatasOrderComparator implements Comparator {
      public int compare(Object o1, Object o2) {
        NodeData n1 = (NodeData) o1;
        NodeData n2 = (NodeData) o2;
        //return n1.getQPath().getAsString().compareTo(n2.getQPath().getAsString());
        return n1.getOrderNumber() - n2.getOrderNumber();
      }
    }
    
}

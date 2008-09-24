/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

/**
 * <code>ParentAxisQuery</code> selects the parent nodes of a context query.
 */
class ParentAxisQuery extends Query {

  /**
   * Default score is 1.0f.
   */
  private static final Float DEFAULT_SCORE = new Float(1.0f);

  /**
   * The context query
   */
  private final Query        contextQuery;

  /**
   * The nameTest to apply on the parent axis, or <code>null</code> if any parent node should be
   * selected.
   */
  private final String       nameTest;

  /**
   * The scorer of the context query
   */
  private Scorer             contextScorer;

  /**
   * Creates a new <code>ParentAxisQuery</code> based on a <code>context</code> query.
   * 
   * @param context
   *          the context for this query.
   * @param nameTest
   *          a name test or <code>null</code> if any parent node is selected.
   */
  ParentAxisQuery(Query context, String nameTest) {
    this.contextQuery = context;
    this.nameTest = nameTest;
  }

  /**
   * Creates a <code>Weight</code> instance for this query.
   * 
   * @param searcher
   *          the <code>Searcher</code> instance to use.
   * @return a <code>ParentAxisWeight</code>.
   */
  protected Weight createWeight(Searcher searcher) {
    return new ParentAxisWeight(searcher);
  }

  /**
   * {@inheritDoc}
   */
  public void extractTerms(Set terms) {
    contextQuery.extractTerms(terms);
  }

  /**
   * {@inheritDoc}
   */
  public Query rewrite(IndexReader reader) throws IOException {
    Query cQuery = contextQuery.rewrite(reader);
    if (cQuery == contextQuery) {
      return this;
    } else {
      return new ParentAxisQuery(cQuery, nameTest);
    }
  }

  /**
   * Always returns 'ParentAxisQuery'.
   * 
   * @param field
   *          the name of a field.
   * @return 'ParentAxisQuery'.
   */
  public String toString(String field) {
    return "(ParentAxisQuery " + contextQuery.toString() + " nameTest:" + nameTest + ")";
  }

  // -----------------------< ParentAxisWeight >-------------------------------

  /**
   * The <code>Weight</code> implementation for this <code>ParentAxisQuery</code>.
   */
  private class ParentAxisWeight implements Weight {

    /**
     * The searcher in use
     */
    private final Searcher searcher;

    /**
     * Creates a new <code>ParentAxisWeight</code> instance using <code>searcher</code>.
     * 
     * @param searcher
     *          a <code>Searcher</code> instance.
     */
    private ParentAxisWeight(Searcher searcher) {
      this.searcher = searcher;
    }

    /**
     * Returns this <code>ParentAxisQuery</code>.
     * 
     * @return this <code>ParentAxisQuery</code>.
     */
    public Query getQuery() {
      return ParentAxisQuery.this;
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
     * Creates a scorer for this <code>ParentAxisQuery</code>.
     * 
     * @param reader
     *          a reader for accessing the index.
     * @return a <code>ParentAxisScorer</code>.
     * @throws IOException
     *           if an error occurs while reading from the index.
     */
    public Scorer scorer(IndexReader reader) throws IOException {
      contextScorer = contextQuery.weight(searcher).scorer(reader);
      HierarchyResolver resolver = (HierarchyResolver) reader;
      return new ParentAxisScorer(searcher.getSimilarity(), reader, resolver);
    }

    /**
     * {@inheritDoc}
     */
    public Explanation explain(IndexReader reader, int doc) throws IOException {
      return new Explanation();
    }
  }

  // --------------------------< ParentAxisScorer >----------------------------

  /**
   * Implements a <code>Scorer</code> for this <code>ParentAxisQuery</code>.
   */
  private class ParentAxisScorer extends Scorer {

    /**
     * An <code>IndexReader</code> to access the index.
     */
    private final IndexReader       reader;

    /**
     * The <code>HierarchyResolver</code> of the index.
     */
    private final HierarchyResolver hResolver;

    /**
     * BitSet storing the id's of selected documents
     */
    private BitSet                  hits;

    /**
     * Map that contains the scores from matching documents from the context query. To save memory
     * only scores that are not equal to 1.0f are put to this map. <p/> key=[Integer] id of selected
     * document from context query<br>
     * value=[Float] score for that document
     */
    private final Map               scores  = new HashMap();

    /**
     * The next document id to return
     */
    private int                     nextDoc = -1;

    /**
     * Creates a new <code>ParentAxisScorer</code>.
     * 
     * @param similarity
     *          the <code>Similarity</code> instance to use.
     * @param reader
     *          for index access.
     */
    protected ParentAxisScorer(Similarity similarity, IndexReader reader, HierarchyResolver resolver) {
      super(similarity);
      this.reader = reader;
      this.hResolver = resolver;
    }

    /**
     * {@inheritDoc}
     */
    public boolean next() throws IOException {
      calculateParent();
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
      Float score = (Float) scores.get(new Integer(nextDoc));
      if (score == null) {
        score = DEFAULT_SCORE;
      }
      return score.floatValue();
    }

    /**
     * {@inheritDoc}
     */
    public boolean skipTo(int target) throws IOException {
      calculateParent();
      nextDoc = hits.nextSetBit(target);
      return nextDoc > -1;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     *           this implementation always throws an <code>UnsupportedOperationException</code>.
     */
    public Explanation explain(int doc) throws IOException {
      throw new UnsupportedOperationException();
    }

    private void calculateParent() throws IOException {
      if (hits == null) {
        hits = new BitSet(reader.maxDoc());

        final IOException[] ex = new IOException[1];
        contextScorer.score(new HitCollector() {
          public void collect(int doc, float score) {
            try {
              doc = hResolver.getParent(doc);
              if (doc != -1) {
                hits.set(doc);
                if (score != DEFAULT_SCORE.floatValue()) {
                  scores.put(new Integer(doc), new Float(score));
                }
              }
            } catch (IOException e) {
              ex[0] = e;
            }
          }
        });

        if (ex[0] != null) {
          throw ex[0];
        }

        // filter out documents that do not match the name test
        if (nameTest != null) {
          TermDocs tDocs = reader.termDocs(new Term(FieldNames.LABEL, nameTest));
          try {
            for (int i = hits.nextSetBit(0); i >= 0; i = hits.nextSetBit(i + 1)) {
              if (!tDocs.skipTo(i)) {
                hits.clear(i);
              }
            }
          } finally {
            tDocs.close();
          }
        }
      }
    }
  }
}

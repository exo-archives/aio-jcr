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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

/**
 * This class implements the Weight calculation for the MatchAllQuery.
 */
class MatchAllWeight implements Weight {

    /**
     * the MatchAllQuery
     */
    private final Query query;

    private final String field;

    /**
     * the current Searcher instance
     */
    private final Searcher searcher;

    /**
     * the weight value
     */
    private float value;

    /**
     * doc frequency for this weight
     */
    private float idf;

    /**
     * the query weight
     */
    private float queryWeight;

    /**
     * @param query
     * @param searcher
     */
    MatchAllWeight(Query query, Searcher searcher, String field) {
        this.query = query;
        this.searcher = searcher;
        this.field = field;
    }

    /**
     * {@inheritDoc}
     */
    public Query getQuery() {
        return query;
    }

    /**
     * {@inheritDoc}
     */
    public float getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public float sumOfSquaredWeights() throws IOException {
        idf = searcher.getSimilarity().idf(searcher.maxDoc(), searcher.maxDoc()); // compute idf
        queryWeight = idf * 1.0f; // boost         // compute query weight
        return queryWeight * queryWeight;           // square it
    }

    /**
     * {@inheritDoc}
     */
    public void normalize(float queryNorm) {
        queryWeight *= queryNorm;                   // normalize query weight
        value = queryWeight * idf;                  // idf for document
    }

    /**
     * {@inheritDoc}
     */
    public Scorer scorer(IndexReader reader) throws IOException {
        return new MatchAllScorer(reader, this, field);
    }

    /**
     * {@inheritDoc}
     */
    public Explanation explain(IndexReader reader, int doc) throws IOException {
        return new Explanation(Similarity.getDefault().idf(reader.maxDoc(), reader.maxDoc()),
                "matchAll");
    }
}

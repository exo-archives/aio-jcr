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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;

/**
 * Wraps the lucene <code>Hits</code> object and adds a close method that allows
 * to release resources after a query has been executed and the results have
 * been read completely.
 */
class QueryHits {

    /**
     * The lucene hits we wrap.
     */
    private final Hits hits;

    /**
     * The IndexReader in use by the lucene hits.
     */
    private final IndexReader reader;

    /**
     * Number of results.
     */
    private final int length;

    /**
     * Creates a new <code>QueryHits</code> instance wrapping <code>hits</code>.
     * @param hits the lucene hits.
     * @param reader the IndexReader in use by <code>hits</code>.
     */
    QueryHits(Hits hits, IndexReader reader) {
        this.hits = hits;
        this.reader = reader;
        this.length = hits.length();
    }

    /**
     * Releases resources held by this hits instance.
     *
     * @throws IOException if an error occurs while releasing resources.
     */
    public final void close() throws IOException {
        reader.close();
    }

    /**
     * Returns the number of results.
     * @return the number of results.
     */
    public final int length() {
      return length;
    }

    /**
     * Returns the <code>n</code><sup>th</sup> document in this QueryHits.
     *
     * @param n index.
     * @return the <code>n</code><sup>th</sup> document in this QueryHits.
     * @throws IOException if an error occurs while reading from the index.
     */
    public final Document doc(int n) throws IOException {
        return hits.doc(n);
    }

    /**
     * Returns the score for the <code>n</code><sup>th</sup> document in this
     * QueryHits.
     * @param n index.
     * @return the score for the <code>n</code><sup>th</sup> document.
     */
    public final float score(int n) throws IOException {
      return hits.score(n);
    }
}

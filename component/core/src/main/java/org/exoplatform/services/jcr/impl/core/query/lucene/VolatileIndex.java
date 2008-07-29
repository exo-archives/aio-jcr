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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;

/**
 * Implements an in-memory index with a pending buffer.
 */
class VolatileIndex extends AbstractIndex {

    /**
     * Default value for {@link #bufferSize}.
     */
    private static final int DEFAULT_BUFFER_SIZE = 10;

    /**
     * Map of pending documents to add to the index
     */
    private final Map<String, Document> pending = new LinkedHashMap<String, Document>();

    /**
     * Number of documents that are buffered before they are added to the index.
     */
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * The number of documents in this index.
     */
    private int numDocs = 0;

    /**
     * Creates a new <code>VolatileIndex</code> using an <code>analyzer</code>.
     *
     * @param analyzer the analyzer to use.
     * @param indexingQueue the indexing queue.
     * @throws IOException if an error occurs while opening the index.
     */
    VolatileIndex(Analyzer analyzer, IndexingQueue indexingQueue) throws IOException {
        super(analyzer, new RAMDirectory(), null, indexingQueue);
    }

    /**
     * Overwrites the default implementation by adding the documents to a
     * pending list and commits the pending list if needed.
     *
     * @param docs the documents to add to the index.
     * @throws IOException if an error occurs while writing to the index.
     */
    void addDocuments(Document[] docs) throws IOException {
        for (int i = 0; i < docs.length; i++) {
            Document old = pending.put(docs[i].get(FieldNames.UUID), docs[i]);
            if (old != null) {
                Util.disposeDocument(old);
            }
            if (pending.size() >= bufferSize) {
                commitPending();
            }
            numDocs++;
        }
        invalidateSharedReader();
    }

    /**
     * Overwrites the default implementation to remove the document from the
     * pending list if it is present or simply calls <code>super.removeDocument()</code>.
     *
     * @param idTerm the uuid term of the document to remove.
     * @return the number of deleted documents
     * @throws IOException if an error occurs while removing the document from
     *                     the index.
     */
    int removeDocument(Term idTerm) throws IOException {
        Document doc = pending.remove(idTerm.text());
        int num;
        if (doc != null) {
            Util.disposeDocument(doc);
            // pending document has been removed
            num = 1;
        } else {
            // remove document from index
            num = super.getIndexReader().deleteDocuments(idTerm);
        }
        numDocs -= num;
        return num;
    }

    /**
     * Returns the number of valid documents in this index.
     *
     * @return the number of valid documents in this index.
     */
    int getNumDocuments() throws IOException {
        return numDocs;
    }

    /**
     * Overwrites the implementation in {@link AbstractIndex} to trigger
     * commit of pending documents to index.
     * @return the index reader for this index.
     * @throws IOException if an error occurs building a reader.
     */
    protected synchronized CommittableIndexReader getIndexReader() throws IOException {
        commitPending();
        return super.getIndexReader();
    }

    /**
     * Overwrites the implementation in {@link AbstractIndex} to commit
     * pending documents.
     * @param optimize if <code>true</code> the index is optimized after the
     *                 commit.
     */
    protected synchronized void commit(boolean optimize) throws IOException {
        commitPending();
        super.commit(optimize);
    }

    /**
     * Sets a new buffer size for pending documents to add to the index.
     * Higher values consume more memory, but help to avoid multiple index
     * cycles when a node is changed / saved multiple times.
     *
     * @param size the new buffer size.
     */
    void setBufferSize(int size) {
        bufferSize = size;
    }

    /**
     * Commits pending documents to the index.
     */
    private void commitPending() throws IOException {
      super.addDocuments(pending.values().toArray(new Document[pending.size()]));
    pending.clear();
    }
}

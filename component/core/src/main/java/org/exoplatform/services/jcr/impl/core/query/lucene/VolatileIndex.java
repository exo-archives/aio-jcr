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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements an in-memory index with a redo log.
 */
class VolatileIndex extends AbstractIndex {

    /**
     * Logger instance for this class.
     */
    private static Log log = ExoLogger.getLogger("jcr.VolatileIndex");

    /**
     * Default value for {@link #bufferSize}.
     */
    private static final int DEFAULT_BUFFER_SIZE = 10;

    /** Map of pending documents to add to the index */
    private final Map pending = new LinkedMap();

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
     * @throws IOException if an error occurs while opening the index.
     */
    VolatileIndex(Analyzer analyzer) throws IOException {
        super(analyzer, new RAMDirectory(), null);
    }

    /**
     * Overwrites the default implementation by writing an entry to the
     * redo log and then adds it to the pending list.
     * @param doc the document to add to the index.
     * @throws IOException if an error occurs while writing to the redo log
     * or the index.
     */
    void addDocument(Document doc) throws IOException {
       Document old = (Document) pending.put(doc.get(FieldNames.UUID), doc);
        if (old != null) {
            disposeDocument(old);
        }
        if (pending.size() >= bufferSize) {
            commitPending();
        }
        invalidateSharedReader();
        numDocs++;
    }

    /**
     * Overwrites the default implementation by writing an entry to the redo
     * log and then calling the <code>super.removeDocument()</code> method or
     * if the document is in the pending list, removes it from there.
     *
     * @param idTerm the uuid term of the document to remove.
     * @throws IOException if an error occurs while writing to the redo log
     * or the index.
     * @return the number of deleted documents
     */
    int removeDocument(Term idTerm) throws IOException {
        Document doc = (Document) pending.remove(idTerm.text());
        int num;
        if (doc != null) {
            disposeDocument(doc);
            // pending document has been removed
            num = 1;
        } else {
            // remove document from index
            num = super.getIndexReader().delete(idTerm);
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
    protected synchronized IndexReader getIndexReader() throws IOException {
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
        for (Iterator it = pending.values().iterator(); it.hasNext();) {
            Document doc = (Document) it.next();
            super.addDocument(doc);
            it.remove();
        }
    }

    /**
     * Disposes the document <code>old</code>. Closes any potentially open
     * readers held by the document.
     *
     * @param old the document to dispose.
     */
    private void disposeDocument(Document old) {
        for (Enumeration e = old.fields(); e.hasMoreElements(); ) {
            Field f = (Field) e.nextElement();
            if (f.readerValue() != null) {
                try {
                    f.readerValue().close();
                } catch (IOException ex) {
                    log.warn("Exception while disposing index document: " + ex);
                }
            }
        }
    }
}

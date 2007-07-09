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
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

/**
 * Extends a <code>MultiReader</code> with support for cached <code>TermDocs</code>
 * on {@link FieldNames#UUID} field.
 */
final class CachingMultiReader extends MultiReader
implements HierarchyResolver {

    /**
     * The sub readers.
     */
    private ReadOnlyIndexReader[] subReaders;

    /**
     * Map of OffsetReaders, identified by caching reader they are based on.
     */
    private final Map readersByBase = new IdentityHashMap();

    /**
     * Document number cache if available. May be <code>null</code>.
     */
    private final DocNumberCache cache;

    /**
     * Doc number starts for each sub reader
     */
    private int[] starts;

    /**
     * Reference count. Every time close is called refCount is decremented. If
     * refCount drops to zero the underlying readers are closed as well.
     */
    private int refCount = 1;

    /**
     * Creates a new <code>CachingMultiReader</code> based on sub readers.
     *
     * @param subReaders the sub readers.
     * @param cache the document number cache.
     * @throws IOException if an error occurs while reading from the indexes.
     */
    public CachingMultiReader(ReadOnlyIndexReader[] subReaders,
                              DocNumberCache cache)
            throws IOException {
        super(subReaders);
        this.cache = cache;
        this.subReaders = subReaders;
        starts = new int[subReaders.length + 1];
        int maxDoc = 0;
        for (int i = 0; i < subReaders.length; i++) {
            starts[i] = maxDoc;
            maxDoc += subReaders[i].maxDoc();
            OffsetReader offsetReader = new OffsetReader(subReaders[i], starts[i]);
            readersByBase.put(subReaders[i].getBase().getBase(), offsetReader);
        }
        starts[subReaders.length] = maxDoc;
    }

    /**
     * Returns the document number of the parent of <code>n</code> or
     * <code>-1</code> if <code>n</code> does not have a parent (<code>n</code>
     * is the root node).
     *
     * @param n the document number.
     * @return the document number of <code>n</code>'s parent.
     * @throws IOException if an error occurs while reading from the index.
     */
    final public int getParent(int n) throws IOException {
        int i = readerIndex(n);
        DocId id = subReaders[i].getParent(n - starts[i]);
        id = id.applyOffset(starts[i]);
        return id.getDocumentNumber(this);
    }

    /**
     * Returns the DocId of the parent of <code>n</code> or {@link DocId#NULL}
     * if <code>n</code> does not have a parent (<code>n</code> is the root
     * node).
     *
     * @param n the document number.
     * @return the DocId of <code>n</code>'s parent.
     * @throws IOException if an error occurs while reading from the index.
     */
    public DocId getParentDocId(int n) throws IOException {
        int i = readerIndex(n);
        DocId id = subReaders[i].getParent(n - starts[i]);
        return id.applyOffset(starts[i]);
    }

    /**
     * {@inheritDoc}
     */
    public TermDocs termDocs(Term term) throws IOException {
        if (term.field() == FieldNames.UUID) {
            // check cache
            DocNumberCache.Entry e = cache.get(term.text());
            if (e != null) {
                // check if valid:
                // 1) reader must be in the set of readers
                // 2) doc must not be deleted
                OffsetReader offsetReader = (OffsetReader) readersByBase.get(e.reader);
                if (offsetReader != null && !offsetReader.reader.isDeleted(e.doc)) {
                    return new SingleTermDocs(e.doc + offsetReader.offset);
                }
            }

            // if we get here, entry is either invalid or did not exist
            // search through readers
            for (int i = 0; i < subReaders.length; i++) {
                TermDocs docs = subReaders[i].termDocs(term);
                try {
                    if (docs.next()) {
                        return new SingleTermDocs(docs.doc() + starts[i]);
                    }
                } finally {
                    docs.close();
                }
            }
        }

        return super.termDocs(term);
    }

    /**
     * Increments the reference count of this reader. Each call to this method
     * must later be acknowledged by a call to {@link #close()}
     */
    synchronized void incrementRefCount() {
        refCount++;
    }

    /**
     * Decrements the reference count and closes the underlying readers if this
     * reader is not in use anymore.
     * @throws IOException if an error occurs while closing this reader.
     */
    protected synchronized void doClose() throws IOException {
        if (--refCount == 0) {
            super.doClose();
        }
    }

    //------------------------< internal >--------------------------------------

    /**
     * Returns the reader index for document <code>n</code>.
     * Implementation copied from lucene MultiReader class.
     *
     * @param n document number.
     * @return the reader index.
     */
    final private int readerIndex(int n) {
        int lo = 0;                                      // search starts array
        int hi = subReaders.length - 1;                  // for first element less

        while (hi >= lo) {
            int mid = (lo + hi) >> 1;
            int midValue = starts[mid];
            if (n < midValue) {
                hi = mid - 1;
            } else if (n > midValue) {
                lo = mid + 1;
            } else {                                      // found a match
                while (mid + 1 < subReaders.length && starts[mid + 1] == midValue) {
                    mid++;                                  // scan to last match
                }
                return mid;
            }
        }
        return hi;
    }

    //-----------------------< OffsetTermDocs >---------------------------------

    /**
     * Simple helper struct that associates an offset with an IndexReader.
     */
    private static final class OffsetReader {

        /**
         * The index reader.
         */
        final ReadOnlyIndexReader reader;

        /**
         * The reader offset in this multi reader instance.
         */
        final int offset;

        /**
         * Creates a new <code>OffsetReader</code>.
         *
         * @param reader the index reader.
         * @param offset the reader offset in a multi reader.
         */
        OffsetReader(ReadOnlyIndexReader reader, int offset) {
            this.reader = reader;
            this.offset = offset;
        }
    }
}

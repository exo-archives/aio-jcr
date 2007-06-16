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

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements a Document number cache with a fixed size and a LRU strategy.
 */
final class DocNumberCache {

    /**
     * Logger instance for this class.
     */
    private static Log log = ExoLogger.getLogger("jcr.DocNumberCache");

    /**
     * Log cache statistics at most every 10 seconds.
     */
    private static final long LOG_INTERVAL = 1000 * 10;

    /**
     * LRU Map where key=identifier value=reader;docNumber
     */
    private final LRUMap docNumbers;

    /**
     * Timestamp of the last cache statistics log.
     */
    private long lastLog;

    /**
     * Cache misses.
     */
    private long misses;

    /**
     * Cache accesses;
     */
    private long accesses;

    /**
     * Creates a new <code>DocNumberCache</code> with a limiting
     * <code>size</code>.
     *
     * @param size the cache limit.
     */
    DocNumberCache(int size) {
        docNumbers = new LRUMap(size);
    }

    /**
     * Puts a document number into the cache using a identifier as key. An entry is
     * only overwritten if the according reader is younger than the reader
     * associated with the existing entry.
     *
     * @param identifier the key.
     * @param reader the index reader from where the document number was read.
     * @param n the document number.
     */
    synchronized void put(String identifier, CachingIndexReader reader, int n) {
        Entry e = (Entry) docNumbers.get(identifier);
        if (e != null) {
            // existing entry
            // ignore if reader is older than the one in entry
            if (reader.getCreationTick() <= e.reader.getCreationTick()) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring put(). New entry is not from a newer reader. " +
                            "existing: " + e.reader.getCreationTick() +
                            ", new: " + reader.getCreationTick());
                }
                e = null;
            }
        } else {
            // entry did not exist
            e = new Entry(reader, n);
        }

        if (e != null) {
            docNumbers.put(identifier, e);
        }
    }

    /**
     * Returns the cache entry for <code>identifier</code>, or <code>null</code> if
     * no entry exists for <code>identifier</code>.
     *
     * @param identifier the key.
     * @return cache entry or <code>null</code>.
     */
    synchronized Entry get(String identifier) {
        Entry entry = (Entry) docNumbers.get(identifier);
        if (log.isInfoEnabled()) {
            accesses++;
            if (entry == null) {
                misses++;
            }
            // log at most after 1000 accesses and every 10 seconds
            if (accesses > 1000 && System.currentTimeMillis() - lastLog > LOG_INTERVAL) {
                long ratio = 100;
                if (misses != 0) {
                    ratio -= misses * 100L / accesses;
                }
                StringBuffer statistics = new StringBuffer();
                statistics.append("size=").append(docNumbers.size());
                statistics.append("/").append(docNumbers.maxSize());
                statistics.append(", #accesses=").append(accesses);
                statistics.append(", #hits=").append((accesses - misses));
                statistics.append(", #misses=").append(misses);
                statistics.append(", cacheRatio=").append(ratio).append("%");
                log.info(statistics);
                accesses = 0;
                misses = 0;
                lastLog = System.currentTimeMillis();
            }
        }
        return entry;
    }

    public static final class Entry {

        /**
         * The IndexReader.
         */
        final CachingIndexReader reader;

        /**
         * The document number.
         */
        final int doc;

        Entry(CachingIndexReader reader, int doc) {
            this.reader = reader;
            this.doc = doc;
        }
    }
}

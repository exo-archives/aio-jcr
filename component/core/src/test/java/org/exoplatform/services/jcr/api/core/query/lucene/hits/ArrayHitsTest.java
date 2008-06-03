package org.exoplatform.services.jcr.api.core.query.lucene.hits;

import org.exoplatform.services.jcr.impl.core.query.lucene.hits.ArrayHits;

import junit.framework.TestCase;

public class ArrayHitsTest extends TestCase {

    public void testSkipToDocumentNumberGreaterThanLastMatch() throws Exception {
        ArrayHits hits = new ArrayHits();
        hits.set(1);
        int doc = hits.skipTo(2);
        assertEquals(-1, doc);
    }
    
}

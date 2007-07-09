/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class EntityMetadataTest extends TestCase {
  
  public void testMetadataLang() {
    List<String> langs = new ArrayList <String> ();
    langs.add("en");
    langs.add("ru");
    langs.add("da");
    langs.add("de");
    MultivaluedMetadata headers = new MultivaluedMetadata();
    headers.put("Content-Language", langs);
    EntityMetadata md = new EntityMetadata(headers);
    assertEquals("en,ru,da,de", md.getLanguages());
  }

  public void testMetadataEncod() {
    List<String> encs = new ArrayList <String> ();
    encs.add("compress;q=0.5");
    encs.add("gzip;q=1.0");
    MultivaluedMetadata headers = new MultivaluedMetadata();
    headers.put("Content-Encoding", encs);
    EntityMetadata md = new EntityMetadata(headers);
    assertEquals("compress;q=0.5,gzip;q=1.0", md.getEncodings());
  }

}

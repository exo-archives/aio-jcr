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
public class MultivaluedMetadataTest extends TestCase {
  
  public void testMetadataLang() {
    MultivaluedMetadata md = new MultivaluedMetadata();
    List<String> langs = new ArrayList <String> ();
    langs.add("en");
    langs.add("ru");
    langs.add("da");
    langs.add("de");
    md.put("Content-Language",langs);
    assertEquals("en, ru, da, de", md.getAll().get("content-language"));
  }

  public void testMetadataEncod() {
    MultivaluedMetadata md = new MultivaluedMetadata();
    List<String> encs = new ArrayList <String> ();
    encs.add("compress;q=0.5");
    encs.add("gzip;q=1.0");
    md.put("Content-Encoding", encs);
    assertEquals("compress;q=0.5, gzip;q=1.0", md.getAll().get("content-encoding"));
  }

}

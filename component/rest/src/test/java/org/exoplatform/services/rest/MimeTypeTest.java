/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import junit.framework.TestCase;
import org.exoplatform.services.rest.data.MimeTypes;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MimeTypeTest extends TestCase {
	
	MimeTypes mt;
	public void setUp() {
		mt = new MimeTypes("image/jpeg;q=0.8,image/gif;q=0.7,image/png;q=0.9");
	}
	
	public void testMimeTypes() {
		assertTrue(mt.hasMimeType("image/jpeg"));
		assertTrue(mt.hasMimeType("image/gif"));
		assertTrue(mt.hasMimeType("image/png"));
		assertFalse(mt.hasMimeType("image/jpg"));
		assertEquals("image/png", mt.getMimeType(0));
		for(String t : mt.getMimeTypes())
			System.out.print(t + ",");
	}

}

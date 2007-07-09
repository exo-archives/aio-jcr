/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

import junit.framework.TestCase;
import org.exoplatform.services.rest.data.HeaderUtils;

public class HeaderUtilTests extends TestCase{
	
  public void testHeaderNoremalizeAcceptString() {
    String t = HeaderUtils.normalizeAccepString("text/plain;    level= 2;  q=0.8,text/xml;   q=0.9");
    assertEquals("text/plain;level=2;q=0.8,text/xml;q=0.9", t);
  }

	public void testHeaderParse() {
 		String[] acc = HeaderUtils.parse("image/jpeg,  text/xml  ;level=1;            q=0.7," +
				"text/plain;          q=0.95, text/html;q=0.8,text/x-c; q=0.75," +
				"                    text/xbel+xml;   q=0.9");
		assertEquals("image/jpeg", acc[0]);
		assertEquals("text/plain", acc[1]);
    assertEquals("text/xbel+xml", acc[2]);
    assertEquals("text/html", acc[3]);
    assertEquals("text/x-c", acc[4]);
		assertEquals("text/xml;level=1",acc[5]);
	}

}

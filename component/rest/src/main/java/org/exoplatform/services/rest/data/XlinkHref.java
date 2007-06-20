/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.data;

import java.lang.reflect.Method;
import java.net.URI;

import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class XlinkHref {

  private static final String XLINK_HREF = "xlinks:href";
  private static final String XLINK_NAMESPACE_URL = "http://www.w3c.org/1999/xlink";

  private URI uri;
  
  public XlinkHref(URI baseURI, Method server) {
    // create uri here
    //uri =  
  }

  public final URI getURI() {
    return uri;
  }
  
  public void putToElement(Element parent) {
    parent.setAttributeNS(XLINK_NAMESPACE_URL, XLINK_HREF, uri.toASCIIString());
  }
}

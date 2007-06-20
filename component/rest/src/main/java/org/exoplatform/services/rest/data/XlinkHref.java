/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.data;

import java.lang.reflect.Method;
import org.w3c.dom.Element;

import org.exoplatform.services.rest.URITemplate;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class XlinkHref {

  private static final String XLINK_HREF = "xlinks:href";
  private static final String XLINK_NAMESPACE_URL = "http://www.w3c.org/1999/xlink";

  private String uri;
  
  public XlinkHref(String baseURI, Method server) {
    URITemplate clazzURIAnno = server.getClass().getAnnotation(URITemplate.class);
    URITemplate methodURIAnno = server.getAnnotation(URITemplate.class);
    String clazzURI = (clazzURIAnno == null) ? "" : clazzURIAnno.value();
    String methodURI = (methodURIAnno == null) ? "" : methodURIAnno.value();
    if(clazzURI.endsWith("/") && methodURI.startsWith("/"))
      this.uri = baseURI + clazzURI + methodURI.replaceFirst("/", "");
    else if(!clazzURI.endsWith("/") && !methodURI.startsWith("/") && !"".equals(methodURI))
      this.uri = baseURI + clazzURI + "/" + methodURI;
    else
      this.uri = baseURI + clazzURI + methodURI;
  }

  public final String getURI() {
    return uri;
  }
  
  public void putToElement(Element parent) {
    parent.setAttributeNS(XLINK_NAMESPACE_URL, XLINK_HREF, uri);
  }
  
}

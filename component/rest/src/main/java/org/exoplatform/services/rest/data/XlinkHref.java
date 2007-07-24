/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.data;

import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL.<br/>
 * Can add xlink:href into given Element of DOM structure.<br/>
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class XlinkHref {

  private static final String XLINK_HREF = "xlinks:href";
  private static final String XLINK_NAMESPACE_URL = "http://www.w3c.org/1999/xlink";

  private String uri;
  
  /**
   * @param u new uri
   */
  public XlinkHref(String u) {
    this.uri = u;
  }

  /**
   * Get uri.
   * @return uri current uri
   */
  public final String getURI() {
    return uri;
  }
  
  /**
   * Add xlink to given element of DOM structure.
   * @param parent element
   */
  public void putToElement(Element parent) {
    parent.setAttributeNS(XLINK_NAMESPACE_URL, XLINK_HREF, uri);
  }
  
  /**
   * Add external suffix to uri an d then insert xlink into element of DOM.
   * @param parent element
   * @param extURI external suffix for uri
   */
  public void putToElement(Element parent, String extURI) {
    parent.setAttributeNS(XLINK_NAMESPACE_URL, XLINK_HREF, uri + extURI);
  }

}

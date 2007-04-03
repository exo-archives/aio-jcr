/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.search;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class XPathQuery implements DavQuery {

  
  private String query;
  
  public XPathQuery() {
  }
  
  public void setQuery(String query) {
    this.query = query;
  }
  
  public Element toXml(Document xmlDocument) {    
    Element xPathElement = xmlDocument.createElementNS(SearchConst.XPATH_NAMESPACE,
        SearchConst.XPATH_PREFIX + "xpath");
    xPathElement.setTextContent(query);
    return xPathElement;
  }
    
}

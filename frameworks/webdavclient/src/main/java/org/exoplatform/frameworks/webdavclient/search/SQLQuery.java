/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.search;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SQLQuery implements DavQuery {
  
  private String query;
  
  public SQLQuery() {
  }
  
  public void setQuery(String query) {
    this.query = query;
  }
  
  public Element toXml(Document xmlDocument) {
    Element sqlElement = xmlDocument.createElementNS(SearchConst.SQL_NAMESPACE,
        SearchConst.SQL_PREFIX + "sql");
    sqlElement.setTextContent(query);
    return sqlElement;
  }
  
}

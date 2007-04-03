/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.commands;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.WebDavContext;
import org.exoplatform.frameworks.davclient.search.DavQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavSearch extends MultistatusCommand {
  
  private DavQuery query; 
  
  public DavSearch(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.SEARCH;    
  }
  
  public void setQuery(DavQuery query) {
    this.query = query;
  }
  
  public Element toXml(Document xmlDocument) {
    Element searchRequestEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE,
        Const.Dav.PREFIX + Const.StreamDocs.SEARCHREQUEST);    
    xmlDocument.appendChild(searchRequestEl);
    
    searchRequestEl.appendChild(query.toXml(xmlDocument));
    
    return searchRequestEl;
  }
  
}

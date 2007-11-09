/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.search.DavQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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

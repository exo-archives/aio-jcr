/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.request;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.exoplatform.services.webdav.search.Search;
import org.exoplatform.services.webdav.search.SearchConst;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SearchRequestDoc.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class SearchRequestDocument implements RequestDocument {
  
  private static Log log = ExoLogger.getLogger("jcr.SearchRequestDoc");
  
  public static final String SEARCHREQUEST = "searchrequest";

  private Search search;
  
  public Search getSearch() {
    return search;
  }
  
  private Search getSearch(String name) throws Exception {
    for (int i = 0; i < SearchConst.SEARCH_TEMPLATES.length; i++) {
      String curSearchName = SearchConst.SEARCH_TEMPLATES[i][0];
      if (curSearchName.equals(name)) {
        return (Search)Class.forName(SearchConst.SEARCH_TEMPLATES[i][1]).newInstance();
      }
    }
    return null;
  }
  
  public String getDocumentName() {
    return SEARCHREQUEST;
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {
    try {
      Node searchRequestN = DavUtil.getChildNode(requestDocument, getDocumentName());
      
      NodeList nodes = searchRequestN.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node searchNode = nodes.item(i);
        
        if (searchNode.getLocalName() != null) {        	
          search = getSearch(searchNode.getLocalName());
          search.init(searchNode);
          break;
        }
      }

    } catch (Throwable exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
      return false;
    }
    
    return true;        
  }

}

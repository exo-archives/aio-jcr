/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.representation;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SearchResultRepresentationFactory {
  
  public static final String XML_SEARCHREQUEST = "searchrequest";
  
  public static XmlResponseRepresentation createSearchResultRepresentation(WebDavService webDavService, Document document, Item node, String href) 
      throws RepositoryException, BadRequestException {

    Node searchRequest = DavUtil.getChildNode(document, XML_SEARCHREQUEST);
    
    Node sqlNode = DavUtil.getChildNode(searchRequest, "sql");    
    if (sqlNode != null) {      
      String query = sqlNode.getTextContent();      
      return new QuerySearchResultRepresentation(webDavService, href, (javax.jcr.Node)node, query, "sql");
    }

    Node xPathNode = DavUtil.getChildNode(searchRequest, "xpath");
    
    if (xPathNode != null) {
      String query = xPathNode.getTextContent();
      return new QuerySearchResultRepresentation(webDavService, href, (javax.jcr.Node)node, query, "xpath");
    }
    
    throw new BadRequestException();
  }

}

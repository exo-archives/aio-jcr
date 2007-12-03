/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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

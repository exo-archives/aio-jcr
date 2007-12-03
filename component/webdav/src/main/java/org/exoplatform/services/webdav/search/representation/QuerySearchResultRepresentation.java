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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.read.AllPropResponseRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class QuerySearchResultRepresentation extends AllPropResponseRepresentation {
  
  private QueryResult queryResult;
  
  public QuerySearchResultRepresentation(WebDavService webDavService, String href, Node node, String sqlQuery, String queryLanguage) throws RepositoryException {
    super(webDavService, href, node, 0);
    
    QueryManager queryManager = node.getSession().getWorkspace().getQueryManager();    
    Query query = queryManager.createQuery(sqlQuery, queryLanguage);
    queryResult = query.execute();
  }
  
  @Override
  protected void listRecursive(XMLStreamWriter xmlStreamWriter, Node curNode, int curDepth) throws XMLStreamException, RepositoryException {
    NodeIterator nodeIter = queryResult.getNodes();
    while (nodeIter.hasNext()) {
      
      Node nextNode = nodeIter.nextNode();
      
      if (nextNode.isNodeType(DavConst.NodeTypes.NT_RESOURCE)) {
        if (nextNode.getParent().isNodeType(DavConst.NodeTypes.NT_FILE)) {
          nextNode = nextNode.getParent();
        } else {
          // skipping
          continue;
        }
      }
      
      xmlStreamWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
      new HrefRepresentation(defaultHref + nextNode.getPath()).write(xmlStreamWriter);      
      writeResponseContent(xmlStreamWriter, nextNode);      
      xmlStreamWriter.writeEndElement();
    }    
  }  

}

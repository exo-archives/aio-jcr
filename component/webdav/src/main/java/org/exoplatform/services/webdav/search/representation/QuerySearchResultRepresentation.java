/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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
        }
      }
      
      xmlStreamWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
      new HrefRepresentation(defaultHref + nextNode.getPath()).write(xmlStreamWriter);      
      writeResponseContent(xmlStreamWriter, nextNode);      
      xmlStreamWriter.writeEndElement();
    }    
  }  

}

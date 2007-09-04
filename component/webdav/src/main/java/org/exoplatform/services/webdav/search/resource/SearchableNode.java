/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.resource;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.request.documents.PropFindDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.common.response.ResponseBuilder;
import org.exoplatform.services.webdav.search.Search;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SearchableNode.java 12883 2007-02-19 16:39:06Z gavrikvetal $
 */

public class SearchableNode extends AbstractNodeResource implements SearchableResource {
  
  private static Log log = ExoLogger.getLogger("jcr.SearchableNode");
  
  private String searchablePath;
  
  public SearchableNode(
      WebDavService webDavService,
      String rootHref,
      Node resourceNode
      ) throws RepositoryException {
    super(webDavService, rootHref, resourceNode);    
    searchablePath = resourceNode.getPath();
  }

  public ArrayList<MultiStatusResponse> doSearch(Search search) throws RepositoryException {
    QueryManager queryManager = null;
    
    try {
      queryManager = getNode().getSession().getWorkspace().getQueryManager();      
    } catch (RepositoryException rexc) {
      rexc.printStackTrace();
      return new ArrayList<MultiStatusResponse>();
    }
        
    log.info("SQL query: " + search.getQuery());
    
    Query query = queryManager.createQuery(search.getQuery(), search.getQueryLanguage());        
    QueryResult result = query.execute();
    
    NodeIterator nodeIter = result.getNodes();

    PropFindDocument propFindDoc = new PropFindDocument();
    propFindDoc.initFactory(webDavService.getConfig().getPropertyFactory());
    
    ArrayList<MultiStatusResponse> responses = new ArrayList<MultiStatusResponse>();
    
    while (nodeIter.hasNext()) {
      Node curNode = nodeIter.nextNode();
      
      if (!isSameNode(curNode)) {
        //log.info("Cutting... " + curNode.getPath());
        continue;
      }
      
      if (DavConst.NodeTypes.JCR_CONTENT.equals(curNode.getName()) &&
          curNode.getParent().isNodeType(DavConst.NodeTypes.NT_FILE)) {
        curNode = curNode.getParent();
      }

      String href = getHref();
      String shortHref = getShortHref();
      String prefix = href.substring(0, href.length() - shortHref.length());
      
      String hrefForNode = prefix + "/" + curNode.getSession().getWorkspace().getName() + curNode.getPath();
      
      WebDavResource resource = new NodeResource(webDavService,  hrefForNode, curNode);
      
      ResponseBuilder builder = new ResponseBuilder(resource, propFindDoc);
      MultiStatusResponse response = builder.getOwnResponse();      
      responses.add(response);      
    }
    
    return responses;
  }
  
  /*
   * removing jcr:content nodes
   */
  
  private boolean isSameNode(Node node) throws RepositoryException {
    
    log.info(">> " + node.getPath());
    
    if ("/".equals(searchablePath)) {
      return true;
    }
    
    if (node.getPath().equals(searchablePath) ||
        node.getPath().startsWith(searchablePath + "/")) {
      return true; 
    }
    
//    if (DavConst.NodeTypes.JCR_CONTENT.equals(node.getName()) &&
//        node.getParent().isNodeType(DavConst.NodeTypes.NT_FILE)) {
//      return false;
//    }
    
    return false;
  }
  
}

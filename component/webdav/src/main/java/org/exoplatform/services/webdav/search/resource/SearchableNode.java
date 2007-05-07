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
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.documents.PropFindDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.search.Search;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SearchableNode.java 12883 2007-02-19 16:39:06Z gavrikvetal $
 */

public class SearchableNode extends AbstractNodeResource implements SearchableResource {
  
  private static Log log = ExoLogger.getLogger("jcr.SearchableNode");
  
  private String searchablePath;
  
  public SearchableNode(WebDavCommandContext context, Node node) throws RepositoryException {
    super(context, node);

    searchablePath = context.getWebDavRequest().getSrcPath();
    if (!searchablePath.endsWith("/")) {
      searchablePath += "/";
    }  
  }

  public ArrayList<Response> doSearch(Search search) throws RepositoryException {
    QueryManager queryManager = null;
    
    try {
      queryManager = getNode().getSession().getWorkspace().getQueryManager();      
    } catch (RepositoryException rexc) {
      rexc.printStackTrace();
      return new ArrayList<Response>();
    }
        
    //log.info(">>> SEARCH QUERY: [" + search.getQuery() + "]");
    
    Query query = queryManager.createQuery(search.getQuery(), search.getQueryLanguage());        
    QueryResult result = query.execute();
    
    //log.info("NODES COUNT: " + result.getNodes().getSize());
    
    NodeIterator nodeIter = result.getNodes();

    PropFindDocument propFindDoc = new PropFindDocument();
    propFindDoc.initFactory(context.getWebDavRequest().getPropertyFactory());
    //PropFindDoc propFindDoc = new PropFindDoc(search.getRequiredPropertyList());
    
    ArrayList<Response> responses = new ArrayList<Response>();
    
    while (nodeIter.hasNext()) {
      Node curNode = nodeIter.nextNode();
      
      //log.info("SEARCHED NODE: [" + curNode.getPath() + "]");
      
      if (!isSameNode(curNode)) {
        continue;
      }
      
      if (DavConst.NodeTypes.JCR_CONTENT.equals(curNode.getName()) &&
          curNode.getParent().isNodeType(DavConst.NodeTypes.NT_FILE)) {
        curNode = curNode.getParent();
      }

      WebDavResource resource = new NodeResource(context, curNode);
      log.info("SEARCHED NAME: " + resource.getName());
      //responses.add(resource.getResponse(propFindDoc));      
    }
    
    return responses;
  }
  
  /*
   * removing jcr:content nodes
   */
  
  private boolean isSameNode(Node node) throws RepositoryException {    
    if (!(node.getPath().startsWith(searchablePath))) {
      return false;
    }
    
//    if (DavConst.NodeTypes.JCR_CONTENT.equals(node.getName()) &&
//        node.getParent().isNodeType(DavConst.NodeTypes.NT_FILE)) {
//      return false;
//    }
    
    return true;
  }
  
}

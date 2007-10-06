/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.exoplatform.services.webdav.deltav.report.versiontree.VersionTreeResponseBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeRepresentation implements RequestRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.VersionTreeRepresentation");
  
  public static final String TAGNAME = "version-tree";
  
  protected HashMap<String, ArrayList<String>> properties = new HashMap<String, ArrayList<String>>();
  
  private WebDavService webDavService;
  
  public VersionTreeRepresentation() {
    log.info("costruct...");
  }

  public String getDocumentName() {
    return TAGNAME;
  }

  public String getNamespaceURI() {
    return "DAV:";
  }

  public ResponseRepresentation getResponseRepresentation() {
    return null;
  }

  protected void readPropertyList(Node props) {
    NodeList nodes = props.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
              
      String name = curNode.getLocalName();
      String nameSpace = curNode.getNamespaceURI();

      log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      log.info("NAME: " + name);
      log.info("NAMESPACE: " + nameSpace);
      
      ArrayList<String> nameSpacedList = properties.get(nameSpace);
      if (nameSpacedList == null) {
        nameSpacedList = new ArrayList<String>();
        properties.put(nameSpace, nameSpacedList);
      }
      
      if (!nameSpacedList.contains(name)) {
        nameSpacedList.add(name);
      }
    }
  }  

  public void parse(Document document) {
    log.info("try parsing...");
    
    try {
      Node versionTree = DavUtil.getChildNode(document, getDocumentName());
      
      log.info("VersionTreeNode: " + versionTree);
      
      if (DavUtil.getChildNode(versionTree, DavProperty.ALLPROP) != null) {
        //selectMode = MultiPropertyRepresentation.MODE_ALLPROP;        
        return;
      }      
      
      Node props = DavUtil.getChildNode(versionTree, DavProperty.PROP);
      
      log.info("PROP node: " + props);
      
      readPropertyList(props);
      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
  }

  public void setWebDavService(WebDavService webDavService) {
    this.webDavService = webDavService;
  }

}

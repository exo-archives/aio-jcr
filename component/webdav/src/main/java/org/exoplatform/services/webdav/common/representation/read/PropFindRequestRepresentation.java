/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropFindRequestRepresentation implements RequestRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.PropFindRepresentation");
  
  public static final String TAGNAME = "propfind";
  
  protected HashMap<String, ArrayList<String>> properties = new HashMap<String, ArrayList<String>>();
  
  public static final int SELECTMODE_PROP = 1;

  public static final int SELECTMODE_ALLPROP = 2;
  
  public static final int SELECTMODE_PROPNAMES = 3;

  protected int selectMode = SELECTMODE_ALLPROP;
  
  protected WebDavService webDavService;
  
  public PropFindRequestRepresentation(WebDavService webDavService) {
    log.info("construct......");
    this.webDavService = webDavService;    
    log.info("WEBDAVSERVICE: " + webDavService);
  }
  
  public String getDocumentName() {
    return TAGNAME;
  }
  
  public String getNamespaceURI() {
    return "DAV:";
  }
  
  protected void readPropertyList(Node props) {
    
    selectMode = SELECTMODE_PROP;
    
    NodeList nodes = props.getChildNodes();    
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
              
      String name = curNode.getLocalName();
      String nameSpace = curNode.getNamespaceURI();
      
      if (name == null) {
        continue;
      }
      
      log.info(">> FINDED NAME: " + name);
      log.info(">> FINDED NAMESPACE: " + nameSpace);
      
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
    log.info("try to parse document: " + document);

    try {      
      Node propFind = DavUtil.getChildNode(document, getDocumentName());

      if (DavUtil.getChildNode(propFind, DavProperty.ALLPROP) != null) {
        return;
      }
      
      Node props = DavUtil.getChildNode(propFind, DavProperty.PROP);
      
      if (DavUtil.getChildNode(props, DavProperty.ALLPROP) != null) {        
        return;
      }
      
      readPropertyList(props);

      Node propInclude = DavUtil.getChildNode(propFind, DavProperty.INCLUDE);
      if (propInclude != null) {
        log.info("Needed extended include property.");
      }
      
      return;
    } catch (Exception exc) {
      log.info("Can't fill document data. " + exc.getMessage());
      exc.printStackTrace();      
    }

  }
  
  public ResponseRepresentation getResponseRepresentation() {
    log.info("try to get response representation");

    if (selectMode == SELECTMODE_PROP) {
      return new PropResponseRepresentation(webDavService, properties);
    } else if (selectMode == SELECTMODE_PROPNAMES) {
      return new PropNamesResponseRepresentation();
    }
    
    return new AllPropResponseRepresentation(webDavService);    
  }
  
}

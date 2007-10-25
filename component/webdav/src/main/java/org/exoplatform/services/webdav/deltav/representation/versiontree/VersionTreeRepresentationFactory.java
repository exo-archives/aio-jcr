/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.read.PropFindRepresentationFactory;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class VersionTreeRepresentationFactory {
  
  public static final String XML_VERSIONTREE = "version-tree";
  
  public static final String XML_ALLPROP = "allprop";
  
  public static final String XML_PROP = "prop";
  
  public static XmlResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href) throws RepositoryException {
    Node versionTree = DavUtil.getChildNode(document, XML_VERSIONTREE);
  
    Node allProp = DavUtil.getChildNode(versionTree, XML_ALLPROP);
    if (allProp != null) {
      return new AllPropVersionTreeResponseRepresentation(webDavService, href, (javax.jcr.Node)node);
    }
    
    
    Node props = DavUtil.getChildNode(versionTree, XML_PROP);
    
    HashMap<String, ArrayList<String>> properties = PropFindRepresentationFactory.getProperties(props);
    
    return new VersionTreeResponseRepresentation(webDavService, properties, href, (javax.jcr.Node)node);
  }

}

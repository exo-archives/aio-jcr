/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.deltav.representation.versiontree.VersionTreeRepresentationFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ReportRepresentationFactory {
  
  public static final String DAV_NS = "DAV:";
    
  public static XmlResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href) throws RepositoryException, BadRequestException {    
    Node reportNode = document.getChildNodes().item(0);
    
    String nameSpace = reportNode.getNamespaceURI();
    String localName = reportNode.getLocalName();
    
    if (DAV_NS.equals(nameSpace) && VersionTreeRepresentationFactory.XML_VERSIONTREE.equals(localName)) {
      return VersionTreeRepresentationFactory.createResponseRepresentation(webDavService, document, node, href);
    }
    
    throw new BadRequestException();
  }

}

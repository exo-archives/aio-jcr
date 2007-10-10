/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.deltav.representation.versiontree.VersionTreeRepresentationFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ReportRepresentationFactory {
  
  public static final String DAV_NS = "DAV:";
    
  private static Log log = ExoLogger.getLogger("jcr.ReportRepresentationFactory");
  
  public static XmlResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href) throws RepositoryException, BadRequestException {    
    Node reportNode = document.getChildNodes().item(0);
    
    String nameSpace = reportNode.getNamespaceURI();
    String localName = reportNode.getLocalName();
    
    log.info("REPORT NODE: " + reportNode);
    log.info("NAMESPACE: " + nameSpace);
    log.info("LOCALNAME: " + localName);
    
    if (DAV_NS.equals(nameSpace) && VersionTreeRepresentationFactory.XML_VERSIONTREE.equals(localName)) {
      
      log.info("Creating version tree representation");
      
      return VersionTreeRepresentationFactory.createResponseRepresentation(webDavService, document, node, href);
    }
    
    log.info("Skipping..........");
        
    throw new BadRequestException();
  }

}

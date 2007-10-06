/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SimpleRequestRepresentation implements RequestRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.SimpleRequestRepresentation");
  
  private WebDavService webDavService;
  
  private String nameSpaceURI;
  
  private String documentName;
  
  public SimpleRequestRepresentation(String nameSpaceURI, String documentName) {
    log.info("construct...");
    this.nameSpaceURI = nameSpaceURI;
    this.documentName = documentName;
  }

  public String getDocumentName() {
    return documentName;
  }

  public String getNamespaceURI() {
    return nameSpaceURI; 
  }

  public ResponseRepresentation getResponseRepresentation() {
    return null;
  }

  public void parse(Document document) {
    log.info("parsing...");
  }

  public void setWebDavService(WebDavService webDavService) {
    this.webDavService = webDavService;
  }

}

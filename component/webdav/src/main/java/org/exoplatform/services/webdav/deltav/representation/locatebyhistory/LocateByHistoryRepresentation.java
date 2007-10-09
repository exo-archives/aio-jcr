/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.locatebyhistory;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LocateByHistoryRepresentation implements RequestRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.LocateByHistoryRepresentation");
  
  private WebDavService webDavService;
  
  public LocateByHistoryRepresentation() {
    log.info("construct...");
  }

  public String getDocumentName() {
    return "locate-by-history";
  }

  public String getNamespaceURI() {
    return "DAV:";
  }

  public ResponseRepresentation getResponseRepresentation() {
    return null;
  }

  public void parse(Document document) {
    log.info("try parsing...");
  }

  public void setWebDavService(WebDavService webDavService) {
    this.webDavService = webDavService;
  }

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.representation.SimpleRequestRepresentation;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RequestRepresentationDispatcher {
  
  private static Log log = ExoLogger.getLogger("jcr.RequestRepresentationDispatcher");
  
  protected WebDavService webDavService;

  public RequestRepresentationDispatcher(WebDavService webDavService) {
    log.info("construct...");
    this.webDavService = webDavService;
  }
  
  public RequestRepresentation getRequestRepresentation(Document document) {
    
    log.info("try to get request representation...");
    
    if (document == null) {
      return null;
    }
    
    Element documentElement = document.getDocumentElement();
    
    String documentName = documentElement.getLocalName();
    String nameSpaceURI = documentElement.getNamespaceURI();
    
    log.info("DOCUMEN NAME: " + documentName);
    log.info("DOCUMENT NAME SPACE: " + nameSpaceURI);
    
    String className = null;
    
    HashMap<String, HashMap<String, String>> representations = webDavService.getDocuments();
    
    HashMap<String, String> reprList = representations.get(nameSpaceURI);
    if (reprList != null) {
      className = reprList.get(documentName);
    }
    
    if (className == null) {
      className = SimpleRequestRepresentation.class.getCanonicalName();
    }
    
    try {
      Class requestRepresentationClasss = Class.forName(className);

      /*
       * Check if RequestRepresentation requires a WebDav Service
       */
      
      RequestRepresentation requestRepresentation;
      
      try {
        Constructor constructor = requestRepresentationClasss.getConstructor(WebDavService.class);
        requestRepresentation = (RequestRepresentation)constructor.newInstance(webDavService);
        
        log.info("CREATED WITH WEBDAV SERVICE INJECTION");
        
      } catch (NoSuchMethodException mexc) {
        log.info("No constructor with parameter WebDavService found! ");
        requestRepresentation = (RequestRepresentation)requestRepresentationClasss.newInstance();
        
        log.info("CREATED USING DEFAULT CONSTRUCTOR");
      }

      requestRepresentation.parse(document);
      return requestRepresentation;
    } catch (Exception exc) {
      log.info("Unhandled ecxeption. " + exc.getMessage(), exc);
    }
    
    return null;
  }

}

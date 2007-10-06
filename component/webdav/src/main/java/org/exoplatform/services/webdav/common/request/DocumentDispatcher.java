/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.request;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.request.documents.DefaultDocument;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.config.WebDavConfig;
import org.exoplatform.services.webdav.config.WebDavConfigImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DocumentDispatcher {
  
  private static Log log = ExoLogger.getLogger("jcr.DocumentDispatcher");

  private WebDavConfig webDavConfig;
  private Document document;
  
  public DocumentDispatcher(WebDavConfig webDavConfig, Document document) {
    this.webDavConfig = webDavConfig;
    this.document = document;
  }
  
  public RequestDocument getRequestDocument() {    
    if (document == null) {
      return new DefaultDocument();
    }
    
    Element documentElement = document.getDocumentElement();
    
    String documentName = documentElement.getLocalName();
    String documentNameSpace = documentElement.getNamespaceURI();
    
    ArrayList<HashMap<String, String>> documents = webDavConfig.getRequestDocuments();
    for (int i = 0; i < documents.size(); i++) {
      HashMap<String, String> documentDescription = documents.get(i);
      
      String name = documentDescription.get(WebDavConfigImpl.XML_DOCUMENTNAME);
      String nameSpace = documentDescription.get(WebDavConfigImpl.XML_NAMESPACE);
      
      if (documentName.equals(name) && documentNameSpace.equals(nameSpace)) {
        String className = documentDescription.get(WebDavConfigImpl.XML_CLASSNAME);

        try {
          RequestDocument requestDocument = (RequestDocument)(Class.forName(className).newInstance());
          requestDocument.init(document, webDavConfig.getPropertyFactory());
          return requestDocument;
        } catch (Exception exc) {
          log.info("Unhandled exception. " + exc.getMessage(), exc);
        }
        
      }
    }
    
    return new DefaultDocument();
  }
  
}

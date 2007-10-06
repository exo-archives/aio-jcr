/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.request.documents;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DefaultDocument implements RequestDocument {
  
  private static Log log = ExoLogger.getLogger("jcr.DefaultDocument");
  
  private String documentName = "default";
  
  public String getDocumentName() {
    log.info("Returning default document........");
    return documentName;
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {    
    log.info("INIT!!!!!!!!!!");
    
    log.info("RequestDocument: " + requestDocument);
    
    log.info("PropertyFactory: " + propertyFactory);
    
    return false;
  }

}

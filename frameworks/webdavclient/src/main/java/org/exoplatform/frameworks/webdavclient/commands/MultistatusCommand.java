/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.documents.DocumentApi;
import org.exoplatform.frameworks.webdavclient.documents.DocumentManager;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.request.PropertyList;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class MultistatusCommand extends DavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.MultistatusCommand");

  protected PropertyList propList = new PropertyList();
  
  protected String xmlName = Const.StreamDocs.PROPFIND;
  
  protected DocumentApi multistatusDocument = null;
  
  public MultistatusCommand(WebDavContext context) throws Exception {
    super(context);
  }
    
  public void requireAllProperties() {
    propList.clearProperies();
  }
  
  public void setRequiredProperty(String propertyName) {
    propList.setProperty(propertyName);
  }
  
  public Element toXml(Document xmlDocument) {
    Element propFindEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE, 
        Const.Dav.PREFIX + xmlName);
    xmlDocument.appendChild(propFindEl);
    
    propFindEl.appendChild(propList.toXml(xmlDocument));    
    
    return propFindEl;
  }
  
  public void finalExecute() {
    try {
      if (client.getReplyCode() != Const.HttpStatus.MULTISTATUS) {
        return;
      }
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    multistatusDocument = DocumentManager.getResponseDocument(client.getResponseStream());
  }
  
  public Multistatus getMultistatus() {
    return (Multistatus)multistatusDocument;
  }
  
  
}

/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.WebDavContext;
import org.exoplatform.frameworks.davclient.documents.DocumentApi;
import org.exoplatform.frameworks.davclient.documents.DocumentManager;
import org.exoplatform.frameworks.davclient.documents.PropDoc;
import org.exoplatform.frameworks.davclient.properties.LockDiscoveryProp;
import org.exoplatform.frameworks.davclient.properties.PropApi;
import org.exoplatform.frameworks.davclient.properties.LockDiscoveryProp.ActiveLock;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavLock extends DavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.DavLock");
  
  protected DocumentApi propDocument = null;
  protected String lockToken = "";
  
  public DavLock(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.LOCK;
  }
  
  public void finalExecute() {
    try {
      if (client.getReplyCode() != Const.HttpStatus.OK) {        
        return;
      }      

      lockToken = client.getResponseHeader(Const.HttpHeaders.LOCKTOKEN);    
      lockToken = lockToken.substring(1, lockToken.length() - 1);    
      propDocument = DocumentManager.getResponseDocument(client.getResponseStream());
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage());
    }    
  }
  
  public String getLockToken() {
    return lockToken;
  }
  
  public DocumentApi getLockDocument() {
    return propDocument;
  }
  
  public ActiveLock getActiveLock() {
    if (!(propDocument instanceof PropDoc)) {
      return null;
    }
    
    PropApi property = ((PropDoc)propDocument).getSingleProperty();
    if (!(property instanceof LockDiscoveryProp)) {
      return null;
    }
    
    return ((LockDiscoveryProp)property).getActiveLock();
  }
  
  public Element toXml(Document xmlDocument) {
    Element lockInfoEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE,
        Const.Dav.PREFIX + Const.StreamDocs.LOCKINFO);
    xmlDocument.appendChild(lockInfoEl);
    //lockInfoEl.setAttribute(Const.Dav.NAMESPACEATTR, Const.Dav.NAMESPACE);
    
    Element lockScopeEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.LOCKSCOPE);
    lockInfoEl.appendChild(lockScopeEl);
    
    Element scopeExclusive = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.EXCLUSIVE);
    lockScopeEl.appendChild(scopeExclusive);
    
    Element lockTypeEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.LOCKTYPE);
    lockInfoEl.appendChild(lockTypeEl);
    
    Element typeWrite = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.WRITE);
    lockTypeEl.appendChild(typeWrite);
    
    Element ownerEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.OWNER);
    lockInfoEl.appendChild(ownerEl);
    
    ownerEl.setTextContent("gavrik-vetal@ukr.net");
    
    return lockInfoEl;
  }

}

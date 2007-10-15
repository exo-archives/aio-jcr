/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.httpclient.HttpHeader;
import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.documents.DocumentApi;
import org.exoplatform.frameworks.webdavclient.documents.DocumentManager;
import org.exoplatform.frameworks.webdavclient.documents.PropDoc;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp.ActiveLock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavLock extends DavCommand {
  
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

      lockToken = client.getResponseHeader(HttpHeader.LOCKTOKEN);    
      lockToken = lockToken.substring(1, lockToken.length() - 1);    
      propDocument = DocumentManager.getResponseDocument(client.getResponseStream());
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
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

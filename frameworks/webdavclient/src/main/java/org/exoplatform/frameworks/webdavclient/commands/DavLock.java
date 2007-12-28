/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.documents.DocumentApi;
import org.exoplatform.frameworks.webdavclient.documents.DocumentManager;
import org.exoplatform.frameworks.webdavclient.documents.PropDoc;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp.ActiveLock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavLock extends DavCommand {
  
  protected DocumentApi propDocument = null;
  protected String lockToken = "";
  protected int depth = 0;
  protected int timeOut = Integer.MAX_VALUE;
  
  public DavLock(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.LOCK;
    
    client.setRequestHeader("connection", "TE");
    client.setRequestHeader("te", "trailers");
    client.setRequestHeader("content-type", "application/xml");    
  }
  
  public void setDepth(int depth) {
    this.depth = depth;    
  }
  
  public void setTimeOut(int timeOut) {
    this.timeOut = timeOut;
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
  
  @Override
  public int execute() throws Exception {
    client.setRequestHeader(HttpHeader.DEPTH, "" + depth);
    client.setRequestHeader(HttpHeader.TIMEOUT, "Second-" + timeOut);
    return super.execute();
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

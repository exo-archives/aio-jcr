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

package org.exoplatform.applications.ooplugin.utils;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.documents.DocumentApi;
import org.exoplatform.frameworks.webdavclient.documents.DocumentManager;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.request.PropertyList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class MultistatusCommand extends DavCommand {
  
  protected PropertyList propList = new PropertyList();
  
  protected String xmlName = Const.StreamDocs.PROPFIND;
  
  protected DocumentApi multistatusDocument = null;
  
  public MultistatusCommand(WebDavContext context) throws Exception {
    super(context);
  }
    
  public void requireAllProperties() {
    propList.clearProperies();
  }
  
  public void setRequiredProperty(String name, String nameSpace) {
    propList.setProperty(name, nameSpace);
  }
  
  public void setRequiredProperty(String name) {
    propList.setProperty(name, "DAV:");
  }
  
  public void isPropNamesRequired(boolean isPropNames) {
    propList.isPropNamesRequired(isPropNames);
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
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    
    multistatusDocument = DocumentManager.getResponseDocument(client.getResponseStream());
  }
  
  public Multistatus getMultistatus() {
    return (Multistatus)multistatusDocument;
  }
  
  
}

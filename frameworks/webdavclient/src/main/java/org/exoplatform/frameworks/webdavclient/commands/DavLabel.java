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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavLabel extends DavCommand {
  
  public static final String ADD = "add";
  
  public static final String SET = "set";
  
  public static final String REMOVE = "remove";
  
  public static final String XML_LABELNAME = "label-name";
  
  private String action = ADD;
  
  private String labelName;
  
  public DavLabel(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.LABEL;
  }
  
  public void addLabel(String labelName) {
    this.labelName = labelName;
    action = ADD;
  }

  public void setLabel(String labelName) {
    this.labelName = labelName;
    action = SET;
  }
  
  public void removeLabel(String labelName) {
    this.labelName = labelName;
    action = REMOVE;
  }
  
  public Element toXml(Document xmlDocument) {
    Element labelElement = xmlDocument.createElementNS(Const.Dav.NAMESPACE,
        Const.Dav.PREFIX + Const.StreamDocs.LABEL);
    xmlDocument.appendChild(labelElement);

    Element actionElement = xmlDocument.createElement(Const.Dav.PREFIX + action);
    labelElement.appendChild(actionElement);
    
    Element labelNameElement = xmlDocument.createElement(Const.Dav.PREFIX + XML_LABELNAME);
    actionElement.appendChild(labelNameElement);
    
    labelNameElement.setTextContent(labelName);
    
//    Element lockScopeEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.LOCKSCOPE);
//    lockInfoEl.appendChild(lockScopeEl);
//    
//    Element scopeExclusive = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.EXCLUSIVE);
//    lockScopeEl.appendChild(scopeExclusive);
//    
//    Element lockTypeEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.LOCKTYPE);
//    lockInfoEl.appendChild(lockTypeEl);
//    
//    Element typeWrite = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.WRITE);
//    lockTypeEl.appendChild(typeWrite);
//    
//    Element ownerEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.OWNER);
//    lockInfoEl.appendChild(ownerEl);
//    
//    ownerEl.setTextContent("gavrik-vetal@ukr.net");
    
    return labelElement;
  }

}

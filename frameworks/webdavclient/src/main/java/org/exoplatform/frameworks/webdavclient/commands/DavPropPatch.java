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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.request.NameSpaceRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavPropPatch extends MultistatusCommand {
  
  protected HashMap<String, ArrayList<String>> propSet = new HashMap<String, ArrayList<String>>();  
  protected ArrayList<String> propRemove = new ArrayList<String>();
  
  protected NameSpaceRegistry nsRegistry = new NameSpaceRegistry();
  
  public DavPropPatch(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.PROPPATCH;
  }  

  public void setProperty(String propertyName, String nameSpace, String propertyValue) {
    if (propSet.containsKey(propertyName)) {
      ArrayList<String> values = propSet.get(propertyName);
      values.add(propertyValue);
    } else {
      ArrayList<String> value = new ArrayList<String>();
      value.add(propertyValue);
      if (nsRegistry.registerNameSpace(propertyName, nameSpace)) {        
        propSet.put(propertyName, value);
      } else {
        propSet.put(Const.Dav.PREFIX + propertyName, value);
      }      
    }
    
  }
  
  public void removeProperty(String propertyName, String nameSpace) {
    if (nsRegistry.registerNameSpace(propertyName, nameSpace)) {
      propRemove.add(propertyName);      
    } else {
      propRemove.add(Const.Dav.PREFIX + propertyName);      
    }
  }

  public Element toXml(Document xmlDocument) {
    Element propUpdateEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE,
        Const.Dav.PREFIX + Const.StreamDocs.PROPERTYUPDATE);
    xmlDocument.appendChild(propUpdateEl);
    
    nsRegistry.fillNameSpaces(propUpdateEl);
    
    Element removeEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.REMOVE);
    propUpdateEl.appendChild(removeEl);
    Element propRemoveEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.PROP);
    removeEl.appendChild(propRemoveEl);
    
    for (int i = 0; i < propRemove.size(); i++) {
      String propName = propRemove.get(i);      
      Element curPropEl = xmlDocument.createElement(propName);
      propRemoveEl.appendChild(curPropEl);
    }
    
    Element setEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.SET);
    propUpdateEl.appendChild(setEl);
    Element propSetEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.PROP);
    setEl.appendChild(propSetEl);
    
    Iterator<String> keyIter = propSet.keySet().iterator();
    while (keyIter.hasNext()) {
      String propName = keyIter.next();
      ArrayList<String> values = propSet.get(propName);

      for (int i = 0; i < values.size(); i++) {
        Element curPropEl = xmlDocument.createElement(propName);
        propSetEl.appendChild(curPropEl);
        curPropEl.setTextContent(values.get(i));
      }
      
    }
    
    return propUpdateEl;
  }
  
}

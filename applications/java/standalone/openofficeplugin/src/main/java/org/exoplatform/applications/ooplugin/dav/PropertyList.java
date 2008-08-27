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

package org.exoplatform.applications.ooplugin.dav;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropertyList {

  protected ArrayList<String> properties = new ArrayList<String>();
  protected NameSpaceRegistry nsRegistry = new NameSpaceRegistry();
  
  protected boolean isPropNames = false;
  
  public void setProperty(String name, String nameSpace) {    
    if (nsRegistry.registerNameSpace(name, nameSpace)) {
      properties.add(name);
    } else {
      properties.add(Const.Dav.PREFIX + name);
    }
  }
  
  public void clearProperies() {
    properties.clear();
    nsRegistry.clearNameSpaces();
  }
  
  public boolean isAllProp() {
    return (properties.size() == 0) ? true : false;
  }
  
  public void isPropNamesRequired(boolean isPropNames) {
    this.isPropNames = isPropNames;
  }
    
  public Element toXml(Document xmlDocument) {    
    if (isPropNames) {
      return xmlDocument.createElement(Const.Dav.PREFIX + "propname");
    }
    
    if (isAllProp()) {
      return xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.ALLPROP);
    }
    
    
    Element propEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.PROP);
    nsRegistry.fillNameSpaces(propEl);

    for (int i = 0; i < properties.size(); i++) {
      String curPropName = properties.get(i); 
      Element curPropEl = xmlDocument.createElement(curPropName);
      propEl.appendChild(curPropEl);
    }
    return propEl;
  }
  
}

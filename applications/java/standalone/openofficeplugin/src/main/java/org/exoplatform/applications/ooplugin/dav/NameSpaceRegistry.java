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

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class NameSpaceRegistry {

  protected HashMap<String, String> nameSpaces = new HashMap<String, String>();

  public void clearNameSpaces() {
    nameSpaces.clear();
  }
  
  public boolean registerNameSpace(String prefixedName, String nameSpace) {
    if (prefixedName.indexOf(":") > 0) {
      String prefix = prefixedName.split(":")[0];
      
      String presentNameSpace = nameSpaces.get(nameSpace);
      if (presentNameSpace == null) {        
        nameSpaces.put(prefix, nameSpace);
      }      
      
      return true;
    }    
    
    return false;
  }
  
  public void fillNameSpaces(Element element) {
    Iterator<String> nsIter = nameSpaces.keySet().iterator();
    while (nsIter.hasNext()) {
      String prefix = nsIter.next();
      String nameSpace = nameSpaces.get(prefix);
      element.setAttribute("xmlns:" + prefix, nameSpace);
    }    
  }  
  
}

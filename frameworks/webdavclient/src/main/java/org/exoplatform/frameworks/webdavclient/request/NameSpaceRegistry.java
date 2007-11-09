/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.request;

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

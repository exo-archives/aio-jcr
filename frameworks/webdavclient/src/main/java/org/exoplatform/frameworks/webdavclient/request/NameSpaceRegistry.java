/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.request;

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class NameSpaceRegistry {

  protected HashMap<String, String> nameSpaces = new HashMap<String, String>();

  public void clearNameSpaces() {
    nameSpaces.clear();
  }
  
  public boolean registerNameSpace(String propertyName) {
    if (propertyName.indexOf(":") > 0) {
      String nameSpace = propertyName.substring(0, propertyName.indexOf(":"));
      
      String presentNameSpace = nameSpaces.get(nameSpace);
      if (presentNameSpace == null) {
        nameSpaces.put(nameSpace, nameSpace);
      }      
      
      return true;
    }    
    
    return false;
  }
  
  public void fillNameSpaces(Element element) {
    Iterator<String> nsIter = nameSpaces.keySet().iterator();
    while (nsIter.hasNext()) {
      String nameSpace = nsIter.next();
      element.setAttribute("xmlns:" + nameSpace, nameSpace + ":");
    }    
  }  
  
}

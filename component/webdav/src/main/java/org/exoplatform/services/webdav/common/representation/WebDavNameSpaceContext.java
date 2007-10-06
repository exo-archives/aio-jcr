/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavNameSpaceContext implements NamespaceContext {

  private HashMap<String, String> prefixes = new HashMap<String, String>();    
  
  public WebDavNameSpaceContext() {
    prefixes.put("DAV:", "D");
  }

  public String getNamespaceURI(String prefix) {
    Iterator<String> keyIter = prefixes.keySet().iterator();
    while (keyIter.hasNext()) {
      String key = keyIter.next();
      String value = prefixes.get(key);
      
      if (value.equals(prefix)) {
        return key;
      }
    }
    
    return null;
  }

  public String getPrefix(String namespaceURI) {
    return null;
  }

  public Iterator getPrefixes(String namespaceURI) {
    return null;
  }  
  
}

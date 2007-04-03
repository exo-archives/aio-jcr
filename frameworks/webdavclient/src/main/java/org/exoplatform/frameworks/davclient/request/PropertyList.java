/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.request;

import java.util.ArrayList;

import org.exoplatform.frameworks.davclient.Const;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyList {

  protected ArrayList<String> properties = new ArrayList<String>();
  protected NameSpaceRegistry nsRegistry = new NameSpaceRegistry();
  
  public void setProperty(String propertyName) {    
    if (nsRegistry.registerNameSpace(propertyName)) {
      properties.add(propertyName);
    } else {
      properties.add(Const.Dav.PREFIX + propertyName);
    }
  }
  
  public void clearProperies() {
    properties.clear();
    nsRegistry.clearNameSpaces();
  }
  
  public boolean isAllProp() {
    return (properties.size() == 0) ? true : false;
  }
    
  public Element toXml(Document xmlDocument) {
    Element propEl = null;
    if (isAllProp()) {
      propEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.ALLPROP);      
    } else {
      propEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.PROP);
      nsRegistry.fillNameSpaces(propEl);

      for (int i = 0; i < properties.size(); i++) {
        String curPropName = properties.get(i); 
        Element curPropEl = xmlDocument.createElement(curPropName);
        propEl.appendChild(curPropEl);
      }
    }
    return propEl;
  }
  
}

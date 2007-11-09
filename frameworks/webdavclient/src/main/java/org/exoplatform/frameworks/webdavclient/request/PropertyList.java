/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.request;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Const;
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

/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
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
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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

  public void setProperty(String propertyName, String propertyValue) {
    if (propSet.containsKey(propertyName)) {
      ArrayList<String> values = propSet.get(propertyName);
      values.add(propertyValue);
    } else {
      ArrayList<String> value = new ArrayList<String>();
      value.add(propertyValue);
      if (nsRegistry.registerNameSpace(propertyName)) {        
        propSet.put(propertyName, value);
      } else {
        propSet.put(Const.Dav.PREFIX + propertyName, value);
      }      
    }
    
  }
  
  public void removeProperty(String propertyName) {
    if (nsRegistry.registerNameSpace(propertyName)) {
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

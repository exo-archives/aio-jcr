/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.document.AbstractXmlSerializable;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: ResponseImpl.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class MultiStatusResponseImpl extends AbstractXmlSerializable implements MultiStatusResponse {
  
  private Href href;
  
  private int status = -1;
  private String description;
  
  private ArrayList<WebDavProperty> propertyes = new ArrayList<WebDavProperty>();
  
  public MultiStatusResponseImpl(Href href) {
    this.href = href;
  }
  
  public Href getHref() {
    return href;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public int getStatus() {
    return status;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void addProperty(WebDavProperty property, boolean excludeNotOk) {
    if (excludeNotOk) {
      if (property.getStatus() != WebDavStatus.OK) {
        return;
      }
    }
    propertyes.add(property);
  }
  
  public String getTagName() {
    return DavProperty.RESPONSE;
  }
  
  public Element createElement(Document document) {
    return document.createElement(DavConst.DAV_PREFIX + DavProperty.RESPONSE);
  }
  
  public void serializeBody(Element element) {
    href.serialize(element);
    
    if (status > 0) {
      Element statusEl = element.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.STATUS);
      element.appendChild(statusEl);
      statusEl.setTextContent(WebDavStatus.getStatusDescription(status));
    }
    
    if (description != null) {
      Element descriptionEl = element.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.RESPONSEDESCRIPTION);
      element.appendChild(descriptionEl);
      descriptionEl.setTextContent(description);
    }

    
    HashMap<Integer, ArrayList<WebDavProperty>> propStatusGroups = new HashMap<Integer, ArrayList<WebDavProperty>>();
    
    HashMap<String, String> nameSpaces = new HashMap<String, String>();    
    for (WebDavProperty curProp : propertyes) {
      
      String propertyName = curProp.getName();
      
      if (propertyName.indexOf(":") > 0) {        
        String nameSpace = propertyName.substring(0, propertyName.indexOf(":"));
        
        if (!nameSpace.equals("DAV")) {
          if (nameSpaces.get(nameSpace) == null) {
            nameSpaces.put(nameSpace, nameSpace);
          }        
        }

      }
      
      int curPropStat = curProp.getStatus();
      ArrayList<WebDavProperty> propGroup = (ArrayList<WebDavProperty>)propStatusGroups.get(curPropStat);
      if (propGroup == null) {
        propGroup = new ArrayList<WebDavProperty>();
        propStatusGroups.put(new Integer(curPropStat), propGroup);
      }
      propGroup.add(curProp);
    }
    
    Iterator<String> nameSpaceIter = nameSpaces.keySet().iterator();
    while (nameSpaceIter.hasNext()) {
      String nameSpace = nameSpaceIter.next();
      element.setAttribute("xmlns:" + nameSpace, nameSpace + ":");      
    }

    Iterator<Integer> groupStatusIter = propStatusGroups.keySet().iterator();
    while (groupStatusIter.hasNext()) {
      Integer curStatus = groupStatusIter.next();
      
      Element propStat = element.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.PROPSTAT);
      element.appendChild(propStat);
      
      Element prop = element.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.PROP);
      propStat.appendChild(prop);
      
      ArrayList<WebDavProperty> propList = propStatusGroups.get(curStatus);
      for (int i = 0; i < propList.size(); i++) {
        WebDavProperty curProp = propList.get(i);
        curProp.serialize(prop);
      }
      
      Element statusElement = element.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.STATUS);
      propStat.appendChild(statusElement);
      statusElement.setTextContent(WebDavStatus.getStatusDescription(curStatus));
    }
    
  }
  
//  public Element serialize(Element parentElement) {
//    Document doc = parentElement.getOwnerDocument();
//    
//    Element responseElement = doc.createElement(DavConst.DAV_PREFIX + DavProperty.RESPONSE);
//    parentElement.appendChild(responseElement);
//    
//    href.serialize(responseElement);
//    
//    if (status > 0) {
//      Element statusEl = doc.createElement(DavConst.DAV_PREFIX + DavProperty.STATUS);
//      responseElement.appendChild(statusEl);
//      statusEl.setTextContent(WebDavStatus.getStatusDescription(status));
//    }
//    
//    if (description != null) {
//      Element descriptionEl = doc.createElement(DavConst.DAV_PREFIX + DavProperty.RESPONSEDESCRIPTION);
//      responseElement.appendChild(descriptionEl);
//      descriptionEl.setTextContent(description);
//    }
//    
//    HashMap<Integer, ArrayList<WebDavProperty>> propStatusGroups = new HashMap<Integer, ArrayList<WebDavProperty>>();
//    
//    HashMap<String, String> nameSpaces = new HashMap<String, String>();    
//    for (WebDavProperty curProp : propertyes) {
//      
//      String propertyName = curProp.getName();
//      
//      if (propertyName.indexOf(":") > 0) {        
//        String nameSpace = propertyName.substring(0, propertyName.indexOf(":"));
//        
//        if (!nameSpace.equals("DAV")) {
//          if (nameSpaces.get(nameSpace) == null) {
//            nameSpaces.put(nameSpace, nameSpace);
//          }        
//        }
//
//      }
//      
//      int curPropStat = curProp.getStatus();
//      ArrayList<WebDavProperty> propGroup = (ArrayList<WebDavProperty>)propStatusGroups.get(curPropStat);
//      if (propGroup == null) {
//        propGroup = new ArrayList<WebDavProperty>();
//        propStatusGroups.put(new Integer(curPropStat), propGroup);
//      }
//      propGroup.add(curProp);
//    }
//    
//    Iterator<String> nameSpaceIter = nameSpaces.keySet().iterator();
//    while (nameSpaceIter.hasNext()) {
//      String nameSpace = nameSpaceIter.next();
//      responseElement.setAttribute("xmlns:" + nameSpace, nameSpace + ":");      
//    }
//
//    Iterator<Integer> groupStatusIter = propStatusGroups.keySet().iterator();
//    while (groupStatusIter.hasNext()) {
//      Integer curStatus = groupStatusIter.next();
//      
//      Element propStat = doc.createElement(DavConst.DAV_PREFIX + DavProperty.PROPSTAT);
//      responseElement.appendChild(propStat);
//      
//      Element prop = doc.createElement(DavConst.DAV_PREFIX + DavProperty.PROP);
//      propStat.appendChild(prop);
//      
//      ArrayList<WebDavProperty> propList = propStatusGroups.get(curStatus);
//      for (int i = 0; i < propList.size(); i++) {
//        WebDavProperty curProp = propList.get(i);
//        curProp.serialize(prop);
//      }
//      
//      Element statusElement = doc.createElement(DavConst.DAV_PREFIX + DavProperty.STATUS);
//      propStat.appendChild(statusElement);
//      statusElement.setTextContent(WebDavStatus.getStatusDescription(curStatus));
//    }
//    
//    return responseElement;
//  }

}

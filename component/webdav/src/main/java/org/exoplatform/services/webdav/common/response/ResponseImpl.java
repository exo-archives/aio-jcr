/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: ResponseImpl.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class ResponseImpl implements Response {
  
  private Href href;
  
  private int status = -1;
  private String description;
  
  private ArrayList<WebDavProperty> propertyes = new ArrayList<WebDavProperty>();
  
  public ResponseImpl(Href href) {
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
      if (property.getStatus() != DavStatus.OK) {
        return;
      }
    }
    propertyes.add(property);
  }
  
  // response
  public void toXml(Document rootDoc, Element multistatusElement) {
    Element response = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.RESPONSE);
    multistatusElement.appendChild(response);
    
    href.serialize(rootDoc, response);
    
    if (status > 0) {
      Element statusEl = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.STATUS);
      response.appendChild(statusEl);
      statusEl.setTextContent(DavStatus.getStatusDescription(status));
    }
    
    if (description != null) {
      Element descriptionEl = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.RESPONSEDESCRIPTION);
      response.appendChild(descriptionEl);
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
      response.setAttribute("xmlns:" + nameSpace, nameSpace + ":");      
    }

    Iterator<Integer> groupStatusIter = propStatusGroups.keySet().iterator();
    while (groupStatusIter.hasNext()) {
      Integer curStatus = groupStatusIter.next();
      
      Element propStat = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.PROPSTAT);
      response.appendChild(propStat);
      
      Element prop = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.PROP);
      propStat.appendChild(prop);
      
      ArrayList<WebDavProperty> propList = propStatusGroups.get(curStatus);
      for (int i = 0; i < propList.size(); i++) {
        WebDavProperty curProp = propList.get(i);
        curProp.serialize(rootDoc, prop);
      }
      
      Element status = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.STATUS);
      propStat.appendChild(status);
      status.setTextContent(DavStatus.getStatusDescription(curStatus));
    }
    
  }

}

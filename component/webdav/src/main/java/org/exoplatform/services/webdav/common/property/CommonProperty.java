/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.document.XmlSerializable;
import org.exoplatform.services.webdav.common.property.factory.MappingTable;
import org.exoplatform.services.webdav.common.property.factory.PropertyConfigTable;
import org.exoplatform.services.webdav.common.property.factory.PropertyMapping;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.Href;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: CommonProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class CommonProperty implements XmlSerializable, WebDavProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.CommonProperty");
  
  protected Href resourceHref;
  
  protected String propertyName = "";  
  
  protected boolean isMultiValue = false;
  protected ArrayList<String> propertyValues = new ArrayList<String>();
  
  protected String propertyValue = "";  
  
  protected boolean isWebDavProperty = false;
  
  protected int status = WebDavStatus.NOT_FOUND;
  protected Element propertyElement;
  
  protected MappingTable mappingTable;
  protected PropertyConfigTable propertyConfigTable;
    
  public CommonProperty(String propertyName) {
    this.propertyName = propertyName;
  }
  
  public void setConfiguration(MappingTable mappingTable, PropertyConfigTable propertyConfigTable) {
    this.mappingTable = mappingTable;
    this.propertyConfigTable = propertyConfigTable;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public int getStatus() {
    return status;
  }

  public void setName(String propertyName) {
    this.propertyName = propertyName;
  }
  
  public String getName() {
    return propertyName;
  }

  public void setValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }
  
  public String getValue() {
    return propertyValue;
  }
  
  public void setIsMultiValue() {
    isMultiValue = true;
    propertyValues = new ArrayList<String>();
  }
  
  public boolean isMultiValue() {
    return this.isMultiValue;
  }

  public void addMultiValue(String propertyValue) {
    propertyValues.add(propertyValue);
  }
  
  protected Node getResourceNode(WebDavResource resource) throws RepositoryException {
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node.isNodeType(DavConst.NodeTypes.NT_VERSION)) {
      node = node.getNode(DavConst.NodeTypes.JCR_FROZENNODE);
    }    
    
    PropertyMapping mapping = mappingTable.getMapping(propertyName, resource);
    
    String childNodeName = null;
    if (mapping != null) {
      childNodeName = mapping.getChildNodeName();
      if (childNodeName != null) {
        node = node.getNode(childNodeName);
      }
    }
    
    return node;
  }
  
  protected String getJcrPropertyName(WebDavResource resource) throws RepositoryException {
    PropertyMapping mapping = mappingTable.getMapping(propertyName, resource);
    String mappedName = null;
    if (mapping != null) {
      mappedName = mapping.getMappedName();
    }
    
    if (mappedName == null) {
      return propertyName;
    }
    return mappedName;
  }
  
  protected void setValues(Node node, String jcrPropertyName) throws RepositoryException {
    String []values = null;
    if (isMultiValue) {
      values = new String[propertyValues.size()];
      for (int i = 0; i < propertyValues.size(); i++) {
        values[i] = propertyValues.get(i);
      }
    } else {
      values = new String[1];
      values[0] = propertyValue;
    }
    
    node.setProperty(jcrPropertyName, values);    
  }
  
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
    
    Node node = getResourceNode(resource);
    String jcrPropertyName = getJcrPropertyName(resource);
    
    Property property = node.getProperty(jcrPropertyName);
    if (property.getDefinition().isMultiple()) {
      isMultiValue = true;
      Value []values = property.getValues();
      for (int i = 0; i < values.length; i++) {
        Value pValue = values[i];
        propertyValues.add(pValue.getString());
      }
    } else {
      propertyValue = property.getString();
    }
    
    status = WebDavStatus.OK;
    return true;
  }
  
  public boolean refresh(WebDavResource resource, Href href) {
    resourceHref = href;
    
    try {
      if (propertyConfigTable.isNeedExclude(resource, this)) {
        status = WebDavStatus.NOT_ACCEPTABLE;
        return false;
      }

      boolean boolStatus = initialize(resource);
      return boolStatus;
    } catch (RepositoryException rexc) {
      //log.info("Unhandled exception. " + rexc.getMessage(), rexc);
    }
    return false;
  }
  
  public boolean set(WebDavResource resource) {    
    if (!(resource instanceof AbstractNodeResource)) {
      status = WebDavStatus.FORBIDDEN;
      return false;
    }
    
    try {
      Node node = getResourceNode(resource);
      String jcrPropertyName = getJcrPropertyName(resource);
      
      if (isMultiValue) {
        setValues(node, jcrPropertyName);        
      } else {
        try {
          node.setProperty(jcrPropertyName, propertyValue);
        } catch (ValueFormatException vexc) {
          setValues(node, jcrPropertyName);
        }        
      }
      
      node.getSession().save();
      status = WebDavStatus.OK;
      return true;
      
    } catch (Exception exc) {
    }

    status = WebDavStatus.INTERNAL_SERVER_ERROR;    
    return false;
  }
  
  public boolean remove(WebDavResource resource) {
    if (!(resource instanceof AbstractNodeResource)) {
      status = WebDavStatus.FORBIDDEN;
      return false;
    }
    
    try {
      Node node = getResourceNode(resource);
      String jcrPropertyName = getJcrPropertyName(resource);
      
      Property property = node.getProperty(jcrPropertyName);
      property.remove();
      node.getSession().save();
      
      status = WebDavStatus.OK;
      return true;      
    } catch (RepositoryException rexc) {
    }
    
    status = WebDavStatus.INTERNAL_SERVER_ERROR;    
    return false;
  }
  
  public Element serialize(Element parentElement, String prefixedName) {
    Document doc = parentElement.getOwnerDocument();
    
    String elementName = prefixedName;
    if (elementName.startsWith(DavConst.DAV_NAMESPACE)) {
      elementName = DavConst.DAV_PREFIX + elementName.substring(DavConst.DAV_NAMESPACE.length());
    }
    
    if (isMultiValue) {
      
      for (int i = 0; i < propertyValues.size(); i++) {
        propertyElement = doc.createElement(elementName);
        parentElement.appendChild(propertyElement);

        if (WebDavStatus.OK != status) {
          return propertyElement;
        }
        
        propertyElement.setTextContent(propertyValues.get(i));        
      }
      
    } else {
    
      propertyElement = doc.createElement(elementName);
      parentElement.appendChild(propertyElement);
      if (WebDavStatus.OK != status) {
        return propertyElement;
      }
      if (!"".equals(propertyValue)) {
        propertyElement.setTextContent(propertyValue);
      }    

    }

    return propertyElement;
  }
  
  public Element serialize(Element parentElement) {
    return serialize(parentElement, propertyName);
  }

  public Element serialize(Document parentDocument) {
    //return serialize(parentElement, propertyName);
    
    log.info("Try to serialize with document as parent");
    
    return null;
  }  
  
}

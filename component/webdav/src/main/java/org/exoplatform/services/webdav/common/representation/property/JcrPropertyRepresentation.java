/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class JcrPropertyRepresentation extends CommonWebDavProperty {
  
  private String propertyNameSpace;
  
  private String propertyName;
  
  private String childNodeName;
  
  private String prefix;
  
  private String propertyValue = "";
  
  private String valueForUpdate;
  
  public JcrPropertyRepresentation(String propertyNameSpace, String propertyName) {
    this.propertyNameSpace = propertyNameSpace;
    this.propertyName = propertyName;
  }

  public JcrPropertyRepresentation(String propertyNameSpace, String propertyName, String childNodeName) {
    this.propertyNameSpace = propertyNameSpace;
    this.propertyName = propertyName;
    this.childNodeName = childNodeName;
  }  
  
  @Override
  public String getNameSpace() {
    return propertyNameSpace;
  }

  @Override
  public String getTagName() {
    return propertyName;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters(propertyValue);
  }
  
  @Override
  public void write(XMLStreamWriter xmlWriter) throws XMLStreamException {
    prefix = xmlWriter.getNamespaceContext().getPrefix(getNameSpace());
    
    if (status == WebDavStatus.OK) {
      xmlWriter.writeStartElement(prefix, getTagName(), getNameSpace());      
      xmlWriter.writeNamespace(prefix, getNameSpace());      
      writeContent(xmlWriter);      
      xmlWriter.writeEndElement();      
    } else {
      xmlWriter.writeEmptyElement(prefix, getTagName(), getNameSpace());
      xmlWriter.writeNamespace(prefix, getNameSpace());
    }
    
  }  

  public void read(Node node) {
    try {
      Node nodeToSelect = node;
      
      if (childNodeName != null) {
        nodeToSelect = nodeToSelect.getNode(childNodeName);
      }
      
      SessionImpl session = (SessionImpl)nodeToSelect.getSession();
      
      prefix = session.getNamespacePrefix(propertyNameSpace);
      
      String prefixedName = prefix + ":" + propertyName;
      
      Property property = nodeToSelect.getProperty(prefixedName);
      
      if (property.getDefinition().isMultiple()) {
        
        Value []values = property.getValues();
        for (int i = 0; i < values.length; i++) {
          Value value = values[i];
          
          if ("" != propertyValue) {
            propertyValue += "\r\n";
          }
          propertyValue += value.getString();
        }
        
      } else {      
        propertyValue = property.getValue().getString();
      }      
      
      status = WebDavStatus.OK;
    } catch (RepositoryException exc) {
    }
  }

  public void update(Node node) {    
    try {
      if (childNodeName != null) {
        node = node.getNode(childNodeName);
      }
      
      SessionImpl session = (SessionImpl)node.getSession();
      
      prefix = session.getNamespacePrefix(propertyNameSpace);
      
      String prefixedName = prefix + ":" + propertyName;
      
      try {
        node.setProperty(prefixedName, valueForUpdate);
      } catch (ValueFormatException vexc) {
        String []values = new String[1];
        values[0] = valueForUpdate;
        node.setProperty(prefixedName, values);
      }
      
      node.getSession().save();
      
      status = WebDavStatus.OK;
    } catch (PathNotFoundException pexc) {
      status = WebDavStatus.NOT_FOUND;
    } catch (RepositoryException rexc) {
      status = WebDavStatus.INTERNAL_SERVER_ERROR;
    }    
  }
  
  public void remove(Node node) {    
    try {
      if (childNodeName != null) {
        node = node.getNode(childNodeName);
      }
      
      SessionImpl session = (SessionImpl)node.getSession();
      
      prefix = session.getNamespacePrefix(propertyNameSpace);
      
      String prefixedName = prefix + ":" + propertyName;

      node.setProperty(prefixedName, (String)null);
      node.getSession().save();
      
      status = WebDavStatus.OK;
    } catch (RepositoryException rexc) {
      status = WebDavStatus.INTERNAL_SERVER_ERROR;
    }
  }

  public void parseContent(org.w3c.dom.Node node) {
    valueForUpdate = node.getTextContent();
  }

}

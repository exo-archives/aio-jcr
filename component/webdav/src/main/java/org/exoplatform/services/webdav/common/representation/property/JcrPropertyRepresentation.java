/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class JcrPropertyRepresentation extends CommonWebDavProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.JcrPropertyRepresentation");
  
  private String propertyNameSpace;
  
  private String propertyName;
  
  private String childNodeName;
  
  private String prefix;
  
  private String propertyValue;
  
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
    
    log.info(">> NAMESPACE: " + getNameSpace());
    log.info(">> TAGNAME: " + getTagName());
    
    //xmlWriter.writeStartElement(getNameSpace(), getTagName());
    
    log.info(">> PREFIX: " + prefix);
    
    xmlWriter.writeStartElement(prefix, getTagName(), getNameSpace());
    
    xmlWriter.writeNamespace(prefix, getNameSpace());
    
    if (status == WebDavStatus.OK) {
      writeContent(xmlWriter);
    }
    xmlWriter.writeEndElement();
  }  

  public void read(Node node) throws RepositoryException {

    Node nodeToSelect = node;
    
    if (childNodeName != null) {
      nodeToSelect = nodeToSelect.getNode(childNodeName);
    }
    
    log.info("NODE TO SELECT: " + nodeToSelect);
    log.info("NODENAME: " + nodeToSelect.getName());
    
    SessionImpl session = (SessionImpl)nodeToSelect.getSession();
    
    prefix = session.getNamespacePrefix(propertyNameSpace);
    
    String prefixedName = prefix + ":" + propertyName;
    log.info("PREFIXED NAME: " + prefixedName);
    
    try {
      Property property = nodeToSelect.getProperty(prefixedName);
      
      if (property.getDefinition().isMultiple()) {
        log.info("!!!!!!!!!! multiple");      
      } else {      
        propertyValue = property.getValue().getString();
      }
      
      log.info("PROPERTY VALUE: " + propertyValue);
      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    
    status = WebDavStatus.OK;
    
//    String []prefixes = session.getAllNamespacePrefixes();
    //String prefix = session.getNamespacePrefix(propertyNameSpace);
    
  }

}


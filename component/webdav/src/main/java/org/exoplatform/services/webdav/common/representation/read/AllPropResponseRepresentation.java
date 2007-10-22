/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.property.JcrPropertyRepresentation;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AllPropResponseRepresentation extends PropFindResponseRepresentation {
  
  public AllPropResponseRepresentation(WebDavService webDavService, String href, Node node, int depth) throws RepositoryException {
    super(webDavService, href, node, depth);
  }
  
  protected void writeResponseContent(XMLStreamWriter xmlStreamWriter, Node node) throws XMLStreamException, RepositoryException {
    ArrayList<PropertyRepresentation> properties = getProperties(node);

    xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.PROPSTAT);
    
    xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.PROP);    
    
    for (int i = 0; i < properties.size(); i++) {
      PropertyRepresentation property = properties.get(i);

      property.read(node);
      
      if (property.getStatus() == WebDavStatus.OK) {
        property.write(xmlStreamWriter);
      }
    }
    
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.STATUS);
    xmlStreamWriter.writeCharacters(WebDavStatus.getStatusDescription(WebDavStatus.OK));
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeEndElement();
  }
  
  protected ArrayList<PropertyRepresentation> getProperties(Node node) throws RepositoryException {
    SessionImpl session = (SessionImpl)node.getSession();
    
    ArrayList<PropertyRepresentation> properties = new ArrayList<PropertyRepresentation>();    

    HashMap<String, HashMap<String, String>> allProperties = webDavService.getProperies();    
    
    HashMap<String, String> allWebDavProperties = allProperties.get("DAV:");
    Iterator<String> keyIter = allWebDavProperties.keySet().iterator();
    while (keyIter.hasNext()) {
      String propertyName = keyIter.next();
      properties.add(webDavService.getPropertyRepresentation("DAV:", propertyName, href));
    }
    
    ArrayList<String> presentedProperties = new ArrayList<String>();
    
    PropertyIterator propIter = node.getProperties();
    while (propIter.hasNext()) {
      Property property = propIter.nextProperty();
      
      String propertyName = property.getName();
      
      if (DavConst.NodeTypes.JCR_DATA.equals(propertyName)) {
        continue;
      }
      
      if (!presentedProperties.contains(propertyName)) {        
        presentedProperties.add(propertyName);
        
        String prefixOnly = propertyName.split(":")[0];
        String nameSpace = session.getNamespaceURI(prefixOnly); 
        String nameOnly = propertyName.split(":")[1];        
        
        properties.add(new JcrPropertyRepresentation(nameSpace, nameOnly));
      }
    }

    if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      Node jcrContentNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);
      
      propIter = jcrContentNode.getProperties();
      while (propIter.hasNext()) {
        Property property = propIter.nextProperty();
        
        String propertyName = property.getName();
        
        if (DavConst.NodeTypes.JCR_DATA.equals(propertyName)) {
          continue;
        }
        
        if (presentedProperties.contains(propertyName)) {
          continue;
        }
        
        presentedProperties.add(propertyName);
        
        String prefixOnly = propertyName.split(":")[0];
        String nameSpace = session.getNamespaceURI(prefixOnly); 
        String nameOnly = propertyName.split(":")[1];        
        
        properties.add(new JcrPropertyRepresentation(nameSpace, nameOnly, DavConst.NodeTypes.JCR_CONTENT));
        
      }
    }    
    
    return properties;
  }

}


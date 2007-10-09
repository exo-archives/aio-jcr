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

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.representation.property.JcrPropertyRepresentation;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AllPropResponseRepresentation extends PropFindResponseRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.AllPropResponseRepresentation");
  
  private WebDavService webDavService;
  
  public AllPropResponseRepresentation(WebDavService webDavService, String href, Node node, int depth) throws RepositoryException {
    super(href, node, depth);
    log.info("construct..........");
    
    this.webDavService = webDavService;
  }
  
  protected void writeResponseContent(XMLStreamWriter xmlStreamWriter, Node node) throws XMLStreamException, RepositoryException {
    ArrayList<PropertyRepresentation> properties = getProperties(node);

    xmlStreamWriter.writeStartElement("DAV:", DavProperty.PROPSTAT);
    
    xmlStreamWriter.writeStartElement("DAV:", DavProperty.PROP);    
    
    for (int i = 0; i < properties.size(); i++) {
      PropertyRepresentation property = properties.get(i);
      
      log.info("Try to read property: " + property);
      
      try {        
        property.read(node);
      } catch (RepositoryException rexc) {
        log.info("Property read exception.! " + rexc.getMessage(), rexc);
      }
      
      if (property.getStatus() == WebDavStatus.OK) {
        property.write(xmlStreamWriter);
      }
    }
    
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeStartElement("DAV:", DavProperty.STATUS);
    xmlStreamWriter.writeCharacters(WebDavStatus.getStatusDescription(WebDavStatus.OK));
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeEndElement();
  }
  
  private ArrayList<PropertyRepresentation> getProperties(Node node) throws RepositoryException {
    SessionImpl session = (SessionImpl)node.getSession();
    
    ArrayList<PropertyRepresentation> properties = new ArrayList<PropertyRepresentation>();    

    HashMap<String, HashMap<String, String>> allProperties = webDavService.getProperies();    
    
    HashMap<String, String> allWebDavProperties = allProperties.get("DAV:");
    Iterator<String> keyIter = allWebDavProperties.keySet().iterator();
    while (keyIter.hasNext()) {
      String propertyName = keyIter.next();
      log.info(">> POROPERTY: " + propertyName);
      properties.add(webDavService.getPropertyRepresentation("DAV:", propertyName));
    }
    
    ArrayList<String> presentedProperties = new ArrayList<String>();
    
    PropertyIterator propIter = node.getProperties();
    while (propIter.hasNext()) {
      Property property = propIter.nextProperty();
      
      String propertyName = property.getName();
      
      log.info("PROPERTY: " + propertyName);
      
      if (!presentedProperties.contains(propertyName)) {        
        presentedProperties.add(propertyName);
        
        String prefixOnly = propertyName.split(":")[0];
        String nameSpace = session.getNamespaceURI(prefixOnly); 
        String nameOnly = propertyName.split(":")[1];
        
        log.info(">>>>>>>>>>>>>>> ADDED:");        
        
        log.info("PREFIX: " + prefixOnly);
        log.info("NAMESPACE: " + nameSpace);
        log.info("NAME: " + nameOnly);
        
        log.info(">>>>>>>>>>>>>>>>>>>>>>>");
        
        properties.add(new JcrPropertyRepresentation(nameSpace, nameOnly));
      }
    }

    if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      Node jcrContentNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);
      log.info("content node: " + jcrContentNode);
      
      propIter = jcrContentNode.getProperties();
      while (propIter.hasNext()) {
        Property property = propIter.nextProperty();
        
        String propertyName = property.getName();
        log.info("CONTENT PROPERTY: " + propertyName);
        
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
        
        log.info(">>>>>>>>>>>>>>> ADDED:");        
        
        log.info("PREFIX: " + prefixOnly);
        log.info("NAMESPACE: " + nameSpace);
        log.info("NAME: " + nameOnly);
        
        log.info(">>>>>>>>>>>>>>>>>>>>>>>");
        
        properties.add(new JcrPropertyRepresentation(nameSpace, nameOnly, DavConst.NodeTypes.JCR_CONTENT));
        
      }
    }
    
    String []prefixes = session.getAllNamespacePrefixes();
    
    for (int i = 0; i < prefixes.length; i++) {
      String curPrefix = prefixes[i];      
      String nameSpaceURI = session.getNamespaceURI(curPrefix);
      
      log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      log.info("PREFIX: " + curPrefix);
      log.info("NAMESPACE: " + nameSpaceURI);
      log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
    
    return properties;
  }

}


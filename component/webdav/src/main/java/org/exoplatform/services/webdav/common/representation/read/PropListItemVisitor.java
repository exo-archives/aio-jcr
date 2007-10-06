/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropListItemVisitor extends PropFindItemVisitor {
  
  private static Log log = ExoLogger.getLogger("jcr.PropListItemVisitor");
  
  public PropListItemVisitor(WebDavService webDavService, boolean breadthFirst, int maxLevel, String rootHref, HashMap<String, ArrayList<String>> properties, XMLStreamWriter xmlWriter) {
    super(webDavService, breadthFirst, maxLevel, rootHref, properties, xmlWriter);
   
    log.info("construct.......");
  }  
  
  protected void fillContent(Node node, XMLStreamWriter xmlWriter) throws RepositoryException, XMLStreamException {
    log.info("try to get properties for: " + node.getPath());
    
    // prepare list of properties
    ArrayList<PropertyRepresentation> propertyRepresentations = new ArrayList<PropertyRepresentation>();
    
    Set<String> nameSpaces = properties.keySet();
    Iterator<String> nameSpaceIter = nameSpaces.iterator();
    while (nameSpaceIter.hasNext()) {
      String propertyNameSpace = nameSpaceIter.next();
      log.info("try to fill for name space: " + propertyNameSpace);
      
      ArrayList<String> propertyList = properties.get(propertyNameSpace);
      for (int i = 0; i < propertyList.size(); i++) {
        String propertyName = propertyList.get(i);
        
        log.info("PROPERTY: " + propertyName);
        
        PropertyRepresentation representation = webDavService.getPropertyRepresentation(propertyNameSpace, propertyName);
        propertyRepresentations.add(representation);
        log.info("PROPERTY REPRESENTATION: " + representation);        
        try {
          representation.read(webDavService, node);
        } catch (PathNotFoundException pexc) {
          
          // don't show it
          
        } catch (RepositoryException exc) {
          log.info(">> SOME EXCEPTION " + exc.getMessage(), exc);
        }
      }
    }
    
    HashMap<Integer, ArrayList<PropertyRepresentation>> propStatusGroup = new HashMap<Integer, ArrayList<PropertyRepresentation>>();
    
    for (int i = 0; i < propertyRepresentations.size(); i++) {
      PropertyRepresentation representation = propertyRepresentations.get(i);
      
      ArrayList<PropertyRepresentation> statusList = propStatusGroup.get(representation.getStatus());
      if (statusList == null) {
        statusList = new ArrayList<PropertyRepresentation>();
        propStatusGroup.put(representation.getStatus(), statusList);        
      }
      statusList.add(representation);
    }
    
    Iterator<Integer> statusIterator = propStatusGroup.keySet().iterator();
    while (statusIterator.hasNext()) {
      int curStatus = statusIterator.next();

      xmlWriter.writeStartElement("DAV:", DavProperty.PROPSTAT);
            
      xmlWriter.writeStartElement("DAV:", DavProperty.PROP);
      ArrayList<PropertyRepresentation> representationList = propStatusGroup.get(curStatus);
      for (int i = 0; i < representationList.size(); i++) {
        PropertyRepresentation representation = representationList.get(i);
        representation.write(xmlWriter);
      }
      xmlWriter.writeEndElement();
      
      xmlWriter.writeStartElement("DAV:", DavProperty.STATUS);
      xmlWriter.writeCharacters(WebDavStatus.getStatusDescription(curStatus));
      xmlWriter.writeEndElement();
      
      xmlWriter.writeEndElement();      
    }
    
  }

}

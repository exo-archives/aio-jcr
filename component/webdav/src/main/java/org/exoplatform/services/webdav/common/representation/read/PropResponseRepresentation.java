/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropResponseRepresentation extends PropFindResponseRepresentation {
  
  private HashMap<String, ArrayList<String>> properties;
  
  public PropResponseRepresentation(WebDavService webDavService, HashMap<String, ArrayList<String>> properties, String href, Node node, int depth) throws RepositoryException {
    super(webDavService, href, node, depth);
    this.properties = properties;
  }
  
  protected void writeResponseContent(XMLStreamWriter xmlStreamWriter, Node node) throws XMLStreamException, RepositoryException {
    ArrayList<PropertyRepresentation> propertyRepresentations = new ArrayList<PropertyRepresentation>();
    
    Set<String> nameSpaces = properties.keySet();
    Iterator<String> nameSpaceIter = nameSpaces.iterator();
    while (nameSpaceIter.hasNext()) {
      String propertyNameSpace = nameSpaceIter.next();
      
      ArrayList<String> propertyList = properties.get(propertyNameSpace);
      for (int i = 0; i < propertyList.size(); i++) {
        String propertyName = propertyList.get(i);
        
        PropertyRepresentation representation = webDavService.getPropertyRepresentation(propertyNameSpace, propertyName, href);
        propertyRepresentations.add(representation);
        representation.read(node);
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

      xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.PROPSTAT);
            
      xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.PROP);
      ArrayList<PropertyRepresentation> representationList = propStatusGroup.get(curStatus);
      for (int i = 0; i < representationList.size(); i++) {
        PropertyRepresentation representation = representationList.get(i);
        representation.write(xmlStreamWriter);
      }
      xmlStreamWriter.writeEndElement();
      
      xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.STATUS);
      xmlStreamWriter.writeCharacters(WebDavStatus.getStatusDescription(curStatus));
      xmlStreamWriter.writeEndElement();
      
      xmlStreamWriter.writeEndElement();      
    }
    
  }

}

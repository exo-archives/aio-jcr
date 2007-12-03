/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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

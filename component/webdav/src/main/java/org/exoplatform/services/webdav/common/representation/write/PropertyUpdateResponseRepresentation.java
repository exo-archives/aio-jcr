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

package org.exoplatform.services.webdav.common.representation.write;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.WebDavNameSpaceContext;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropertyUpdateResponseRepresentation extends XmlResponseRepresentation {
  
  public static final String XML_MULTISTATUS = "multistatus"; 
  
  public static final String XML_RESPONSE = "response";
  
  private Node node;
  
  private String href;
  
  private ArrayList<PropertyRepresentation> setList;
  
  private ArrayList<PropertyRepresentation> removeList;
  
  public PropertyUpdateResponseRepresentation(Item node, String href, 
      ArrayList<PropertyRepresentation> setList, ArrayList<PropertyRepresentation> removeList) throws RepositoryException {
    
    super(new WebDavNameSpaceContext((ManageableRepository)node.getSession().getRepository()));
    
    this.node = (Node)node;    
    this.href = href;
    this.setList = setList;
    this.removeList = removeList;
  }

  @Override
  protected void write(XMLStreamWriter writer) throws XMLStreamException, RepositoryException {
    
    writer.writeStartElement("D", XML_MULTISTATUS, "DAV:");

    writer.writeNamespace("D", "DAV:");
    
    writer.writeStartElement("D", XML_RESPONSE, "DAV:");
    
    String responseHref = href + node.getPath();
    
    new HrefRepresentation(responseHref).write(writer);    
    
    ArrayList<PropertyRepresentation> properties = new ArrayList<PropertyRepresentation>();
    
    /*
     * Update
     */
    for (int i = 0; i < setList.size(); i++) {
      PropertyRepresentation property = setList.get(i);
      property.update(node);
      properties.add(property);
    }
    
    /*
     * Remove
     */
    for (int i = 0; i < removeList.size(); i++) {
      PropertyRepresentation property = removeList.get(i);      
      property.remove(node);      
      properties.add(property);
    }
    
    /*
     * Sort by status
     */
    HashMap<Integer, ArrayList<PropertyRepresentation>> propStatusGroup = new HashMap<Integer, ArrayList<PropertyRepresentation>>();
    
    for (int i = 0; i < properties.size(); i++) {
      PropertyRepresentation representation = properties.get(i);
      
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

      writer.writeStartElement("DAV:", WebDavProperty.PROPSTAT);
            
      writer.writeStartElement("DAV:", WebDavProperty.PROP);
      ArrayList<PropertyRepresentation> representationList = propStatusGroup.get(curStatus);
      for (int i = 0; i < representationList.size(); i++) {
        PropertyRepresentation representation = representationList.get(i);
        representation.write(writer);
      }
      writer.writeEndElement();
      
      writer.writeStartElement("DAV:", WebDavProperty.STATUS);
      writer.writeCharacters(WebDavStatus.getStatusDescription(curStatus));
      writer.writeEndElement();
      
      writer.writeEndElement();      
    }    
    
    writer.writeEndElement();
    
    writer.writeEndElement();    
    
  }

}

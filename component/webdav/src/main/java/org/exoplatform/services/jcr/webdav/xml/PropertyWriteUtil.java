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

package org.exoplatform.services.jcr.webdav.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropertyWriteUtil {

  public static void writePropStats(XMLStreamWriter xmlStreamWriter, Map<String, Set<HierarchicalProperty>> propStatuses) throws XMLStreamException {
    for (Map.Entry<String, Set<HierarchicalProperty>> stat : propStatuses.entrySet()) {    
      xmlStreamWriter.writeStartElement("DAV:", "propstat");
      
        xmlStreamWriter.writeStartElement("DAV:", "prop");
        for(HierarchicalProperty prop : propStatuses.get(stat.getKey())) {
           writeProperty(xmlStreamWriter, prop);
        }
        xmlStreamWriter.writeEndElement();
        
        xmlStreamWriter.writeStartElement("DAV:", "status");
        xmlStreamWriter.writeCharacters(stat.getKey());
        xmlStreamWriter.writeEndElement();
  
      // D:propstat
      xmlStreamWriter.writeEndElement();
    }
  }

  public static void writeProperty(XMLStreamWriter xmlStreamWriter, HierarchicalProperty prop) throws XMLStreamException {   
    String uri = prop.getName().getNamespaceURI();    
    String prefix = prop.getName().getPrefix();   
    String local = prop.getName().getLocalPart();
    
    if (prop.getValue() == null) {
      
      if (prop.getChildren().size() != 0) {
        
        xmlStreamWriter.writeStartElement(uri, local);        
        if (!uri.equalsIgnoreCase("DAV:")) {
          xmlStreamWriter.writeNamespace(prefix, uri);
        }
        
        writeAttributes(xmlStreamWriter, prop);
        
        for (int i = 0; i < prop.getChildren().size(); i++) {
          HierarchicalProperty property = prop.getChildren().get(i);
          writeProperty(xmlStreamWriter, property);
        }
        xmlStreamWriter.writeEndElement();        
      } else {
        xmlStreamWriter.writeEmptyElement(uri, local);
        if (!uri.equalsIgnoreCase("DAV:")) {
          xmlStreamWriter.writeNamespace(prefix, uri);
        }
        
        writeAttributes(xmlStreamWriter, prop);
      }
      
    } else {
      xmlStreamWriter.writeStartElement(uri, local);
      
      if (!uri.equalsIgnoreCase("DAV:")) {
        xmlStreamWriter.writeNamespace(prefix, uri);
      }
      
      writeAttributes(xmlStreamWriter, prop);
      
      xmlStreamWriter.writeCharacters(prop.getValue());
      xmlStreamWriter.writeEndElement();
    }
  }

  public static void writeAttributes(XMLStreamWriter xmlStreamWriter, HierarchicalProperty property) throws XMLStreamException {   
    HashMap<String, String> attributes = property.getAttributes();    
    Iterator<String> keyIter = attributes.keySet().iterator();
    while (keyIter.hasNext()) {
      String attrName = keyIter.next();
      String attrValue = attributes.get(attrName);
      xmlStreamWriter.writeAttribute(attrName, attrValue);
    }
  }   
  
}

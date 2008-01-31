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

package org.exoplatform.services.jcr.webdav.command.proppatch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.xml.PropertyWriteUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropPatchResponseEntity implements SerializableEntity {
  
  private final WebDavNamespaceContext nsContext; 
  
  private Node node;
  
  private final URI uri;
  
  private final List<HierarchicalProperty> setList;
  
  private final List<HierarchicalProperty> removeList;
  
  public PropPatchResponseEntity(WebDavNamespaceContext nsContext, Node node, URI uri,
      List<HierarchicalProperty> setList, List<HierarchicalProperty> removeList) {
    this.nsContext = nsContext;
    this.node = node;
    this.uri = uri;
    this.setList = setList;
    this.removeList = removeList;
  }

  public void writeObject(OutputStream outStream) throws IOException {
    try {
      XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance()
      .createXMLStreamWriter(outStream, Constants.DEFAULT_ENCODING);
    
      xmlStreamWriter.setNamespaceContext(nsContext);
      xmlStreamWriter.setDefaultNamespace("DAV:");
  
      xmlStreamWriter.writeStartDocument();
      xmlStreamWriter.writeStartElement("D", "multistatus", "DAV:");
      xmlStreamWriter.writeNamespace("D", "DAV:");
      
      xmlStreamWriter.writeAttribute("xmlns:b", "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");
  
      xmlStreamWriter.writeStartElement("DAV:", "response");
        xmlStreamWriter.writeStartElement("DAV:", "href");
          xmlStreamWriter.writeCharacters(uri.toASCIIString());
        xmlStreamWriter.writeEndElement();
        
        Map<String, Set<HierarchicalProperty>> propStats = getPropStat();    
        PropertyWriteUtil.writePropStats(xmlStreamWriter, propStats);
        
      xmlStreamWriter.writeEndElement();
  
      // D:multistatus
      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndDocument();
      
    } catch (XMLStreamException exc) {
      throw new IOException(exc.getMessage());
    }    
  }

  protected Map<String, Set<HierarchicalProperty>> getPropStat() {
    Map<String, Set<HierarchicalProperty>> propStats = new HashMap<String, Set<HierarchicalProperty>>();
    
    for (int i = 0; i < setList.size(); i++) {
      HierarchicalProperty setProperty = setList.get(i);
      
      String statname;
      try {
        String propertyName = WebDavNamespaceContext.createName(setProperty.getName());
        
        try {
          node.setProperty(propertyName, setProperty.getValue());
        } catch (RepositoryException exc) {
          String []value = new String[1];
          value[0] = setProperty.getValue();
          node.setProperty(propertyName, value);
        }
                
        statname = WebDavStatus.getStatusDescription(WebDavStatus.OK);
      } catch (AccessDeniedException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.FORBIDDEN);
      } catch (ItemNotFoundException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.NOT_FOUND);
      } catch (PathNotFoundException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.NOT_FOUND);
      } catch (RepositoryException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.INTERNAL_SERVER_ERROR);
      }

      if (!propStats.containsKey(statname)) {
        propStats.put(statname, new HashSet<HierarchicalProperty>());
      }

      Set<HierarchicalProperty> propSet = propStats.get(statname);
      propSet.add(new HierarchicalProperty(setProperty.getName()));      
    }

    for (int i = 0; i < removeList.size(); i++) {
      HierarchicalProperty removeProperty = removeList.get(i);
      
      String statname;
      try {
        String propertyName = WebDavNamespaceContext.createName(removeProperty.getName());
        
        Property property = node.getProperty(propertyName);
        property.remove();
        property.save();

        statname = WebDavStatus.getStatusDescription(WebDavStatus.OK);
      } catch (AccessDeniedException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.FORBIDDEN);
      } catch (ItemNotFoundException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.NOT_FOUND);
      } catch (PathNotFoundException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.NOT_FOUND);      
      } catch (RepositoryException e) {
        statname = WebDavStatus.getStatusDescription(WebDavStatus.INTERNAL_SERVER_ERROR);
      }

      if (!propStats.containsKey(statname)) {
        propStats.put(statname, new HashSet<HierarchicalProperty>());
      }

      Set<HierarchicalProperty> propSet = propStats.get(statname);
      propSet.add(new HierarchicalProperty(removeProperty.getName()));      
    }
    
  
    return propStats;
  }
  
}

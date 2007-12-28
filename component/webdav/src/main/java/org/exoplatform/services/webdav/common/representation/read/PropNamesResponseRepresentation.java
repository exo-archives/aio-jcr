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

public class PropNamesResponseRepresentation extends AllPropResponseRepresentation {
  
  public PropNamesResponseRepresentation(WebDavService webDavService, String href, Node node, int depth) throws RepositoryException {
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
        property.setStatus(WebDavStatus.NOT_FOUND);
        property.write(xmlStreamWriter);
      }
    }
    
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.STATUS);
    xmlStreamWriter.writeCharacters(WebDavStatus.getStatusDescription(WebDavStatus.OK));
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeEndElement();
  }
  
}

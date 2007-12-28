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

package org.exoplatform.services.webdav.common.representation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SinglePropertyRepresentation implements ResponseRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.SinglePropertyRepresentation");
  
  private PropertyRepresentation propertyRepresentation;
  
  public SinglePropertyRepresentation(PropertyRepresentation propertyRepresentation) {
    this.propertyRepresentation = propertyRepresentation;
  }

  public void init(String prefix, Node node, int maxLevel) throws RepositoryException {
  }

  public void write(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    xmlStreamWriter.writeStartElement("D", "prop", "DAV:");
    xmlStreamWriter.writeNamespace("D", "DAV:");
    
    propertyRepresentation.write(xmlStreamWriter);
    
    xmlStreamWriter.writeEndElement();
  }

}

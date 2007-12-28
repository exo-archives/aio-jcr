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

import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.RepositoryException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class XmlResponseRepresentation implements SerializableEntity {
  
  private static Log log = ExoLogger.getLogger("jcr.XmlResponseRepresentation");
  
  private WebDavNameSpaceContext nameSpaceContext;
  
  public XmlResponseRepresentation(WebDavNameSpaceContext nameSpaceContext) {
    this.nameSpaceContext = nameSpaceContext; 
  }
  
  protected abstract void write(XMLStreamWriter writer) throws XMLStreamException, RepositoryException;

  public void writeObject(OutputStream outputStream) throws IOException {
    try {
      XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, Constants.DEFAULT_ENCODING);
      
      writer.writeStartDocument(Constants.DEFAULT_ENCODING, "1.0");
      
      writer.setNamespaceContext(nameSpaceContext);
      
      write(writer);
      
      writer.writeEndDocument();
    } catch (Exception exc) {
      log.info("Unhandled ecxeption. " + exc.getMessage(), exc);
    }
    
  }  
  
}


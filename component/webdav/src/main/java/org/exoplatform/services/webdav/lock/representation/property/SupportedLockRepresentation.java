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

package org.exoplatform.services.webdav.lock.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SupportedLockRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "supportedlock";
  
  public static final String XML_LOCKENTRY = "lockentry";
  
  public static final String XML_LOCKSCOPE = "lockscope";
  
  public static final String XML_EXCLUSIVE = "exclusive";
  
  public static final String XML_LOCKTYPE = "locktype";
  
  public static final String XML_WRITE = "write";
  
  @Override
  public String getTagName() {
    return TAGNAME;
  }

  public void read(Node node) {
    try {
      if (!node.canAddMixin(DavConst.NodeTypes.MIX_LOCKABLE)) {
        return;
      }
      status = WebDavStatus.OK;      
    } catch (RepositoryException exc) {
    }
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeStartElement(getNameSpace(), XML_LOCKENTRY);    
      
      xmlWriter.writeStartElement(getNameSpace(), XML_LOCKSCOPE);
        xmlWriter.writeEmptyElement(getNameSpace(), XML_EXCLUSIVE);
      xmlWriter.writeEndElement();
      
      xmlWriter.writeStartElement(getNameSpace(), XML_LOCKTYPE);
        xmlWriter.writeEmptyElement(getNameSpace(), XML_WRITE);
      xmlWriter.writeEndElement();
    
    xmlWriter.writeEndElement();
  }  
  
}

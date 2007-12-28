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

package org.exoplatform.services.webdav.common.representation.property;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class CommonWebDavProperty implements PropertyRepresentation {
  
  protected int status = WebDavStatus.NOT_FOUND;
  
  public abstract String getTagName();
  
  public abstract String getNameSpace();
  
  protected abstract void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException;
  
  public int getStatus() {
    return status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public void write(XMLStreamWriter xmlWriter) throws XMLStreamException {
    if (status == WebDavStatus.OK) {
      xmlWriter.writeStartElement(getNameSpace(), getTagName());
      writeContent(xmlWriter);
      xmlWriter.writeEndElement();
    } else {
      xmlWriter.writeEmptyElement(getNameSpace(), getTagName());
    }
  }
  
}

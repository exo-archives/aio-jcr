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

package org.exoplatform.services.webdav.deltav.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CheckedInRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "checked-in";
  
  private String href;
  
  private HrefRepresentation hrefRepresentation;
  
  public CheckedInRepresentation(String href) {
    this.href = href;
  }

  public void read(Node node) {
    try {
      
      String hrefValue;
      
      if (node instanceof Version) {
        hrefValue = href + "?VERSIONID=" + node.getName();
      } else {
        if (node.isCheckedOut()) {
          return;
        }
        
        hrefValue = href + "?VERSIONID=" + node.getBaseVersion().getName();
      }
      
      status = WebDavStatus.OK;      
      hrefRepresentation = new HrefRepresentation(hrefValue);
    } catch (RepositoryException exc) {
    }
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    hrefRepresentation.write(xmlWriter);
  }

}

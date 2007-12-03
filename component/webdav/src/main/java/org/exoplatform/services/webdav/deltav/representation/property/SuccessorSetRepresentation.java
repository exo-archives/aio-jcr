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

import java.util.ArrayList;

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

public class SuccessorSetRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "successor-set";
  
  private String href;
  
  private ArrayList<String> successorHrefs = new ArrayList<String>();
  
  public SuccessorSetRepresentation(String href) {
    this.href = href;   
  }

  public void read(Node node) {    
    if (!(node instanceof Version)) {
      return;
    }
    
    try {      
      Version []successors = ((Version)node).getSuccessors();

      while (successors.length > 0) {
        successorHrefs.add(href + "?VERSIONID=" + successors[0].getName());
        successors = successors[0].getSuccessors();
      }      
      status = WebDavStatus.OK;
    } catch (RepositoryException exc) {
    }
    
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    for (int i = 0; i < successorHrefs.size(); i++) {
      String successorHref = successorHrefs.get(i);
      new HrefRepresentation(successorHref).write(xmlWriter);
    }
  }

}

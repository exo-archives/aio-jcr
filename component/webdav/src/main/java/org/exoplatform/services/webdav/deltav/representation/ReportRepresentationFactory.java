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

package org.exoplatform.services.webdav.deltav.representation;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.deltav.representation.versiontree.VersionTreeRepresentationFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ReportRepresentationFactory {
  
  public static final String DAV_NS = "DAV:";
    
  public static XmlResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href) throws RepositoryException, BadRequestException {    
    Node reportNode = document.getChildNodes().item(0);
    
    String nameSpace = reportNode.getNamespaceURI();
    String localName = reportNode.getLocalName();
    
    if (DAV_NS.equals(nameSpace) && VersionTreeRepresentationFactory.XML_VERSIONTREE.equals(localName)) {
      return VersionTreeRepresentationFactory.createResponseRepresentation(webDavService, document, node, href);
    }
    
    throw new BadRequestException();
  }

}

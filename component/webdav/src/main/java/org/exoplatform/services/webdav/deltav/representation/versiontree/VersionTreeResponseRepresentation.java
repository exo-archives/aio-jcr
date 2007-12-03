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

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.read.PropResponseRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class VersionTreeResponseRepresentation extends PropResponseRepresentation {
  
  public VersionTreeResponseRepresentation(WebDavService webDavService, HashMap<String, ArrayList<String>> properties, String href, Node node) throws RepositoryException {
    super(webDavService, properties, href, node, 0);
  }

  @Override
  protected void listRecursive(XMLStreamWriter xmlStreamWriter, Node curNode, int curDepth) throws XMLStreamException, RepositoryException {
    href = defaultHref + curNode.getPath();
    
    VersionHistory versionHistory = curNode.getVersionHistory();
    VersionIterator versionIterator = versionHistory.getAllVersions();
    
    while (versionIterator.hasNext()) {
      Version version = versionIterator.nextVersion();
      
      if (DavConst.NodeTypes.JCR_ROOTVERSION.equals(version.getName())) {
        continue;
      }
      
      xmlStreamWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
      String responseHref = href + "?VERSIONID=" + version.getName();
      new HrefRepresentation(responseHref).write(xmlStreamWriter);
      
      writeResponseContent(xmlStreamWriter, version);
      
      xmlStreamWriter.writeEndElement();
    }
    
  }

}

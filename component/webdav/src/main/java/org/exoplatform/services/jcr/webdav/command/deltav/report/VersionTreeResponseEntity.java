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

package org.exoplatform.services.jcr.webdav.command.deltav.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.resource.IllegalResourceTypeException;
import org.exoplatform.services.jcr.webdav.resource.VersionResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedResource;
import org.exoplatform.services.jcr.webdav.xml.PropertyWriteUtil;
import org.exoplatform.services.jcr.webdav.xml.PropstatGroupedRepresentation;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class VersionTreeResponseEntity implements StreamingOutput {

  protected XMLStreamWriter              xmlStreamWriter;

  protected final WebDavNamespaceContext namespaceContext;

  private Set<VersionResource>           versions;

  private Set<QName>                     properties;

  public VersionTreeResponseEntity(WebDavNamespaceContext namespaceContext,
                                   VersionedResource versionedResource,
                                   Set<QName> properties) throws RepositoryException,
      IllegalResourceTypeException {
    this.namespaceContext = namespaceContext;
    this.properties = properties;
    versions = versionedResource.getVersionHistory().getVersions();
  }

  public void write(OutputStream outputStream) throws IOException {
    try {
      this.xmlStreamWriter = XMLOutputFactory.newInstance()
                                             .createXMLStreamWriter(outputStream,
                                                                    Constants.DEFAULT_ENCODING);
      xmlStreamWriter.setNamespaceContext(namespaceContext);
      xmlStreamWriter.setDefaultNamespace("DAV:");

      xmlStreamWriter.writeStartDocument();
      xmlStreamWriter.writeStartElement("D", "multistatus", "DAV:");
      xmlStreamWriter.writeNamespace("D", "DAV:");

      xmlStreamWriter.writeAttribute("xmlns:b", "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");

      Iterator<VersionResource> versionIterator = versions.iterator();
      while (versionIterator.hasNext()) {
        VersionResource versionResource = versionIterator.next();
        xmlStreamWriter.writeStartElement("DAV:", "response");

        xmlStreamWriter.writeStartElement("DAV:", "href");
        xmlStreamWriter.writeCharacters(versionResource.getIdentifier().toASCIIString());
        xmlStreamWriter.writeEndElement();

        PropstatGroupedRepresentation propstat = new PropstatGroupedRepresentation(versionResource,
                                                                                   properties,
                                                                                   false);
        PropertyWriteUtil.writePropStats(xmlStreamWriter, propstat.getPropStats());

        xmlStreamWriter.writeEndElement();
      }

      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndDocument();
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }

}

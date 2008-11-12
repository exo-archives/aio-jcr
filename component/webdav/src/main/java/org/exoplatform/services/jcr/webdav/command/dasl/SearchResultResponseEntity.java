/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.webdav.command.dasl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryResult;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.FileResource;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.resource.ResourceUtil;
import org.exoplatform.services.jcr.webdav.resource.VersionedCollectionResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedFileResource;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.PropertyWriteUtil;
import org.exoplatform.services.jcr.webdav.xml.PropstatGroupedRepresentation;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class SearchResultResponseEntity implements SerializableEntity {

  private final WebDavNamespaceContext nsContext;

  private QueryResult                  queryResult;

  private String                       baseURI;

  private static Set<QName>            properties = new HashSet<QName>();

  static {
    properties.add(new QName("DAV:", "displayname"));
    properties.add(new QName("DAV:", "resourcetype"));
    properties.add(new QName("DAV:", "creationdate"));
    properties.add(new QName("DAV:", "getlastmodified"));
    properties.add(new QName("DAV:", "getcontentlength"));
  }

  public SearchResultResponseEntity(QueryResult queryResult,
                                    final WebDavNamespaceContext nsContext,
                                    String baseURI) {
    this.queryResult = queryResult;
    this.nsContext = nsContext;
    this.baseURI = baseURI;
  }

  public void writeObject(OutputStream outStream) throws IOException {
    try {
      XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance()
                                                        .createXMLStreamWriter(outStream,
                                                                               Constants.DEFAULT_ENCODING);

      xmlStreamWriter.setNamespaceContext(nsContext);
      xmlStreamWriter.setDefaultNamespace("DAV:");

      xmlStreamWriter.writeStartDocument();
      xmlStreamWriter.writeStartElement("D", "multistatus", "DAV:");
      xmlStreamWriter.writeNamespace("D", "DAV:");

      xmlStreamWriter.writeAttribute("xmlns:b", "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");

      NodeIterator nodeIter = queryResult.getNodes();
      while (nodeIter.hasNext()) {
        Node nextNode = nodeIter.nextNode();

        if (nextNode.isNodeType("nt:resource")) {
          if (nextNode.getParent().isNodeType("nt:file")) {
            nextNode = nextNode.getParent();
          } else {
            // skipping
            continue;
          }
        }

        URI uri = new URI(TextUtil.escape(baseURI + nextNode.getPath(), '%', true));

        Resource resource;
        if (ResourceUtil.isVersioned(nextNode)) {
          if (ResourceUtil.isFile(nextNode)) {
            resource = new VersionedFileResource(uri, nextNode, nsContext);
          } else {
            resource = new VersionedCollectionResource(uri, nextNode, nsContext);
          }
        } else {
          if (ResourceUtil.isFile(nextNode)) {
            resource = new FileResource(uri, nextNode, nsContext);
          } else {
            resource = new CollectionResource(uri, nextNode, nsContext);
          }
        }

        xmlStreamWriter.writeStartElement("DAV:", "response");

        xmlStreamWriter.writeStartElement("DAV:", "href");
        xmlStreamWriter.writeCharacters(resource.getIdentifier().toASCIIString());
        xmlStreamWriter.writeEndElement();

        PropstatGroupedRepresentation propstat = new PropstatGroupedRepresentation(resource,
                                                                                   properties,
                                                                                   false);

        PropertyWriteUtil.writePropStats(xmlStreamWriter, propstat.getPropStats());

        xmlStreamWriter.writeEndElement();

      }

      // D:multistatus
      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndDocument();
    } catch (XMLStreamException exc) {
      throw new IOException(exc.getMessage());
    } catch (Exception exc) {

      System.out.println("Unhandled Exception. " + exc.getMessage());
      exc.printStackTrace();

      throw new IOException(exc.getMessage());
    }
  }

}

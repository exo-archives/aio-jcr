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

package org.exoplatform.services.jcr.webdav.command.propfind;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.IllegalResourceTypeException;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.xml.PropertyWriteUtil;
import org.exoplatform.services.jcr.webdav.xml.PropstatGroupedRepresentation;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class PropFindResponseEntity implements StreamingOutput {

  /**
   * logger.
   */
  private static Log                     log = ExoLogger.getLogger(PropFindResponseEntity.class);

  /**
   * XML writer.
   */
  protected XMLStreamWriter              xmlStreamWriter;

  /**
   * Output stream.
   */
  protected OutputStream                 outputStream;

  /**
   * Namespace context.
   */
  protected final WebDavNamespaceContext namespaceContext;

  /**
   * Root resource.
   */
  protected final Resource               rootResource;

  /**
   * The list of properties to get.
   */
  protected Set<QName>                   propertyNames;

  /**
   * Request depth.
   */
  protected final int                    depth;

  /**
   * Boolean flag, shows if only property names a requested.
   */
  protected final boolean                propertyNamesOnly;

  /**
   * Constructor.
   * 
   * @param depth reqest depth.
   * @param rootResource root resource.
   * @param propertyNames the list of properties requested
   * @param propertyNamesOnly if only property names a requested
   */
  public PropFindResponseEntity(int depth,
                                Resource rootResource,
                                Set<QName> propertyNames,
                                boolean propertyNamesOnly) {
    this.rootResource = rootResource;
    this.namespaceContext = rootResource.getNamespaceContext();
    this.propertyNames = propertyNames;
    this.depth = depth;
    this.propertyNamesOnly = propertyNamesOnly;
  }

  /**
   * {@inheritDoc}
   */
  public void write(OutputStream stream) throws IOException {
    this.outputStream = stream;
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

      traverseResources(rootResource, 0);

      // D:multistatus
      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndDocument();

      // rootNode.accept(this);
    } catch (Exception exc) {
      log.error(exc.getMessage(), exc);
      throw new IOException(exc.getMessage());
    }
  }

  /**
   * Traverses resources and collects the vales of required properties.
   * 
   * @param resource resource to traverse
   * @param counter the depth
   * @throws XMLStreamException {@link XMLStreamException}
   * @throws RepositoryException {@link RepositoryException}
   * @throws IllegalResourceTypeException {@link IllegalResourceTypeException}
   * @throws URISyntaxException {@link URISyntaxException}
   */
  private void traverseResources(Resource resource, int counter) throws XMLStreamException,
                                                                RepositoryException,
                                                                IllegalResourceTypeException,
                                                                URISyntaxException {

    xmlStreamWriter.writeStartElement("DAV:", "response");

    xmlStreamWriter.writeStartElement("DAV:", "href");
    if (resource.isCollection()) {
      xmlStreamWriter.writeCharacters(resource.getIdentifier().toASCIIString() + "/");
    } else {
      xmlStreamWriter.writeCharacters(resource.getIdentifier().toASCIIString());
    }
    xmlStreamWriter.writeEndElement();

    PropstatGroupedRepresentation propstat = new PropstatGroupedRepresentation(resource,
                                                                               propertyNames,
                                                                               propertyNamesOnly);

    PropertyWriteUtil.writePropStats(xmlStreamWriter, propstat.getPropStats());

    xmlStreamWriter.writeEndElement();

    int d = depth;

    if (depth == -1) {

    }

    if (resource.isCollection()) {
      if (counter < d) {
        CollectionResource collection = (CollectionResource) resource;
        for (Resource child : collection.getResources()) {
          traverseResources(child, counter + 1);
        }
      }
    }

  }

}

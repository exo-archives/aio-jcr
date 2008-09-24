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

package org.exoplatform.services.jcr.webdav.command.order;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.jcr.Node;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class OrderPatchResponseEntity implements SerializableEntity {

  protected final WebDavNamespaceContext nsContext;

  protected final URI                    uri;

  protected Node                         node;

  protected List<OrderMember>            members;

  public OrderPatchResponseEntity(WebDavNamespaceContext nsContext,
                                  URI uri,
                                  Node node,
                                  List<OrderMember> members) {
    this.nsContext = nsContext;
    this.uri = uri;
    this.node = node;
    this.members = members;
  }

  public void writeObject(OutputStream outputStream) throws IOException {
    try {
      XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance()
                                                        .createXMLStreamWriter(outputStream,
                                                                               Constants.DEFAULT_ENCODING);
      xmlStreamWriter.setNamespaceContext(nsContext);
      xmlStreamWriter.setDefaultNamespace("DAV:");

      xmlStreamWriter.writeStartDocument();
      xmlStreamWriter.writeStartElement("D", "multistatus", "DAV:");
      xmlStreamWriter.writeNamespace("D", "DAV:");

      xmlStreamWriter.writeAttribute("xmlns:b", "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");

      for (int i = 0; i < members.size(); i++) {
        OrderMember member = members.get(i);

        xmlStreamWriter.writeStartElement("DAV:", "response");

        xmlStreamWriter.writeStartElement("DAV:", "href");
        String href = uri.toASCIIString() + "/" + TextUtil.escape(member.getSegment(), '%', true);
        xmlStreamWriter.writeCharacters(href);
        xmlStreamWriter.writeEndElement();

        xmlStreamWriter.writeStartElement("DAV:", "status");
        xmlStreamWriter.writeCharacters(WebDavStatus.getStatusDescription(member.getStatus()));
        xmlStreamWriter.writeEndElement();

        xmlStreamWriter.writeEndElement();
      }

      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeEndDocument();

    } catch (Exception exc) {
      throw new IOException();
    }

  }

}

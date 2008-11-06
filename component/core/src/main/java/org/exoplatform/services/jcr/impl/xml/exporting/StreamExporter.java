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
package org.exoplatform.services.jcr.impl.xml.exporting;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public abstract class StreamExporter extends BaseXmlExporter {

  protected final XMLStreamWriter writer;

  public StreamExporter(XMLStreamWriter writer,
                        SessionImpl session,
                        ItemDataConsumer dataManager,
                        ValueFactoryImpl systemValueFactory,
                        boolean skipBinary,
                        boolean noRecurse) throws NamespaceException, RepositoryException {

    super(session, dataManager, systemValueFactory, skipBinary, noRecurse ? 1 : -1);
    this.writer = writer;
    setNoRecurse(noRecurse);

  }

  @Override
  public void export(NodeData node) throws RepositoryException, XMLStreamException {
    if (writer != null) {
      writer.writeStartDocument(Constants.DEFAULT_ENCODING, "1.0");
      node.accept(this);
      writer.writeEndDocument();
      writer.close();
    }
  }

  protected void startPrefixMapping() throws RepositoryException, XMLStreamException {
    String[] prefixes = namespaceRegistry.getPrefixes();
    for (String prefix : prefixes) {
      // skeep xml prefix
      if ((prefix == null) || (prefix.length() < 1) || prefix.equals(Constants.NS_XML_PREFIX)) {
        continue;
      }
      writer.writeNamespace(prefix, namespaceRegistry.getURI(prefix));
    }
  };
}

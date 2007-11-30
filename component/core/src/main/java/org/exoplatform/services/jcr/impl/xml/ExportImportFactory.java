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

package org.exoplatform.services.jcr.impl.xml;

import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.xml.exporting.BaseXmlExporter;
import org.exoplatform.services.jcr.impl.xml.exporting.DocumentViewContentExporter;
import org.exoplatform.services.jcr.impl.xml.exporting.DocumentViewStreamExporter;
import org.exoplatform.services.jcr.impl.xml.exporting.SystemViewContentExporter;
import org.exoplatform.services.jcr.impl.xml.exporting.SystemViewStreamExporter;
import org.exoplatform.services.jcr.impl.xml.importing.BackupDataImporter;
import org.exoplatform.services.jcr.impl.xml.importing.ContentHandlerImporter;
import org.exoplatform.services.jcr.impl.xml.importing.StreamImporter;
import org.xml.sax.ContentHandler;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ExportImportFactory {
  private final SessionImpl sessionImpl;

  public ExportImportFactory(SessionImpl sessionImpl) {
    this.sessionImpl = sessionImpl;
  }

  public BackupDataImporter getBackupImporter(InvocationContext context) {
    return new BackupDataImporter(context);
  }

  /**
   * Create export visitor for given type of view
   * 
   * @param type - 6.4 XML Mappings
   * @param contentHandler - for which will be generate SAX events
   * @param skipBinary - If skipBinary is true then any properties of
   *          PropertyType.BINARY will be serialized as if they are empty.
   * @param noRecurse - if noRecurse is false, the whole subtree are serialized
   * @return ItemDataTraversingVisitor
   * @throws NamespaceException
   * @throws RepositoryException
   */
  public BaseXmlExporter getExportVisitor(XmlMapping type,
                                          ContentHandler contentHandler,
                                          boolean skipBinary,
                                          boolean noRecurse) throws NamespaceException,
                                                            RepositoryException {

    if (type == XmlMapping.SYSVIEW) {
      return new SystemViewContentExporter(contentHandler,
                                           sessionImpl,
                                           sessionImpl.getTransientNodesManager(),
                                           skipBinary,
                                           noRecurse);
    } else if (type == XmlMapping.DOCVIEW) {
      return new DocumentViewContentExporter(contentHandler,
                                             sessionImpl,
                                             sessionImpl.getTransientNodesManager(),
                                             skipBinary,
                                             noRecurse);
    }
    return null;
  }

  /**
   * Create export visitor for given type of view
   * 
   * @param type - 6.4 XML Mappings
   * @param stream - output result stream
   * @param skipBinary - If skipBinary is true then any properties of
   *          PropertyType.BINARY will be serialized as if they are empty.
   * @param noRecurse - if noRecurse is false, the whole subtree are serialized
   * @return
   * @throws NamespaceException
   * @throws RepositoryException
   * @throws IOException
   */
  public BaseXmlExporter getExportVisitor(XmlMapping type,
                                          OutputStream stream,
                                          boolean skipBinary,
                                          boolean noRecurse) throws NamespaceException,
                                                            RepositoryException,
                                                            IOException {

    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    XMLStreamWriter streamWriter;
    try {
      streamWriter = outputFactory.createXMLStreamWriter(stream, Constants.DEFAULT_ENCODING);
    } catch (XMLStreamException e) {
      throw new IOException(e.getLocalizedMessage());
    }

    if (type == XmlMapping.SYSVIEW) {
      return new SystemViewStreamExporter(streamWriter,
                                          sessionImpl,
                                          sessionImpl.getTransientNodesManager(),
                                          skipBinary,
                                          noRecurse);
    } else if (type == XmlMapping.DOCVIEW) {
      return new DocumentViewStreamExporter(streamWriter,
                                            sessionImpl,
                                            sessionImpl.getTransientNodesManager(),
                                            skipBinary,
                                            noRecurse);
    }
    return null;
  }

  /**
   * @param saveType
   * @param node
   * @param uuidBehavior
   * @param context
   * @return
   */
  public ContentHandler getImportHandler(NodeImpl node,
                                         int uuidBehavior,
                                         XmlSaveType saveType,
                                         InvocationContext context) {

    return new ContentHandlerImporter(node, uuidBehavior, saveType, context);
  }

  /**
   * @param saveType
   * @param node
   * @param uuidBehavior
   * @param context
   * @return
   */
  public StreamImporter getStreamImporter(NodeImpl node,
                                          int uuidBehavior,
                                          XmlSaveType saveType,
                                          InvocationContext context) {

    return new StreamImporter(node, uuidBehavior, saveType, context);
  }
}

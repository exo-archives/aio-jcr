/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.xml.exporting.ContentHandlerDocExport;
import org.exoplatform.services.jcr.impl.xml.exporting.ContentHandlerSysExport;
import org.exoplatform.services.jcr.impl.xml.exporting.ExportXmlBase;
import org.exoplatform.services.jcr.impl.xml.exporting.StreamDocExport;
import org.exoplatform.services.jcr.impl.xml.exporting.StreamSysExport;
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
  public ExportXmlBase getExportVisitor(XmlMapping type,
                                        ContentHandler contentHandler,
                                        boolean skipBinary,
                                        boolean noRecurse) throws NamespaceException,
      RepositoryException {

    if (type == XmlMapping.SYSVIEW) {
      return new ContentHandlerSysExport(contentHandler,
                                         sessionImpl,
                                         sessionImpl.getTransientNodesManager(),
                                         skipBinary,
                                         noRecurse);
    } else if (type == XmlMapping.DOCVIEW) {
      return new ContentHandlerDocExport(contentHandler,
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
  public ExportXmlBase getExportVisitor(XmlMapping type,
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
      return new StreamSysExport(streamWriter,
                                 sessionImpl,
                                 sessionImpl.getTransientNodesManager(),
                                 skipBinary,
                                 noRecurse);
    } else if (type == XmlMapping.DOCVIEW) {
      return new StreamDocExport(streamWriter,
                                 sessionImpl,
                                 sessionImpl.getTransientNodesManager(),
                                 skipBinary,
                                 noRecurse);
    }
    return null;
  }

  public ContentHandler getImportHandler(XmlSaveType saveType,
                                         NodeImpl node,
                                         int uuidBehavior,
                                         boolean respectPropertyDefinitionsConstraints) {

    return new ContentHandlerImporter(saveType, node, uuidBehavior, respectPropertyDefinitionsConstraints);
  }

  public StreamImporter getStreamImporter(XmlSaveType saveType,
                                          NodeImpl node,
                                          int uuidBehavior,
                                          boolean respectPropertyDefinitionsConstraints) {

    return new StreamImporter(saveType, node, uuidBehavior, respectPropertyDefinitionsConstraints);
  }

}

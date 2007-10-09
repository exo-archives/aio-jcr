/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.exporting;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public abstract class StreamExporter extends BaseXmlExporter {

  protected final XMLStreamWriter writer;

  public StreamExporter(XMLStreamWriter writer,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) throws NamespaceException, RepositoryException {

    super(session, dataManager, skipBinary, noRecurse ? 1 : -1);
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

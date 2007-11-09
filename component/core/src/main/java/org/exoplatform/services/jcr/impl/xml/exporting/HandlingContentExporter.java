/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.exporting;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public abstract class HandlingContentExporter extends BaseXmlExporter {

  protected final ContentHandler contentHandler;

  public HandlingContentExporter(ContentHandler handler,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) throws NamespaceException, RepositoryException {

    super(session, dataManager, skipBinary, noRecurse ? 1 : -1);
    this.contentHandler = handler;
    setNoRecurse(noRecurse);
  }

  @Override
  public void export(NodeData node) throws RepositoryException, SAXException {
    if (contentHandler != null) {
      contentHandler.startDocument();
      startPrefixMapping();
      node.accept(this);
      endPrefixMapping();
      contentHandler.endDocument();
    }
  }

  protected void endPrefixMapping() throws RepositoryException, SAXException {
    String[] prefixes = session.getNamespacePrefixes();
    for (String prefix : prefixes) {
      contentHandler.endPrefixMapping(prefix);
    }
  }

  protected void startPrefixMapping() throws RepositoryException, SAXException {
    String[] prefixes = namespaceRegistry.getPrefixes();
    for (String prefix : prefixes) {
      // skeep xml prefix
      if ((prefix == null) || (prefix.length() < 1) || prefix.equals(Constants.NS_XML_PREFIX)) {
        continue;
      }
      contentHandler.startPrefixMapping(prefix, session.getNamespaceURI(prefix));
    }
  }
}

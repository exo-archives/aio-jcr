/***************************************************************************
 * Copyright 2001-2005 The eXo Platform SARL                               *
 * All rights reserved.                                                    *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Created by The eXo Platform SARL .
 * 
 * Class used as wrapper for ContentHandler, if base ContentHandler not
 * specified all content events reported by the SAX parser will be silently
 * ignored
 * 
 * @author <a href="mailto:alex.kravchuk@gmail.com">Alexander Kravchuk </a>
 * @version $Id: ContentHandlerWrapper.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class ContentHandlerWrapper implements ContentHandler {
  protected Log log = LogFactory.getLog(this.getClass());

  private ContentHandler contentHandler = null;

  /** map of current namespace scope */
  private Map currentNamespaceScope = new Hashtable();

  /** Collection new added Namespaces */
  private Collection newNamespaces = new ArrayList();

  public ContentHandlerWrapper(ContentHandler contentHandler) {
    log.debug("Created, specified contentHandler is null? - "
        + (contentHandler == null));
    this.contentHandler = contentHandler;
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    log.trace("characters char[" + new String(ch, 0, length) + "] start["
        + start + "] length[" + length + "]");

    if (contentHandler != null) {
      contentHandler.characters(ch, start, length);
    }
  }

  public void endDocument() throws SAXException {
    log.trace("endDocument");

    if (contentHandler != null) {
      contentHandler.endDocument();
    }
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    log.trace("endElement namespaceURI[" + namespaceURI + "] localName["
        + localName + "] qName[" + qName + "]");

    if (contentHandler != null) {
      contentHandler.endElement(namespaceURI, localName, qName);
    }

  }

  public void endPrefixMapping(String prefix) throws SAXException {
    log.trace("endPrefixMapping prefix[" + prefix + "]");

    currentNamespaceScope.remove(prefix);
    newNamespaces.remove(prefix);

    if (contentHandler != null) {
      contentHandler.endPrefixMapping(prefix);
    }

  }

  public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException {
    log.trace("ignorableWhitespace char[" + new String(ch, start, length)
        + "] start[" + start + "] length[" + length + "]");

    if (contentHandler != null) {
      contentHandler.ignorableWhitespace(ch, start, length);
    }

  }

  public void processingInstruction(String target, String data)
      throws SAXException {
    log.trace("processingInstruction target[" + target + "] + data[" + data
        + "]");

    if (contentHandler != null) {
      contentHandler.processingInstruction(target, data);
    }
  }

  public void setDocumentLocator(Locator locator) {
    log.trace("setDocumentLocator");

    if (contentHandler != null) {
      contentHandler.setDocumentLocator(locator);
    }

  }

  public void skippedEntity(String name) throws SAXException {
    log.trace("skippedEntity name[" + name + "]");

    if (contentHandler != null) {
      contentHandler.skippedEntity(name);
    }

  }

  public void startDocument() throws SAXException {
    log.trace("startDocument");

    if (contentHandler != null) {
      contentHandler.startDocument();
    }
  }

  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException {
    log.trace("startElement namespaceURI[" + namespaceURI + "] localName["
        + localName + "] qName[" + qName + "]atts count [" + atts.getLength()
        + "]");

    //Attributes newAtts = addNamespaceDefinitionsToAttr(atts);
    if (contentHandler != null) {
      contentHandler.startElement(namespaceURI, localName, qName, atts);
    } else {

    }

  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    log.trace("startPrefixMapping prefix[" + prefix + "] uri[" + uri + "]");

    //save namespace mapping
    if (!currentNamespaceScope.containsKey(prefix)
        || !currentNamespaceScope.get(prefix).equals(uri)) {
      currentNamespaceScope.put(prefix, uri);
      newNamespaces.add(prefix);
    }

    if (contentHandler != null) {
      contentHandler.startPrefixMapping(prefix, uri);
    }

  }
  
  private Attributes addNamespaceDefinitionsToAttr(Attributes atts) {

    Attributes newAtts = atts;

    if (newNamespaces.size() > 0 && atts != null) {
      AttributesImpl attsImpl = new AttributesImpl(atts);
      for (Iterator prefixIterator = newNamespaces.iterator(); prefixIterator
          .hasNext();) {
        String prefix = (String) prefixIterator.next();
        if (attsImpl.getIndex("xmlns:".concat(prefix)) == -1)
          attsImpl.addAttribute("", prefix, "xmlns:".concat(prefix), "CDATA",
              (String) currentNamespaceScope.get(prefix));

      }
      newNamespaces.clear();
      newAtts = attsImpl;
    }
    return newAtts;
  }

}
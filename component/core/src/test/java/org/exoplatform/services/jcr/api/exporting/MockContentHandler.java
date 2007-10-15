/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.exporting;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 18 ao�t 2004
 */
public class MockContentHandler implements ContentHandler {
  public boolean reached;
  public int nodes;
  public int properties;
  public int docElement;

  public void endDocument() throws SAXException {
  }

  public void startDocument() throws SAXException {
    reached = true;
  }

  public void characters(char ch[], int start, int length) throws SAXException {
  }

  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void skippedEntity(String name) throws SAXException {
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
  }

  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts) throws SAXException {
    if ("sv:node".equals(qName))
      nodes++;
    else if ("sv:property".equals(qName))
      properties++;
    else
      docElement++;


  }
}


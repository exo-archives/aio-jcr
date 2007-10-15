/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

import javax.jcr.RepositoryException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version 
 */
public class DocViewContentHandler /*implements ContentHandler */{
  private StringBuffer buffer;
  protected String encoding;
  
  public DocViewContentHandler(String encoding)
  {
    this.encoding = encoding;
    this.buffer = new StringBuffer();
    
  }
  public void characters(char[] ch, int start, int length) throws SAXException {
    // TODO Auto-generated method stub

  }

  public void endDocument() throws SAXException {
    // TODO Auto-generated method stub

  }

  public void endElement(String uri, String localName, String name) throws SAXException {
    // TODO Auto-generated method stub
    
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    // TODO Auto-generated method stub

  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    // TODO Auto-generated method stub

  }

  public void processingInstruction(String target, String data) throws SAXException {
    // TODO Auto-generated method stub

  }

  public void setDocumentLocator(Locator locator) {
    // TODO Auto-generated method stub

  }

  public void skippedEntity(String name) throws SAXException {
    // TODO Auto-generated method stub

  }

  public void startDocument() throws SAXException {
    this.buffer.append("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");
 }

  public void startElement(String uri, String localName, String name, Attributes atts)
      throws SAXException {
    // TODO Auto-generated method stub

  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    writeAttribute("xmlns:" + prefix, uri);

  }
  
  public String getContentAsString(){
    return buffer.toString();
    
    
  }
  private void writeAttribute(String qname, String value) {
    buffer.append(" " + qname + "=\"" + value + "\"");
  }

}

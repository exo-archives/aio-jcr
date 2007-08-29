/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ContentHandlerImporter implements ContentHandler,ErrorHandler {
  private Importer          importer;

  private final NodeImpl    parent;

  private final XmlSaveType saveType;

  private final int         uuidBehavior;

  public ContentHandlerImporter(XmlSaveType saveType, NodeImpl parent, int uuidBehavior) {
    super();
    this.saveType = saveType;
    this.uuidBehavior = uuidBehavior;
    this.parent = parent;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char ch[], int start, int length) throws SAXException {
    try {
      importer.characters(ch, start, length);
    } catch (RepositoryException e) {
      throw new SAXException(e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  public void endDocument() throws SAXException {
    try {
      importer.save();
    } catch (RepositoryException e) {
      throw new SAXException(e);
    }catch (IllegalStateException e) {
      throw new SAXException(e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String qName) throws SAXException {
    try {
      importer.endElement(uri, localName, qName);
    } catch (RepositoryException e) {
      throw new SAXException(e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  public void endPrefixMapping(String arg0) throws SAXException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
   */
  public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
   *      java.lang.String)
   */
  public void processingInstruction(String arg0, String arg1) throws SAXException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
   */
  public void setDocumentLocator(Locator arg0) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  public void skippedEntity(String arg0) throws SAXException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  public void startDocument() throws SAXException {
    this.importer = new NeutralImporter(parent, uuidBehavior, saveType);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    try {
      // /!!!!
      Map<String, String> attribute = new HashMap<String, String>();
      for (int i = 0; i < atts.getLength(); i++) {
        attribute.put(atts.getQName(i), atts.getValue(i));
      }

      importer.startElement(uri, localName, qName, attribute);
    } catch (RepositoryException e) {
      //e.printStackTrace();
      throw new SAXException(e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
   *      java.lang.String)
   */
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    importer.registerNamespace(prefix, uri);

  }

  public void error(SAXParseException exception) throws SAXException {
    // TODO Auto-generated method stub
    
  }

  public void fatalError(SAXParseException exception) throws SAXException {
    // TODO Auto-generated method stub
    
  }

  public void warning(SAXParseException exception) throws SAXException {
    // TODO Auto-generated method stub
    
  }

}

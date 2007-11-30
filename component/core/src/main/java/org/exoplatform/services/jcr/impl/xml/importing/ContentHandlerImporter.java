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

package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.exoplatform.services.ext.action.InvocationContext;
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
public class ContentHandlerImporter implements ContentHandler, ErrorHandler, RawDataImporter {
  private ContentImporter         importer;

  private final NodeImpl          parent;

  private final XmlSaveType       saveType;

  private final int               uuidBehavior;

  private final InvocationContext context;

  public ContentHandlerImporter(NodeImpl parent,
                                int uuidBehavior,
                                XmlSaveType saveType,
                                InvocationContext context) {
    super();
    this.saveType = saveType;
    this.uuidBehavior = uuidBehavior;
    this.parent = parent;
    this.context = context;

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
   * @see org.exoplatform.services.jcr.impl.xml.importing.RawDataImporter#createContentImporter(org.exoplatform.services.jcr.impl.core.NodeImpl,
   *      int, org.exoplatform.services.jcr.impl.xml.XmlSaveType,
   *      org.exoplatform.services.ext.action.InvocationContext)
   */
  public ContentImporter createContentImporter(NodeImpl parent,
                                               int uuidBehavior,
                                               XmlSaveType saveType,
                                               InvocationContext context) {
    return new NeutralImporter(parent, uuidBehavior, saveType, context);

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
      e.printStackTrace();
      throw new SAXException(e);
    } catch (IllegalStateException e) {
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

  public void error(SAXParseException exception) throws SAXException {
    // TODO Auto-generated method stub

  }

  public void fatalError(SAXParseException exception) throws SAXException {
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
    this.importer = createContentImporter(parent, uuidBehavior, saveType, context);

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
      // e.printStackTrace();
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

  public void warning(SAXParseException exception) throws SAXException {
    // TODO Auto-generated method stub

  }

}

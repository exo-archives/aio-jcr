/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.Map;

import javax.jcr.RepositoryException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public interface Importer {
  /**
   * Receive notification of character data.
   * 
   * @param ch
   * @param start
   * @param length
   * @throws RepositoryException
   */
  public void characters(char[] ch, int start, int length) throws RepositoryException;

  /**
   * Receive notification of the end of an element.
   * <p>
   * The parser will invoke this method at the end of every element in the XML
   * document; there will be a corresponding startElement event for every
   * endElement event (even when the element is empty).
   * </p>
   * <p>
   * For information on the names, see startElement.
   * </p>
   * 
   * @param uri the Namespace URI, or the empty string if the element has no
   *          Namespace URI or if Namespace processing is not being performed
   * @param localName the local name (without prefix), or the empty string if
   *          Namespace processing is not being performed
   * @param qName the qualified XML name (with prefix), or the empty string if
   *          qualified names are not available
   */
  public void endElement(String uri, String localName, String qName) throws RepositoryException;

  public void registerNamespace(String prefix, String uri);

  public void save() throws RepositoryException;

  /**
   * Receive notification of the beginning of an element.
   * <p>
   * The Parser will invoke this method at the beginning of every element in the
   * XML document; there will be a corresponding {@link #endElement endElement}
   * event for every startElement event (even when the element is empty). All of
   * the element's content will be reported, in order, before the corresponding
   * endElement event.
   * </p>
   * 
   * @param uri the Namespace URI, or the empty string if the element has no
   *          Namespace URI or if Namespace processing is not being performed
   * @param localName the local name (without prefix), or the empty string if
   *          Namespace processing is not being performed
   * @param qName the qualified name (with prefix), or the empty string if
   *          qualified names are not available
   * @param atts the attributes attached to the element. If there are no
   *          attributes, it shall be an empty object. The value of this object
   *          after startElement returns is undefined
   * @throws org.xml.sax.SAXException any SAX exception, possibly wrapping
   *           another exception
   * @see #endElement
   * @see org.xml.sax.Attributes
   * @see org.xml.sax.helpers.AttributesImpl
   */
  public void startElement(String namespaceURI,
                           String localName,
                           String qName,
                           Map<String, String> atts) throws RepositoryException;
}

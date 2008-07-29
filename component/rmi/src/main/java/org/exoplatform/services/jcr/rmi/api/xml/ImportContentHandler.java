/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exoplatform.services.jcr.rmi.api.xml;

import java.io.ByteArrayOutputStream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



/**
 * Base class for a SAX content handler for importing XML data. This class
 * provides a general mechanism for converting a SAX event stream to raw XML
 * data and feeding the received byte array into an import method. Subclasses
 * can provide different import mechanisms simply by implementing the abstract
 * {@link #importXML(byte[]) importXML(byte[])} method.
 */
public abstract class ImportContentHandler implements ContentHandler, ErrorHandler {

  /** Internal buffer for the XML byte stream. */
  private ByteArrayOutputStream buffer;

  /** The internal XML serializer. */
  private Session               session;
  private TransformerHandler handler;
  /**
   * Creates a SAX content handler for importing XML data.
   * 
   * @throws RepositoryException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws VersionException
   */

  public ImportContentHandler(Session session, String absPath) throws VersionException,
      ConstraintViolationException, LockException, RepositoryException {
    this.session = session;

    checkNodeImport(absPath);
    this.buffer = new ByteArrayOutputStream();

      SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
      try {
        handler = tf.newTransformerHandler();
      } catch (TransformerConfigurationException e) {
        throw new RepositoryException(e);
      }
      Transformer serializer = handler.getTransformer();
      serializer.setOutputProperty(OutputKeys.INDENT, "no");
      serializer.setOutputProperty(OutputKeys.METHOD, "xml");
      
      
      StreamResult streamResult = new StreamResult(buffer);
      
      handler.setResult(streamResult);
  }     
  
  /**
   * Imports the given XML data. This method is called by the
   * {@link #endDocument() endDocument()} method after the received XML stream
   * has been serialized.
   * <p>
   * Subclasses must implement this method to provide the actual import
   * mechanism.
   * 
   * @param xml the XML data to import
   * @throws Exception on import errors
   */
  protected abstract void importXML(byte[] xml) throws Exception;

  /** {@inheritDoc} */
  public void setDocumentLocator(Locator locator) {
    handler.setDocumentLocator(locator);
  }

  /** {@inheritDoc} */
  public void startDocument() throws SAXException {
    handler.startDocument();
  }

  /** {@inheritDoc} */
  public void endDocument() throws SAXException {
    handler.endDocument();
    try {
      importXML(buffer.toByteArray());
    } catch (Exception ex) {
      throw new SAXException(ex);
    }
  }

  /** {@inheritDoc} */
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    handler.startPrefixMapping(prefix, uri);
  }

  /** {@inheritDoc} */
  public void endPrefixMapping(String prefix) throws SAXException {
    handler.endPrefixMapping(prefix);
  }

  /** {@inheritDoc} */
  public void startElement(String uri, String localName, String qName, Attributes atts)
      throws SAXException {
    handler.startElement(uri, localName, qName, atts);
  }

  /** {@inheritDoc} */
  public void endElement(String uri, String localName, String qName) throws SAXException {
    handler.endElement(uri, localName, qName);
  }

  /** {@inheritDoc} */
  public void characters(char[] ch, int start, int length) throws SAXException {
    handler.characters(ch, start, length);
  }

  /** {@inheritDoc} */
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    handler.ignorableWhitespace(ch, start, length);
  }

  /** {@inheritDoc} */
  public void processingInstruction(String target, String data) throws SAXException {
    handler.processingInstruction(target, data);
  }

  /** {@inheritDoc} */
  public void skippedEntity(String name) throws SAXException {
    handler.skippedEntity(name);
  }

  // ============ org.xml.sax.ErrorHandler implementation ============

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException err) throws SAXException {
    // TODO Auto-generated method stub
    errorErrorLog(null, err);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException err) throws SAXException {
    // TODO Auto-generated method stub
    fatalErrorLog(null, err);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException err) throws SAXException {
    // TODO Auto-generated method stub
    warnErrorLog(null, err);
  }

  private void warnErrorLog(String message, Throwable exception) {
    toErrorLog("[WARN] " + (message != null && message.length() > 0 ? message + "; " : ""),
        exception);
  }

  private void errorErrorLog(String message, Throwable exception) {
    toErrorLog("[ERROR] " + (message != null && message.length() > 0 ? message + "; " : ""),
        exception);
  }

  private void fatalErrorLog(String message, Throwable exception) {
    toErrorLog("[FATAL] " + (message != null && message.length() > 0 ? message + "; " : ""),
        exception);
  }

  private void toErrorLog(String message, Throwable exception) {

    // very simple impl, Peter Nedonosko
    String err = "ErrorHandler (exo-jcr NodeImporter) "
        + (message != null && message.length() > 0 ? message + ": " : "") + "Exception: "
        + exception;
    // PrintStream errStream = (userErrorStream != null ? userErrorStream :
    // System.err);
    System.err.println(err);
    exception.printStackTrace(System.err);
  }

  private void checkNodeImport(String absNodePath) throws VersionException,
      ConstraintViolationException, LockException, RepositoryException {
    checkNodeImport((NodeImpl) session.getItem(absNodePath));
  }

  private void checkNodeImport(NodeImpl node) throws VersionException, ConstraintViolationException,
      LockException, RepositoryException {
    // checked-in check
    if (!node.checkedOut()) {
      throw new VersionException("Node " + node.getPath()
          + " or its nearest ancestor is checked-in");
    }

    // Check if node is not protected
    if (node.getDefinition().isProtected()) {
      throw new ConstraintViolationException("Can't add protected node " + node.getName() + " to "
          + node.getParent().getPath());
    }

    // Check locking
    if (node.isLocked()) {
      throw new LockException("Node " + node.getPath() + " is locked ");
    }
  }
}

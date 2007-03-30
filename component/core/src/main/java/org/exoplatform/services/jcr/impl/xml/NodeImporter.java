/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.NodeTypeRecognizer;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Created by The eXo Platform SARL .
 *
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: NodeImporter.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class NodeImporter extends DefaultHandler {
  protected static Log                   log = ExoLogger.getLogger("jcr.NodeImporter");
  
  public static final int   SAVETYPE_NONE   = 1;

  public static final int   SAVETYPE_UPDATE = 2;

  public static final int   SAVETYPE_SAVE   = 4;

  private int               saveType;

  private ContentHandler    wrappedHandler;

  protected NodeImpl        parent;

  protected int             uuidBehavior    = -1;

  private NamespaceRegistry nsReg;

  private SessionImpl       session;

  private PrintStream       userErrorStream = null;

  private boolean           saveOnEnd       = false;

  public NodeImporter(NodeImpl parent) throws RepositoryException {
    this.parent = parent;
    this.nsReg = parent.getSession().getWorkspace().getNamespaceRegistry();
    this.session = (SessionImpl) parent.getSession();
    setSaveType(SAVETYPE_NONE);
  }

  public void parse(InputStream stream) throws IOException, SAXException,
      ParserConfigurationException, RepositoryException {

    // parse XML
    XMLReader reader;
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      reader = factory.newSAXParser().getXMLReader();
    } catch (Exception e) {
      throw new SAXException(e.getMessage());
    }
    reader.setFeature("http://apache.org/xml/features/allow-java-encodings",true);
    reader.setContentHandler(this);
    reader.setErrorHandler(this);
    reader.parse(new InputSource(stream));

  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (wrappedHandler == null)
      throw new SAXException("Handler is not set.");
    wrappedHandler.characters(ch, start, length);
  }

  public void endDocument() throws SAXException {
    if (wrappedHandler == null)
      throw new SAXException("Handler is not set.");
    wrappedHandler.endDocument();
      try {
        List<ItemState> itemStates = null;
        if (wrappedHandler instanceof ImporterBase){
          itemStates = ((ImporterBase) wrappedHandler).getItemStatesList();
        }else{
          throw new  RepositoryException("wrong handler");
        }
        switch (getSaveType()) {
        case SAVETYPE_UPDATE:
          for (ItemState itemState : itemStates) {
            switch (itemState.getState()) {
            case ItemState.ADDED:
              session.getTransientNodesManager().update(itemState, true);
              break;
            case ItemState.DELETED:
              session.getTransientNodesManager().delete(itemState.getData());
            default:
              break;
            }

          }
          break;
        case SAVETYPE_SAVE:
          PlainChangesLogImpl changesLog = new PlainChangesLogImpl(itemStates, session.getId());
          session.getTransientNodesManager().getTransactManager().save(changesLog);
          break;
        case SAVETYPE_NONE:
        default:
          break;
        }

      } catch (RepositoryException e) {
        // TODO Auto-generated catch block
        throw new SAXException(e.getMessage(), e);
    }
  }

  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    if (wrappedHandler == null)
      throw new SAXException("Handler is not set.");
    wrappedHandler.endElement(namespaceURI, localName, qName);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void skippedEntity(String name) throws SAXException {
  }

  public void startDocument() throws SAXException {
  }

  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws SAXException {
    if (wrappedHandler == null) {
      try {
        recognizeDocumentType(namespaceURI, qName);
      } catch (RepositoryException e) {
        throw new SAXException(e);
      }
    }
    wrappedHandler.startElement(namespaceURI, localName, qName, atts);
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {

    try {
      nsReg.getPrefix(uri);
    } catch (NamespaceException e) {
      try {
        nsReg.registerNamespace(prefix, uri);
      } catch (NamespaceException e1) {
        throw new RuntimeException(e1);
      } catch (RepositoryException e1) {
        throw new RuntimeException(e1);
      }

    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  protected final void recognizeDocumentType(String namespaceURI, String qName)
      throws RepositoryException {

    if (NodeTypeRecognizer.recognize(namespaceURI, qName) == NodeTypeRecognizer.SYS) {
      wrappedHandler = new SysNodeImporter(parent, uuidBehavior);
    } else {
      wrappedHandler = new DocNodeImporter(parent, uuidBehavior);
    }
  }

  protected final void recognizeDocumentType(InputStream is) throws IOException, SAXException,
      ParserConfigurationException, RepositoryException {

    if (NodeTypeRecognizer.recognize(is) == NodeTypeRecognizer.SYS) {
      wrappedHandler = new SysNodeImporter(parent, uuidBehavior);
    } else {
      DocNodeImporter importer = new DocNodeImporter(parent, uuidBehavior);
      importer.setSaveOnEnd(isSaveOnEnd());
      wrappedHandler = importer;
    }
  }

  public void setUuidBehavior(int uuidBehavior) {
    this.uuidBehavior = uuidBehavior;
  }

  public int getUuidBehavior() {
    return uuidBehavior;
  }

  // ============ org.xml.sax.ErrorHandler implementation ============

  /*
   * (non-Javadoc)
   *
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException err) throws SAXException {
    // TODO Auto-generated method stub
    errorErrorLog(err.getLocalizedMessage(), err);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException err) throws SAXException {
    // TODO Auto-generated method stub
    fatalErrorLog(err.getLocalizedMessage(), err);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException err) throws SAXException {
    // TODO Auto-generated method stub
    warnErrorLog(err.getLocalizedMessage(), err);
  }

  private void warnErrorLog(String message, Throwable exception) {
//    toErrorLog("[WARN] " + (message != null && message.length() > 0 ? message + "; " : ""),
//        exception);
    log.warn(message);
  }

  private void errorErrorLog(String message, Throwable exception) {
//    toErrorLog("[ERROR] " + (message != null && message.length() > 0 ? message + "; " : ""),
//        exception);
    log.error(message);
  }

  private void fatalErrorLog(String message, Throwable exception) {
//    toErrorLog("[FATAL] " + (message != null && message.length() > 0 ? message + "; " : ""),
//        exception);
    log.fatal(message);
  }
  @Deprecated
  private void toErrorLog(String message, Throwable exception) {

    // very simple impl, Peter Nedonosko
    String err = "ErrorHandler (exo-jcr NodeImporter) "
        + (message != null && message.length() > 0 ? message + ": " : "") + "Exception: "
        + exception;
    PrintStream errStream = (userErrorStream != null ? userErrorStream : System.err);
    errStream.println(err);
    exception.printStackTrace(errStream);
  }

  public void setUserErrorStream(PrintStream printStream) {
    userErrorStream = printStream;
  }

  public PrintStream getUserErrorStream() {
    return userErrorStream;
  }

  public boolean isSaveOnEnd() {
    return saveOnEnd;
  }

  @Deprecated
  public void setSaveOnEnd(boolean saveOnEnd) {
    this.saveOnEnd = saveOnEnd;
  }

  /**
   * @return the saveType
   */
  public int getSaveType() {
    return saveType;
  }

  /**
   * @param saveType the saveType to set
   */
  public void setSaveType(int saveType) {
    this.saveType = saveType;
  }
  /**
   * Resolve an external entity.
   *
   * <p>Always return null, so that the parser will use the system
   * identifier provided in the XML document.  This method implements
   * the SAX default behaviour: application writers can override it
   * in a subclass to do special translations such as catalog lookups
   * or URI redirection.</p>
   *
   * @param publicId The public identifer, or null if none is
   *                 available.
   * @param systemId The system identifier provided in the XML 
   *                 document.
   * @return The new input source, or null to require the
   *         default behaviour.
   * @exception java.io.IOException If there is an error setting
   *            up the new input source.
   * @exception org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.EntityResolver#resolveEntity
   */
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
      IOException {
    // TODO Auto-generated method stub
    return null;
  }
  /**
   * Receive notification of a notation declaration.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass if they wish to keep track of the notations
   * declared in a document.</p>
   *
   * @param name The notation name.
   * @param publicId The notation public identifier, or null if not
   *                 available.
   * @param systemId The notation system identifier.
   * @exception org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.DTDHandler#notationDecl
   */
  public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    // TODO Auto-generated method stub
    
  }
  /**
   * Receive notification of an unparsed entity declaration.
   *
   * <p>By default, do nothing.  Application writers may override this
   * method in a subclass to keep track of the unparsed entities
   * declared in a document.</p>
   *
   * @param name The entity name.
   * @param publicId The entity public identifier, or null if not
   *                 available.
   * @param systemId The entity system identifier.
   * @param notationName The name of the associated notation.
   * @exception org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   */
  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
      throws SAXException {
    // TODO Auto-generated method stub
    
  }
}

/***************************************************************************
 * Copyright 2001-2005 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL
 *  Base class for export workspace data to SAX events
 *
 * @author <a href="mailto:alex.kravchuk@gmail.com">Alexander Kravchuk</a>
 * @version $Id: ExporterBase.java 13463 2007-03-16 09:17:29Z geaz $
 */

abstract public class ExporterBase {
  protected static Log log = ExoLogger.getLogger("jcr.ExporterBase");

  protected Session session;

  /**
   * store set of session nemespace.
   * Namespace prefix used as key
   */
  private Map sessionNamespaces = new Hashtable();

  public ExporterBase(Session session) throws RepositoryException {
    if (session == null)
      throw new java.lang.IllegalArgumentException("session can't be null");
    this.session = session;

  }

  /**
   * wrapped content handler, to avoid NullPointerException
   */
  protected ContentHandler contentHandler = new ContentHandlerWrapper(null);

  private ContentHandler customerContentHandler;

  /**
   * Return the current content handler
   */
  public ContentHandler getContentHandler() {
    return customerContentHandler;
  }

  /**
   * Allow an application to register a content event handler.
   *  If the application does not register a content handler,
   * all content events reported by the SAX parser will be silently ignored.
   * Applications may register a new or different handler in the middle of a
   * parse, and the SAX parser must begin using the new handler immediately.
   * @param handler ContentHandler The content handler
   * @throws NullPointerException If the handler argument is null.
   */
  public void setContentHandler(ContentHandler handler) {
    if (handler == null)
      throw new NullPointerException("Can't set null handler");
    this.customerContentHandler = handler;
    this.contentHandler = new ContentHandlerWrapper(handler);
  }

  private boolean recurse = false;

  /**
   * if <code>true</code> then entire subtree rooted at specified node
   * is serialized
   * @return boolean
   */

  public boolean isRecurse() {
    return recurse;
  }

  public void setRecurse(boolean isRecurse) {
    this.recurse = isRecurse;
  }

  /**
   * Actual value of each <code>PropertyType.BINARY</code> property
   * is recorded using Base64 encoding
   */
  public static final int BINARY_PROCESS = 0;

  /**
   * Any properties of <code>PropertyType.BINARY</code> will be ignored
   *  and will not appear in the serialized output
   */
  public static final int BINARY_SKIP = 1;

  /**
   * Serialized only definition of of each <code>PropertyType.BINARY</code>
   * property without values
   */
  public static final int BINARY_EMPTY = 2;

  private int binaryConduct = BINARY_PROCESS;

  /**
   * Specify how properties of <code>PropertyType.BINARY</code> serialized
   * @see <code>BINARY_PROCESS</code>,<code>BINARY_SKIP</code>,
   * <code>BINARY_EMPTY</code>
   */
  public int getBinaryConduct() {
    return binaryConduct;
  }

  public void setBinaryConduct(int binaryConduct) {
    if (binaryConduct != BINARY_PROCESS && binaryConduct != BINARY_SKIP
        && binaryConduct != BINARY_EMPTY)
      throw new java.lang.IllegalArgumentException(
          "binaryConduct must be one of "
              + "BINARY_PROCESS,BINARY_SKIP, BINARY_EMPTY");
    this.binaryConduct = binaryConduct;
  }

  /**
   * @deprecated
   * @todo test with big InputStreams
   */
  private String getInputStreamAsBase64(InputStream inputStream) throws java.io.IOException {
    StringBuffer strBuf = new StringBuffer("");
    long totalReadBytes = 0l;
    if (!(inputStream == null)) {
      byte[] byteArray = new byte[1024];
      while (inputStream.available() > 0) {
        int readBytes = inputStream.read(byteArray);
        totalReadBytes += readBytes;

        //trim array
        byte[] readByteArray;
        if (readBytes == byteArray.length)
          readByteArray = byteArray;
        else {
          readByteArray = new byte[readBytes];
          System.arraycopy(byteArray, 0, readByteArray, 0, readBytes);
        }
        strBuf.append(new String(Base64.encodeBase64(readByteArray)));
      }
    }
    log.debug("Read " + totalReadBytes + " bytes from binary value");
    return strBuf.toString();
  }
  
  //helpers
  /**
   * return Value as string
   * Binary values encoded by BASE64
   */
  protected String getValueAsString(Value value) throws RepositoryException {
    if (value.getType() != PropertyType.BINARY)
      return value.getString();
      //return StringConverter.normalizeString(value.getString(), false);

    
    ValueData valueData = ((BaseValue) value).getInternalData();
    
    try {
//      String b64s = new String(Base64.encodeBase64(BLOBUtil.readValue(valueData)));
//      return b64s;
      return new String(Base64.encodeBase64(valueData.getAsByteArray()));
    } catch(IOException e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
    }
  }

  /**
   * Return collection of current nemespace prefixes
   */
  protected Collection getNamespacePrefixes() {
    return sessionNamespaces.keySet();
  }

  /**
   * Return session namespace uri by prefix
   * Uri of empty string is empty string
   * If uri not found RepositoryException is throws
   */
  protected String getNamespaceURI(String prefix) throws RepositoryException {
    if (prefix.equals(""))
      return "";
    else {
      String uri = (String) sessionNamespaces.get(prefix);
      if (uri == null)
        throw new RepositoryException("No namespace uri found for prefix '"
            + prefix + "'");
      return uri;
    }

  }

  /**
   * Return qName of property
   * @todo Name escaping
   */
  protected String getItemQName(Item item) throws RepositoryException {
    String result = item.getName();
    if (result.equals("") && item.isNode()) {
      result = "jcr:root";
    }
    return result;
  }

  /**
   * Return Namespace prefix of property
   */
  protected String getItemNsPrefix(Item item) throws RepositoryException {
    String prefix = "";
    String qName = getItemQName(item);
    if (qName.indexOf(':') != -1)
      prefix = qName.substring(0, qName.indexOf(':'));
    return prefix;
  }

  /**
   * Return Namespace prefix of property
   */
  protected String getItemNsURI(Item item) throws RepositoryException {
    return getNamespaceURI(getItemNsPrefix(item));
  }

  /**
   * Return name of property without namespace prefix
   */
  protected String getItemLocalName(Item item) throws RepositoryException {

    String qName = getItemQName(item);
    String localName = qName;

    if (qName.indexOf(':') != -1)
      localName = qName.substring(qName.indexOf(':') + 1, qName.length());
    return localName;
  }

  /**
   * Return Type name of property
   * @see javax.jcr.PropertyType
   */
  protected String getPropertyType(Property property)
      throws RepositoryException {
    return ExtendedPropertyType.nameFromValue(property.getType());
  }

  /**
   *
   * @param node Node
   * @throws RepositoryException
   * @throws SAXException
   * @todo add Locator support
   */
  final public void export(NodeImpl node) throws RepositoryException, SAXException {
    if (node == null) {
      log.error("Specified Node is null");
      throw new java.lang.IllegalArgumentException("node can't be null ");
    }

    if (contentHandler != null) {
      log.debug("start export");
      contentHandler.startDocument();
      readNamespaces();
      startNamespaceScope();
      nodeProcess(node);
      endNamespaceScope();
      contentHandler.endDocument();
    }
  }

  private void propertyProcess(Property property) throws RepositoryException,
      SAXException {
    log.trace("propertyProcess [" + property.getPath() + "]");
    //System.out.println("propProcess prop " +property.getPath());

    startProperty(property);
    Value[] values;

    try {
      values = new Value[] { property.getValue() };
    } catch (ValueFormatException ex) {
      //multi-valued
      values = property.getValues();
    }

    startPropertyValues(property);
    for (int i = 0; i < values.length; i++)
      processValue(values[i]);

    endPropertyValues(property);
    endProperty(property);
  }

  private void processValue(Value value) throws RepositoryException,
      SAXException {
    //process binary value
    if (value.getType() == PropertyType.BINARY) {
      if (getBinaryConduct() == BINARY_SKIP)
        return;
      else if (getBinaryConduct() == BINARY_EMPTY)
        try {
          value = new BinaryValue("");
        } catch (IOException e) {
          throw new RepositoryException(e);
        }
    }
    exportValue(value);
  }

  /**
   *
   * @todo add jcr:primaryType and jcr:mixinTypes is first and second
   */
  private void nodeProcess(NodeImpl node) throws SAXException, RepositoryException {
    log.trace("nodeProcess [" + node.getPath() + "]");

    startNode(node);

    startNodeProperties(node);
    for (PropertyImpl prop: node.childProperties()) {
      propertyProcess(prop);
    }
    endNodeProperties(node);

    if (isRecurse()) {
      for (NodeImpl nextNode: node.childNodes()) 
        nodeProcess(nextNode);
    }
    endNode(node);
  }

  private void readNamespaces() throws RepositoryException {
    log.debug("Read namespace definitions from session");
    ;
    String[] prefixes = session.getNamespacePrefixes();

    for (int i = 0; i < prefixes.length; i++) {
      if (prefixes[i].length() > 0)
        sessionNamespaces
            .put(prefixes[i], session.getNamespaceURI(prefixes[i]));
    }

  }

  private void startNamespaceScope() throws RepositoryException, SAXException {
    log.debug("start namespace scope");
    ;
    Iterator prefixes = getNamespacePrefixes().iterator();
    while (prefixes.hasNext()) {
      String prefix = (String) prefixes.next();
      contentHandler.startPrefixMapping(prefix, getNamespaceURI(prefix));
    }
  }

  private void endNamespaceScope() throws RepositoryException, SAXException {
    log.debug("end namespace scope");
    ;
    Iterator prefixIterator = getNamespacePrefixes().iterator();
    while (prefixIterator.hasNext())
      contentHandler.endPrefixMapping((String) prefixIterator.next());
  }

  // item process events ******************************************************
  protected void startNode(Node node) throws RepositoryException, SAXException {
  }

  protected void endNode(Node node) throws RepositoryException, SAXException {
  }

  protected void startNodeProperties(Node node) throws RepositoryException,
      SAXException {
  }

  protected void endNodeProperties(Node node) throws RepositoryException,
      SAXException {
  }

  protected void startProperty(Property property) throws RepositoryException,
      SAXException {
  }

  protected void endProperty(Property property) throws RepositoryException,
      SAXException {
  }

  protected void startPropertyValues(Property property)
      throws RepositoryException, SAXException {
  }

  protected void endPropertyValues(Property property)
      throws RepositoryException, SAXException {
  }

  protected void exportValue(Value value) throws RepositoryException,
      SAXException {
  }
  //**************************************************************************

}
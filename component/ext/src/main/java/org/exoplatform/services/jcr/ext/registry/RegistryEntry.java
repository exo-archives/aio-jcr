/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.registry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Encapsulates registry entry (i.e services', applications' etc settings) 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public final class RegistryEntry {

  private Document document;
  
  /**
   * creates a RegistryEntry after XML DOM Document
   * root element node name it is the name of the Entry
   * @param dom
   */
  public RegistryEntry(Document dom) {
    this.document = dom;
  }
  
  /**
   * creates an empty RegistryEntry
   * @param rootName
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public RegistryEntry(String rootName) throws IOException,
  SAXException, ParserConfigurationException {
  	DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
  	this.document = db.newDocument();
  	Element nodeElement = document.createElement(rootName);
  	document.appendChild(nodeElement);
  }
  
  /**
   * Factory method to create RegistryEntry from serialized XML 
   * @param bytes
   * @return RegistryEntry
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public static RegistryEntry parse(byte[] bytes) throws IOException,
      SAXException, ParserConfigurationException {
    return new RegistryEntry(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
        new ByteArrayInputStream(bytes)));
  }

  /**
   * Factory method to create RegistryEntry from stream XML
   * @return RegistryEntry
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public static RegistryEntry parse(InputStream in) throws IOException,
      SAXException, ParserConfigurationException {
    return new RegistryEntry(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in));
  }

  /**
   * @return the entry as InputStream
   * @throws IOException
   * @throws TransformerException
   */
  public InputStream getAsInputStream() throws TransformerException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TransformerFactory.newInstance().newTransformer().transform(
        new DOMSource(document), new StreamResult(out));

    return new ByteArrayInputStream(out.toByteArray());
  }
  
  /**
   * @return the name of entry (which is the same as underlying Document's root name) 
   */
  public String getName() {
    return document.getDocumentElement().getNodeName();
  }
  
  /**
   * @return the underlying Document
   */
  public Document getDocument() {
    return document;
  }
}

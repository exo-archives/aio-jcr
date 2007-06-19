/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLEntityTransformer implements EntityTransformer<Document> {

  public Document readFrom(InputStream entityDataStream) throws IOException {
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(entityDataStream);
    } catch (SAXException saxe) {
      throw new IOException("Can't read from input stream");
    } catch (ParserConfigurationException pce) {
      throw new IOException("Can't read from input stream");
    }
  }

  public void writeTo(Document entity, OutputStream entityDataStream) throws IOException {
    try {
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource(entity),
          new StreamResult(entityDataStream));
    } catch (TransformerException tre) {
      throw new IOException("Can't write to output stream");
    }
  }

}

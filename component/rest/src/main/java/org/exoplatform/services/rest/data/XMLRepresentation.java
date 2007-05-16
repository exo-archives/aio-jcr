/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.services.rest.Representation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class XMLRepresentation extends BaseRepresentationMetadata
 implements Representation {
  
  private Document document;
  
  public XMLRepresentation(Document xmlDocument) {
    super("text/xml");
    this.document = xmlDocument;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.rest.Representation#getStream()
   */
  public InputStream getStream() throws IOException {
    return new ByteArrayInputStream(outputStream().toByteArray());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.rest.Representation#getString()
   */
  public String getString() throws IOException {
    
    if (characterSet != null) {
      return outputStream().toString(characterSet);
    } else {
      return outputStream().toString();
    }
  }

  private ByteArrayOutputStream outputStream() throws IOException {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      TransformerFactory.newInstance().newTransformer().transform(
          new DOMSource(document), new StreamResult(os));
      return os;
    } catch (TransformerConfigurationException tce) {
      throw new IOException("Couldn't write the XML representation: "
          + tce.getMessage());
    } catch (TransformerException te) {
      throw new IOException("Couldn't write the XML representation: "
          + te.getMessage());
    } catch (TransformerFactoryConfigurationError tfce) {
      throw new IOException("Couldn't write the XML representation: "
          + tfce.getMessage());
    }
  }
  
}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This type of transformer can read XML from input stream.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLInputTransformer extends InputEntityTransformer {

  @Override
  public final Document readFrom(InputStream entityDataStream) throws IOException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(entityDataStream);
    } catch (SAXException saxe) {
      throw new IOException("Can't read from input stream " + saxe);
    } catch (ParserConfigurationException pce) {
      throw new IOException("Can't read from input stream " + pce);
    }
  }

}

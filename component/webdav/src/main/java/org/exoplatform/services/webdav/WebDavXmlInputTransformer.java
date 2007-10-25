/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.services.rest.transformer.InputEntityTransformer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavXmlInputTransformer extends InputEntityTransformer {
  
  //private static Log log = ExoLogger.getLogger("jcr.WebDavXmlInputTransformer");
  
  @Override
  public Document readFrom(InputStream entityDataStream) throws IOException {
    try {      
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      
      byte []buffer = new byte[2048];
      while (true) {
        int readed = entityDataStream.read(buffer);
        if (readed < 0) {
          break;
        }
        outStream.write(buffer, 0, readed);
      }
      
      byte []bytes = outStream.toByteArray();
      
      //log.info("Request:\r\n" + new String(bytes));
      
      if (bytes.length == 0) {
        return null;
      }
      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    } catch (SAXException saxe) {
      return null;
    } catch (ParserConfigurationException pce) {
      throw new IOException("Can't read from input stream " + pce);
    }
  }

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class XmlResponseWriter implements SerializableEntity {
  
  private static Log log = ExoLogger.getLogger("jcr.XmlResponseWriter");
  
  protected SerializableRepresentation serializable;
  
  public XmlResponseWriter(SerializableRepresentation serializable) {
    this.serializable = serializable;
  }
  
  public void writeObject(OutputStream outputStream) throws IOException {
    try {      
      XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, Constants.DEFAULT_ENCODING);
      
      writer.writeStartDocument(Constants.DEFAULT_ENCODING, "1.0");
      
      WebDavNameSpaceContext nameSpaceContext = new WebDavNameSpaceContext();
      
      writer.setNamespaceContext(nameSpaceContext);
      
      serializable.write(writer);
      
      writer.writeEndDocument();
      
    } catch (Exception exc) {
      log.info("Unhandled ecxeption. " + exc.getMessage(), exc);
    }
    
  }

}

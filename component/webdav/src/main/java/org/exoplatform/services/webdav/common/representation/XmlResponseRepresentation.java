/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.RepositoryException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class XmlResponseRepresentation implements SerializableEntity {
  
  private static Log log = ExoLogger.getLogger("jcr.XmlResponseRepresentation");
  
  private WebDavNameSpaceContext nameSpaceContext;
  
  public XmlResponseRepresentation(WebDavNameSpaceContext nameSpaceContext) {
    this.nameSpaceContext = nameSpaceContext; 
  }
  
  protected abstract void write(XMLStreamWriter writer) throws XMLStreamException, RepositoryException;

  public void writeObject(OutputStream outputStream) throws IOException {
    try {      
      
      XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, Constants.DEFAULT_ENCODING);
      
      writer.writeStartDocument(Constants.DEFAULT_ENCODING, "1.0");
      
      writer.setNamespaceContext(nameSpaceContext);
      
      write(writer);
      
      writer.writeEndDocument();
    } catch (Exception exc) {
      log.info("Unhandled ecxeption. " + exc.getMessage(), exc);
    }
    
  }  
  
}


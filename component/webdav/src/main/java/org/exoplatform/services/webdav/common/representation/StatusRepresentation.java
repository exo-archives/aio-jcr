/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class StatusRepresentation implements SerializableRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.StatusRepresentation");
  
  public static final String XML_STATUS = "status";
  
  private int status;
  
  public StatusRepresentation(int status) {
    log.info("construct...");
    
    this.status = status;
  }

  public void write(XMLStreamWriter xmlStreamWriter) throws XMLStreamException, RepositoryException {
    xmlStreamWriter.writeStartElement("DAV:", XML_STATUS);
    
    //String description = 
    
    xmlStreamWriter.writeEndElement();    
  }

}

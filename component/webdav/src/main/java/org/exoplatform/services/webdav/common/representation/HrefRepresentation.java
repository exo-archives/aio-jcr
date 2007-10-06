/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.util.DavTextUtil;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class HrefRepresentation implements SerializableRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.HrefRepresentation");
  
  protected String href;
  
  public HrefRepresentation(String href) {
    log.info("construct..");
    
    this.href = href;
  }

  public void write(XMLStreamWriter xmlStreamWriter) throws Exception {    
    try {
      xmlStreamWriter.writeStartElement("D", "href", "DAV:");
      
      String escaped = DavTextUtil.Escape(href, '%', true);
      //escaped = escaped.replace(":", "%3a");
      
      log.info("HrefEscaped: " + escaped);
      xmlStreamWriter.writeCharacters(escaped);
      
      xmlStreamWriter.writeEndElement();
      
    } catch (XMLStreamException xexc) {
      log.info("Unhandled exception. " + xexc.getMessage(), xexc);
    }
    
  }

}


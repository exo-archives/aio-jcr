/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.common.util.DavTextUtil;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class HrefRepresentation implements SerializableRepresentation {
  
  protected String href;
  
  public HrefRepresentation(String href) {
    this.href = href;
  }

  public void write(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {    
    xmlStreamWriter.writeStartElement("D", "href", "DAV:");
    
    String escaped = DavTextUtil.Escape(href, '%', true);
    
    xmlStreamWriter.writeCharacters(escaped);
    
    xmlStreamWriter.writeEndElement();
    
  }

}

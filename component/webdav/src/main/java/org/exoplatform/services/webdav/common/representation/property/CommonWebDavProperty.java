/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class CommonWebDavProperty implements PropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.CommonWebDavProperty");
  
  protected int status = WebDavStatus.NOT_FOUND;
  
  public abstract String getTagName();
  
  public abstract String getNameSpace();
  
  protected abstract void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException;
  
  public CommonWebDavProperty() {
    log.info("construct...");
  }
  
  public int getStatus() {
    return status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public void write(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeStartElement(getNameSpace(), getTagName());
    if (status == WebDavStatus.OK) {
      writeContent(xmlWriter);
    }
    xmlWriter.writeEndElement();
  }
  
}

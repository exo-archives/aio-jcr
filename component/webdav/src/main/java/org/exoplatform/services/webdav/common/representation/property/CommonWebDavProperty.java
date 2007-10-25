/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class CommonWebDavProperty implements PropertyRepresentation {
  
  protected int status = WebDavStatus.NOT_FOUND;
  
  public abstract String getTagName();
  
  public abstract String getNameSpace();
  
  protected abstract void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException;
  
  public int getStatus() {
    return status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public void write(XMLStreamWriter xmlWriter) throws XMLStreamException {
    if (status == WebDavStatus.OK) {
      xmlWriter.writeStartElement(getNameSpace(), getTagName());
      writeContent(xmlWriter);
      xmlWriter.writeEndElement();
    } else {
      xmlWriter.writeEmptyElement(getNameSpace(), getTagName());
    }
  }
  
}

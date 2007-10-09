/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SinglePropertyRepresentation implements ResponseRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.SinglePropertyRepresentation");
  
  private PropertyRepresentation propertyRepresentation;
  
  public SinglePropertyRepresentation(PropertyRepresentation propertyRepresentation) {
    this.propertyRepresentation = propertyRepresentation;
  }

  public void init(String prefix, Node node, int maxLevel) throws RepositoryException {
  }

  public void write(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
    xmlStreamWriter.writeStartElement("D", "prop", "DAV:");
    xmlStreamWriter.writeNamespace("D", "DAV:");
    
    propertyRepresentation.write(xmlStreamWriter);
    
    xmlStreamWriter.writeEndElement();
  }

}

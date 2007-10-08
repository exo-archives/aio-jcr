/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
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

public class NotFoundPropertyRepresentation extends CommonWebDavProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.NotExistedPropertyRepresentation");
  
  private String nameSpaceURI;
  
  private String propertyName;
  
  public NotFoundPropertyRepresentation(String nameSpaseURI, String propertyName) {
    log.info("construct...");
    
    this.nameSpaceURI = nameSpaseURI;
    this.propertyName = propertyName;
    
    status = WebDavStatus.NOT_FOUND;
  }

  public void write(XMLStreamWriter xmlWriter) throws XMLStreamException {    
    log.info("NAMESPACEURI: " + nameSpaceURI);
    log.info("PROPERTY NAME: " + propertyName);
    
    xmlWriter.writeStartElement(propertyName);
    xmlWriter.writeEndElement();
  }

  public void read(Node node) throws RepositoryException {
  }

  @Override
  public String getNameSpace() {
    return nameSpaceURI;
  }

  @Override
  public String getTagName() {
    return propertyName;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
  }

}

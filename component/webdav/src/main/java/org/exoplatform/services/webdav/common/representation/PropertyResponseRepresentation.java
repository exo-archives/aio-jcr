/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyResponseRepresentation extends XmlResponseRepresentation {
  
  private PropertyRepresentation propertyRepresentation; 

  public PropertyResponseRepresentation(ManageableRepository repository, PropertyRepresentation propertyRepresentation) throws RepositoryException {
    super(new WebDavNameSpaceContext(repository));    
    this.propertyRepresentation = propertyRepresentation;
  }

  @Override
  protected void write(XMLStreamWriter writer) throws XMLStreamException, RepositoryException {
    writer.writeStartElement("D", "prop", "DAV:");
    writer.writeNamespace("D", "DAV:");
    
    propertyRepresentation.write(writer);
    
    writer.writeEndElement();
  }
  
}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropNamesResponseRepresentation extends AllPropResponseRepresentation {
  
  public PropNamesResponseRepresentation(WebDavService webDavService, String href, Node node, int depth) throws RepositoryException {
    super(webDavService, href, node, depth);
  }
  
  protected void writeResponseContent(XMLStreamWriter xmlStreamWriter, Node node) throws XMLStreamException, RepositoryException {
    ArrayList<PropertyRepresentation> properties = getProperties(node);

    xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.PROPSTAT);
    
    xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.PROP);    
    
    for (int i = 0; i < properties.size(); i++) {
      PropertyRepresentation property = properties.get(i);
      
      property.read(node);
      
      if (property.getStatus() == WebDavStatus.OK) {
        property.setStatus(WebDavStatus.NOT_FOUND);
        property.write(xmlStreamWriter);
      }
    }
    
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeStartElement("DAV:", WebDavProperty.STATUS);
    xmlStreamWriter.writeCharacters(WebDavStatus.getStatusDescription(WebDavStatus.OK));
    xmlStreamWriter.writeEndElement();
    
    xmlStreamWriter.writeEndElement();
  }
  
}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.order.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrderingTypeRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "ordering-type";
  
  @Override
  public String getTagName() {
    return TAGNAME;
  }

  public void read(Node node) {
    try {
      if (node.getPrimaryNodeType().hasOrderableChildNodes()) {
        status = WebDavStatus.OK;
      }
    } catch (RepositoryException exc) {
    }
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    new HrefRepresentation("DAV:custom").write(xmlWriter);
  }

}

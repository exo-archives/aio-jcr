/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ResourceTypeRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "resourcetype";
  
  public static final int RESOURCETYPE_RESOURCE = 0;
  
  public static final int RESOURCETYPE_COLLECTION = 1;
  
  private int resourceType = RESOURCETYPE_COLLECTION;
  
  public void read(Node node) {
    try {
      if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
        resourceType = RESOURCETYPE_RESOURCE;
      }
      status = WebDavStatus.OK;
    } catch (RepositoryException exc) {
    }
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    if (resourceType == RESOURCETYPE_COLLECTION) {
      xmlWriter.writeEmptyElement("DAV:", DavProperty.COLLECTION);
      //xmlWriter.writeStartElement("DAV:", DavProperty.COLLECTION);
      //xmlWriter.writeEndElement();
    }
    
  }

}

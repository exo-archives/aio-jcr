/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedLockRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "supportedlock";
  
  public static final String XML_LOCKENTRY = "lockentry";
  
  public static final String XML_LOCKSCOPE = "lockscope";
  
  public static final String XML_EXCLUSIVE = "exclusive";
  
  public static final String XML_LOCKTYPE = "locktype";
  
  public static final String XML_WRITE = "write";
  
  @Override
  public String getTagName() {
    return TAGNAME;
  }

  public void read(Node node) {
    try {
      if (!node.canAddMixin(DavConst.NodeTypes.MIX_LOCKABLE)) {
        return;
      }
      status = WebDavStatus.OK;      
    } catch (RepositoryException exc) {
    }
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeStartElement(getNameSpace(), XML_LOCKENTRY);    
      
      xmlWriter.writeStartElement(getNameSpace(), XML_LOCKSCOPE);
        xmlWriter.writeEmptyElement(getNameSpace(), XML_EXCLUSIVE);
        //xmlWriter.writeStartElement(getNameSpace(), XML_EXCLUSIVE);
        //xmlWriter.writeEndElement();
      xmlWriter.writeEndElement();
      
      xmlWriter.writeStartElement(getNameSpace(), XML_LOCKTYPE);
        xmlWriter.writeEmptyElement(getNameSpace(), XML_WRITE);
        //xmlWriter.writeStartElement(getNameSpace(), XML_WRITE);
        //xmlWriter.writeEndElement();
      xmlWriter.writeEndElement();
    
    xmlWriter.writeEndElement();
  }  
  
}

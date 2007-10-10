/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class IsRootRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "isroot";
  
  private boolean isRoot = false;
  
  public void read(Node node) {
    try {
      if ("/".equals(node.getPath())) {
        isRoot = true;
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
    xmlWriter.writeCharacters(isRoot ? "1" : "0");
  }

}

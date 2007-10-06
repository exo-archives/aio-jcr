/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavService;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface PropertyRepresentation {
  
  void setStatus(int status);
  
  int getStatus();

  void read(WebDavService webdavService, Node node) throws RepositoryException;
  
  //void set(WebDavService webdavService, Node node) throws RepositoryException;
  
  void write(XMLStreamWriter xmlWriter) throws XMLStreamException;

}

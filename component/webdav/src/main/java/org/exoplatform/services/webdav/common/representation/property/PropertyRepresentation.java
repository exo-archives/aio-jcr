/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public interface PropertyRepresentation {
  
  void setStatus(int status);
  
  int getStatus();
  
  void parseContent(org.w3c.dom.Node node);

  void read(Node node);
  
  void update(Node node);
  
  void remove(Node node);
  
  void write(XMLStreamWriter xmlWriter) throws XMLStreamException;

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface RestTemplate {
  
  static final String EXO_PREFIX = "exo:";
  
  static final String EXO_HREF = "http://www.exoplatform.com";
  
  static final String XMLNS_EXO = "xmlns:exo";
  
  static final String XMLNS_LINK = "xmlns:xlink";
  
  static final String XLINK_HREF = "xlink:href"; 
  
  static final String EXO_XLINK = "http://www.w3.org/1999/xlink";  

  Element serialize(Document xmlDocument);
  
  boolean parse(Node templateNode);
  
  boolean fillContent() throws Exception;
  
}

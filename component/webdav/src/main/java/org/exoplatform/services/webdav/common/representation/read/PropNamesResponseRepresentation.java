/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import javax.jcr.Node;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropNamesResponseRepresentation extends PropFindResponseRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.PropNamesResponseRepresentation");
  
  public PropNamesResponseRepresentation() {
    log.info("construct..");
  }
  
  protected void writeResponseContent(XMLStreamWriter xmlStreamWriter, Node node) {
    
  }

}


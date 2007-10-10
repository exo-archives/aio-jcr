/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.property;

import javax.jcr.Node;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CheckedInRepresentation extends WebDavPropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.CheckedInRepresentation");
  
  public static final String TAGNAME = "checked-in";
  
  public CheckedInRepresentation() {
    log.info("construct...");
  }

  public void read(Node node) {
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
  }

}

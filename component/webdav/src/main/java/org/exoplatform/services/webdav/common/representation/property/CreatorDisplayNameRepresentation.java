/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CreatorDisplayNameRepresentation extends WebDavPropertyRepresentation {
  
  public static Log log = ExoLogger.getLogger("jcr.CreatorDisplayNameRepresentation");
  
  public static final String TAGNAME = "creator-displayname";
  
  public CreatorDisplayNameRepresentation() {
    log.info("construct...");
  }

  public void read(Node node) throws RepositoryException {
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
  }

}

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
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DisplayNameRepresentation extends WebDavPropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.DisplayNameRepresentation");
  
  public static final String TAGNAME = "displayname";
  
  private String displayName;
  
  public DisplayNameRepresentation() {
    log.info("Construct...");
  }

  public String getTagName() {
    return TAGNAME;
  }
  
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters(displayName);
  }

  public void read(WebDavService webdavService, Node node) throws RepositoryException {
    status = WebDavStatus.OK;

    if ("/".equals(node.getPath())) {
      displayName = node.getSession().getWorkspace().getName();
      return;
    }
    
    displayName = node.getName();
  }

}

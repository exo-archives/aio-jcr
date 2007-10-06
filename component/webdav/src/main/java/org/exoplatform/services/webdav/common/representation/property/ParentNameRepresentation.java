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

public class ParentNameRepresentation extends WebDavPropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.ParentNameRepresentation");
  
  public static final String TAGNAME = "parentname";
  
  private String parentName = "";
  
  public ParentNameRepresentation() {
    log.info("construct...");
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters(parentName);
  }

  public void read(WebDavService webdavService, Node node) throws RepositoryException {    
    log.info(">>>>>>>>>>>>>>> NODE NAME: " + node.getName());
    log.info(">>>>>>>>>>>>>>> NODE DEPTH: " + node.getDepth());
    log.info(">>>>>>>>>>>>>>> NODE PATH: " + node.getPath());
    
    if (node.getDepth() == 0) {
      return;
    }
    
    if (node.getDepth() > 1) {
      parentName = node.getParent().getName();
    } else {
      parentName = node.getSession().getWorkspace().getName();
    }

    status = WebDavStatus.OK;
  }

}

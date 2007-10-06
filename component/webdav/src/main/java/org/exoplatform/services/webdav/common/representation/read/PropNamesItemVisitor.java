/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropNamesItemVisitor extends PropFindItemVisitor {
  
  private static Log log = ExoLogger.getLogger("jcr.PropNamesItemVisitor");

  public PropNamesItemVisitor(WebDavService webdavService, boolean breadthFirst, int maxLevel, String rootHref, HashMap<String, ArrayList<String>> properties, XMLStreamWriter xmlWriter) {
    super(webdavService, breadthFirst, maxLevel, rootHref, properties, xmlWriter);    
    log.info("construct...");
  }
  
  protected void fillContent(Node node, XMLStreamWriter xmlWriter) throws RepositoryException {
    
  }
  
}

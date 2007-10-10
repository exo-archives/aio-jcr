/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeRepresentationFactory {
  
  public static final String XML_VERSIONTREE = "version-tree";
  
  private static Log log = ExoLogger.getLogger("jcr.VersionTreeRepresentationFactory");
  
  public static XmlResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href) throws RepositoryException {
    
    return new VersionTreeResponseRepresentation(node);
  }

}


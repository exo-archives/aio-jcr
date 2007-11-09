/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class WebDavPropertyRepresentation extends CommonWebDavProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavPropertyRepresentation");
  
  public static final String NAMESPACE = "DAV:";
  
  @Override
  public String getNameSpace() {
    return NAMESPACE;
  }
  
  public void update(Node node) {
    status = WebDavStatus.FORBIDDEN;
  }
  
  public void remove(Node node) {
    status = WebDavStatus.FORBIDDEN;
  }
  
  public void parseContent(org.w3c.dom.Node node) {
    log.info("parse content...");
  }

}

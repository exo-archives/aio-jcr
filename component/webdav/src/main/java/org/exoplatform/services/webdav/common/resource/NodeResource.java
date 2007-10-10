/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import javax.jcr.Node;

import org.exoplatform.services.webdav.WebDavService;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: NodeResource.java 12222 2007-01-23 09:12:38Z gavrikvetal $
 */

public class NodeResource extends AbstractNodeResource {
  
  public NodeResource(
      WebDavService webDavService,
      String rootHref,
      Node resourceNode) {
    super(webDavService, rootHref, resourceNode);
  }  

}

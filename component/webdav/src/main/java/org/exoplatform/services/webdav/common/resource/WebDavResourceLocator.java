/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface WebDavResourceLocator {
  
  WebDavResource getSrcResource(boolean isFakeEnable) throws RepositoryException;
  
  WebDavResource getDestinationResource(String destinationPath) throws RepositoryException;

}


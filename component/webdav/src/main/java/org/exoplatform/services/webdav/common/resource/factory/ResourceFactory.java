/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource.factory;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.search.resource.SearchableResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public interface ResourceFactory {

  WebDavResource getSrcResource(boolean enableFake) throws RepositoryException;
  
  WebDavResource getDestinationResource() throws RepositoryException;
  
  SearchableResource getSearchableResource() throws RepositoryException;
  
}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.resource;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface SearchableResourceLocator extends WebDavResourceLocator {

  SearchableResource getSearchableResource() throws RepositoryException;
  
}

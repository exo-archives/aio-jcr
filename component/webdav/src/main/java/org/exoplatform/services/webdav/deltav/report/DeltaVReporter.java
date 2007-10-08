/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.report;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.WebDavResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface DeltaVReporter {
  
  DeltaVReport doReport(WebDavResource resource) throws RepositoryException;

}

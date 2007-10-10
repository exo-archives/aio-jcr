/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface JCRResource {

  Session getSession() throws RepositoryException;
  
  Workspace getWorkspace() throws RepositoryException;
  
  String getPath() throws RepositoryException;
  
}

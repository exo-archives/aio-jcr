/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.io.IOException;
import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavResource.java 12234 2007-01-23 12:20:00Z gavrikvetal $
 */

public interface WebDavResource {
  
  String getName() throws RepositoryException;
  
  int getType();
  
  String getHref() throws RepositoryException;
  
  boolean isCollection() throws RepositoryException;
  
  ResourceData getResourceData() throws IOException, RepositoryException;

  ArrayList<WebDavResource> getChildResources() throws RepositoryException;  
  
}

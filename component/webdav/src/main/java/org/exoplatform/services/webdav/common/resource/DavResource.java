/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.response.Response;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavResource.java 12234 2007-01-23 12:20:00Z gavrikvetal $
 */

public interface DavResource {
  
  boolean isCollection() throws RepositoryException;
  
  String getName() throws RepositoryException;
  
  DavResourceInfo getInfo() throws RepositoryException;

  ArrayList<String> getAvailableMethods();
  
  Session getSession() throws RepositoryException;
  
  Response getResponse(CommonPropDocument reqProps) throws RepositoryException;
  
  ArrayList<DavResource> getChildsResources() throws RepositoryException;
  
  int getChildCount() throws RepositoryException;
  
  ArrayList<Response> getChildsResponses(CommonPropDocument reqProps, int depth) throws RepositoryException;  
  
}

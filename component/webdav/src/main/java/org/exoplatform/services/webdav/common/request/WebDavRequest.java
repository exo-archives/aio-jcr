/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.request;

import java.io.InputStream;
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.webdav.WebDavSessionProvider;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavRequest.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public interface WebDavRequest {
  
  PropertyFactory getPropertyFactory();

  Session getSourceSession(WebDavSessionProvider sessionProvider) throws RepositoryException;
  
  Session getDestinationSession(WebDavSessionProvider sessionProvider) throws RepositoryException;
  
  Session getSession(WebDavSessionProvider sessionProvider, String workspaceName) throws RepositoryException;
    
  boolean isSameHosts();
  
  String getServerPrefix();
  
  String getServerApp();
  
  String getSrcWorkspace();
  
  String getSrcPath();
  
  String getSrcVersion();
  
  String getDestWorkspace();
  
  String getDestPath();
  
  ArrayList<String> getLockTokens();
  
  String getNodeType();
  
  ArrayList<String> getMixTypes();
  
  RequestDocument getDocumentFromRequest();
  
  InputStream getRequestStream() throws Exception;
  
  String getContentType();
  
  int getDepth();
  
  long getRangeStart();
  
  long getRangeEnd();
  
}

/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavResourceInfo.java 12787 2007-02-13 12:13:17Z gavrikvetal $
 */

public interface DavResourceInfo {

  void setName(String name);
  
  String getName();
  
  void setLastModified(String lastModified);
  
  String getLastModified();
  
  void setContentType(String contentType);
  
  String getContentType();
  
  void setType(boolean isCollection);
  
  boolean getType();
  
  void setContentStream(InputStream stream);
  
  InputStream getContentStream();

  void setContentLength(long contentLength);
  
  long getContentLength();
  
}

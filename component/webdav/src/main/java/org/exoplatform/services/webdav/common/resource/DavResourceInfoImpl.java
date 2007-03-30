/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavResourceInfoImpl.java 12787 2007-02-13 12:13:17Z gavrikvetal $
 */

public class DavResourceInfoImpl implements DavResourceInfo {
  
  private String name;
  private String lastModified;
  private String contentType;
  private boolean isCollection = true;
  private InputStream stream;
  
  private long contLength = 0;
  
  public DavResourceInfoImpl() {
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setLastModified(String lastModified) {
    this.lastModified = lastModified;
  }
  
  public String getLastModified() {
    return lastModified;
  }
  
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  
  public String getContentType() {
    return contentType;
  }
  
  public void setType(boolean isCollection) {
    this.isCollection = isCollection;
  }
  
  public boolean getType() {
    return isCollection;
  }
  
  public void setContentStream(InputStream stream) {
    this.stream = stream;    
    try {
      if (stream != null) {
        contLength = stream.available(); 
      }      
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
  
  public InputStream getContentStream() {
    return stream;
  }

  public void setContentLength(long contentLength) {
    this.contLength = contentLength;
  }
  
  public long getContentLength() {
    return contLength;
  }

}

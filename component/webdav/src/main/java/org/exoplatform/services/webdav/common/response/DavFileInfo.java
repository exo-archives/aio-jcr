/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.response;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DavFileInfo {

  protected String name;
  protected InputStream fileInputFtream;
  protected String lastModified;
  protected String contentType;
  protected boolean isCollectionRes = true;
  
  public DavFileInfo() {
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setInputStream(InputStream fileInputFtream) {
    this.fileInputFtream = fileInputFtream;
  }
  
  public InputStream getInputStream() {
    return fileInputFtream;
  }
  
  public void setLatstModified(String lastModified) {
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
  
  public int getContentLength() {
    try {
      if (fileInputFtream != null) {
        return fileInputFtream.available();
      }      
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    return 0;
  }
  
  public void setResType(boolean isCollectionRes) {
    this.isCollectionRes = isCollectionRes;
  }
  
  public boolean isCollection() {
    return isCollectionRes;
  }
  
}

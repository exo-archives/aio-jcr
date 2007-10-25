/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource.resourcedata;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class AbstractResourceData implements ResourceData {

  protected String name;
  protected boolean iscollection;
  protected String contentType;
  protected String lastModified;  
  protected InputStream resourceInputStream;
  protected long resourceLenght;  
  
  public String getName() {
    return name;
  }
  
  public boolean isCollection() {
    return iscollection;
  }
  
  public String getContentType() {
    return contentType;
  }
  
  public String getLastModified() {
    return lastModified;
  }
  
  public InputStream getContentStream() {
    return resourceInputStream;
  }
  
  public long getContentLength() {
    return resourceLenght;
  }
  
}


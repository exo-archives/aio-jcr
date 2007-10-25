/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public interface PropApi {
  
  public void setStatus(String httpStatus);
  
  public void setStatus(int status);
  
  public int getStatus();
  
  public String getName();  
  
  public String getValue();
  
  public boolean init(Node node);
  
}

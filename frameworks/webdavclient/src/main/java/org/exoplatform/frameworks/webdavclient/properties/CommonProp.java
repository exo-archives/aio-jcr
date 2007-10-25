/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CommonProp implements PropApi {

  protected String propertyName = "";
  protected String propertyValue = "";
  protected int status = Const.HttpStatus.NOTFOUND;
  
  public CommonProp() {
  }
  
  public CommonProp(String propertyName) {
    this.propertyName = propertyName;
  }
  
  public void setStatus(String httpStatus) {
    String []statusPart = httpStatus.split(" ");
    status = new Integer(statusPart[1]);    
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }

  public String getName() {
    return propertyName;
  }
  
  public String getValue() {
    return propertyValue;
  }
  
  public boolean init(Node node) {
    if (status == Const.HttpStatus.OK) {
      propertyValue = node.getTextContent();
      return true;
    }
    return false;
  }
  
}

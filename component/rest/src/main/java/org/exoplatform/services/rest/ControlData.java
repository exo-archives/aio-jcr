/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ControlData {
  
  private String methodName;
  private String acceptedMediaType;
  private int status;

  public ControlData(String methodName, String acceptedMediaType) {
    super();
    this.methodName = methodName;
    this.acceptedMediaType = acceptedMediaType;
  }

  public String getMethodName() {
    return methodName;
  }
  
  public String getAcceptedMediaType() {
    return acceptedMediaType;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}

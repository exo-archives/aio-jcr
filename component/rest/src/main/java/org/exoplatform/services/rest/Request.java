/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.util.Map;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class Request extends Message {

  private String methodName;
  private ResourceIdentifier resourceIdentifier;
  private Map<String, String> httpHeaderParameters;
  private Map<String, String> httpQueryParameters;
  
  public Request(ResourceIdentifier resourceIdentifier, 
      String methodName, Representation entity) {
    this.methodName = methodName;
    this.resourceIdentifier = resourceIdentifier;
    this.entity = entity;
  }
  
  public Request(ResourceIdentifier resourceIdentifier, 
      String methodName) {
    this.methodName = methodName;
    this.resourceIdentifier = resourceIdentifier;
  }

  public ResourceIdentifier getResourceIdentifier() {
    return resourceIdentifier;
  }

  public void setResourceIdentifier(ResourceIdentifier resourceIdentifier) {
    this.resourceIdentifier = resourceIdentifier;
  }

  public String getMethodName() {
    return methodName;
  }
  
  public void setHttpHeaderParameters(Map<String, String> p) {
    this.httpHeaderParameters = p;
  }

  public void setHttpQueryParameters(Map<String, String> p) {
    this.httpQueryParameters = p;
  }

  public Map<String, String> getHttpHeaderParameters() {
    return this.httpHeaderParameters;
  }

  public Map<String, String> getHttpQueryParameters() {
    return this.httpQueryParameters;
  }

}

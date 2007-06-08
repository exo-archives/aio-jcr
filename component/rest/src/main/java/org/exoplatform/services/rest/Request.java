/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.io.InputStream;
import java.util.Map;
import java.util.Enumeration;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class Request extends Message {

  private String methodName;        // HTTP Method
  private ResourceIdentifier resourceIdentifier;
  private Map<String, Enumeration<String>> httpHeaderParams;
  private Map<String, String[]> httpQueryParams;
  private InputStream entityDataStream;

  /**
   * @param entityDataStream input data stream from http request
   * (http method POST etc)
   * @param resourceIdentifier
   * @param methodName HTTP method (GET, POST, DELETE, etc)
   * @param httpHeaderParams 
   * @param httpQueryParams
   */
  protected Request(InputStream entityDataStream, ResourceIdentifier resourceIdentifier, 
      String methodName, Map<String, Enumeration<String>> httpHeaderParams,
      Map<String, String[]> httpQueryParams) {

    this.methodName = methodName;
    this.resourceIdentifier = resourceIdentifier;
    this.entityDataStream = entityDataStream;
    this.httpQueryParams = httpQueryParams;
    this.httpHeaderParams = httpHeaderParams;
  }
  
  /**
   * 
   * Create a new instance of Request
   * 
   * @param entityDataStream
   * @param resourceIdentifier
   * @param methodName
   * @param headerParams
   * @param queryParams
   * @return Request
   */
  public static Request getInstance(InputStream entityDataStream,
      ResourceIdentifier resourceIdentifier, String methodName,
      Map<String, Enumeration<String>> headerParams,
      Map<String, String[]> queryParams) {
    
    return new Request(entityDataStream, resourceIdentifier,
        methodName, headerParams, queryParams);
  }
  
  public InputStream getEntityDataStream() {
    return this.entityDataStream;
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
  
  public Map<String, Enumeration<String>> getHttpHeaderParams() {
    return this.httpHeaderParams;
  }

  public Map<String, String[]> getHttpQueryParams() {
    return this.httpQueryParams;
  }

}

/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL        .<br/>
 * HTTP Request object
 * @author Gennady Azarenkov
 * @version $Id: $
 */


/**
 * Request represents REST request (not HTTP request)
 */
public class Request {

  private String methodName;        // HTTP Method
  private ResourceIdentifier resourceIdentifier;
  private MultivaluedMetadata headerParams;
  private MultivaluedMetadata queryParams;
  private InputStream entityDataStream;

  /**
   * @param entityDataStream input data stream from http request
   * (http method POST etc)
   * @param resourceIdentifier
   * @param methodName HTTP method (GET, POST, DELETE, etc)
   * @param httpHeaderParams 
   * @param httpQueryParams
   */
  public Request(InputStream entityDataStream, ResourceIdentifier resourceIdentifier, 
      String methodName, MultivaluedMetadata httpHeaderParams,
      MultivaluedMetadata httpQueryParams) {

    this.methodName = methodName;
    this.resourceIdentifier = resourceIdentifier;
    this.entityDataStream = entityDataStream;
    this.queryParams = httpQueryParams;
    this.headerParams = httpHeaderParams;
  }
  
  public InputStream getEntityStream() {
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
  
  public MultivaluedMetadata getHeaderParams() {
    return this.headerParams;
  }

  public MultivaluedMetadata getQueryParams() {
    return this.queryParams;
  }

}

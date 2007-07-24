/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL.<br/>
 * Request represents REST request (not HTTP request).<br/>
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class Request {

  private String methodName;        // HTTP Method
  private ResourceIdentifier resourceIdentifier;
  private MultivaluedMetadata headerParams;
  private MultivaluedMetadata queryParams;
  private InputStream entityDataStream;

  /**
   * @param entityDataStream input data stream from http request
   * (http methods POST, PUT)
   * @param resourceIdentifier @see org.exoplatform.services.rest.ResourceIdentifier
   * @param methodName the HTTP method (GET, POST, DELETE, etc)
   * @param httpHeaderParams the HTTP headers
   * @param httpQueryParams the query parameters
   */
  public Request(InputStream entityDataStream, ResourceIdentifier resourceIdentifier,
      String methodName, MultivaluedMetadata httpHeaderParams, MultivaluedMetadata httpQueryParams) {
    this.methodName = methodName;
    this.resourceIdentifier = resourceIdentifier;
    this.entityDataStream = entityDataStream;
    this.queryParams = httpQueryParams;
    this.headerParams = httpHeaderParams;
  }

  /**
   * Retrun entity body represented by InputStream.
   * @return the entity data stream
   */
  public InputStream getEntityStream() {
    return this.entityDataStream;
  }

  /**
   * get ResourceIdentifier.
   * @see org.exoplatform.services.rest.ResourceIdentifier
   * @return the ResourceIdentifier
   */
  public ResourceIdentifier getResourceIdentifier() {
    return resourceIdentifier;
  }

  /**
   * set ResourceIdentifier.
   * @see org.exoplatform.services.rest.ResourceIdentifier
   * @param resourceIdentifier the ResourceIdentifier
   */
  public void setResourceIdentifier(ResourceIdentifier resourceIdentifier) {
    this.resourceIdentifier = resourceIdentifier;
  }

  /**
   * HTTP method name.
   * @return the HTTP method name
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Return a map of key-values pair of header parameters.
   * @see org.exoplatform.services.rest.MultivaluedMetadata
   * @return the all key-values pair of headers
   */
  public MultivaluedMetadata getHeaderParams() {
    return this.headerParams;
  }

  /**
   * Return a map of key-values pair of query parameters.
   * @see org.exoplatform.services.rest.MultivaluedMetadata
   * @return the all key-values pair of query parameters
   */
  public MultivaluedMetadata getQueryParams() {
    return this.queryParams;
  }

}

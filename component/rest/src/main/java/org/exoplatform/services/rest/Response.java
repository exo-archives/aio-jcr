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

public class Response implements Message {
  
  private Representation representation;
  private ResourceMetadata resourceMetadata;
  private ResourceIdentifier resourceIdentifier;
/*  
  public Response(Request request) {
    this.resourceIdentifier = request.getResourceIdentifier();
  }
*/
  
  public Representation getEntity() {
    return representation;
  }
  

  public void setEntity(Representation entity) {
    this.representation = entity;
  }
  
  public void setResourceMetadata(ResourceMetadata metadata) {
    this.resourceMetadata = metadata;
  } 

  public ResourceMetadata getResourceMetadata() {
    return resourceMetadata;
  } 
  
  public ResourceIdentifier getResourceIdentifier() {
    return resourceIdentifier;
  }

  public void setResourceIdentifier(ResourceIdentifier resourceIdentifier) {
    this.resourceIdentifier = resourceIdentifier;
  } 

}

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

public class Request implements Message {

  private ControlData controlData;
  private ResourceIdentifier resourceIdentifier;
  private Representation entity;
  
  public Request(ResourceIdentifier resourceIdentifier, 
      Representation entity, ControlData controlData) {
    this.controlData = controlData;
    this.resourceIdentifier = resourceIdentifier;
    this.entity = entity;
  }

  public ControlData getControlData() {
    return controlData;
  }

  public ResourceIdentifier getResourceIdentifier() {
    return resourceIdentifier;
  }

  public Representation getEntity() {
    return entity;
  }

}

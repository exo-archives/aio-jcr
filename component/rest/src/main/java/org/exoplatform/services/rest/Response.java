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

public class Response extends Message {
  
  private Representation representation;
  private int status;

  public Response(int status) {
    this.status = status;
  }

  public Response(int status, Representation representation) {
    this.status = status;
  }
  
  public Representation getEntity() {
    return representation;
  }

  public void setEntity(Representation entity) {
    this.representation = entity;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }



}

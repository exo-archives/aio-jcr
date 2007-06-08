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
  
  private Representation<?> representation;
  private int status;

  private Response(int status, Representation<?> representation) {
    this.status = status;
    this.representation = representation;
  }

  public static Response getInstance(int status) {
    return new Response (status, null);
  }

  public static Response getInstance(int status, Representation<?> representation) {
    return new Response(status, representation);
  }

  public Representation<?> getRepresentation() {
    return representation;
  }

  public void setRepresentation(Representation<?> representation) {
    this.representation = representation;
  }
  
  public int getStatus() {
    return status;
  }



}

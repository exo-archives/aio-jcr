/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import org.exoplatform.services.rest.data.MimeTypes;
/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class Message {
  
  protected Representation entity;
  protected String acceptedMediaType = MimeTypes.ALL;

  public Representation getEntity() {
    return this.entity;
  }

  public void setEntity(Representation entity) {
    this.entity = entity;
  }
  
  public String getAcceptedMediaType() {
    return acceptedMediaType;
  }

  public void setAcceptedMediaType(String acceptedMediaType) {
    this.acceptedMediaType = acceptedMediaType;
  }
}

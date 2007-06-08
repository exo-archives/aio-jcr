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
  
  /**
   * acceptedMediaType determine the type of data wich 
   * ResourceContainer can get and serve. This give possibility
   * to set a different ResourceContainer to serve the same
   * uri and HTTP method, but with different content type
   */
  protected String acceptedMediaType = MimeTypes.ALL;

  public String getAcceptedMediaType() {
    return acceptedMediaType;
  }

  public void setAcceptedMediaType(String acceptedMediaType) {
    this.acceptedMediaType = acceptedMediaType;
  }
}

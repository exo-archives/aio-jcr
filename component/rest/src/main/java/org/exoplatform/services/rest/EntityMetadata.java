/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class EntityMetadata {
  
  protected MultivaluedMetadata metadata;

  public EntityMetadata(MultivaluedMetadata headers) {
    this.metadata = headers;
  }

  public String getContentLocation() {
    return metadata.getFirst("Content-Location");
  }

  public String getEncodings() {
    return metadata.getAll().get("Content-Encoding");
  }

  public String getLanguages() {
    return metadata.getAll().get("Content-Language");
  }
  
  public String getLastModified() {
    return metadata.getFirst("Last-Modified");
  }

  public int getLength() {
    if(metadata.getFirst("Content-Length") != null)
      return new Integer(metadata.getFirst("Content-Length"));
    return -1;
  }

  public String getMediaType() {
    return metadata.getFirst("Content-Type");
  }
  

}

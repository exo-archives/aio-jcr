/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class EntityMetadata {
  
  protected MultivaluedMetadata metadata = new MultivaluedMetadata();
  
  public EntityMetadata(String mediaType) {
    setMediaType(mediaType);
  }
  
  public String getCharacterSet() {
    return null; //metadata.getFirst(key);
  }

  public List <String> getEncodings() {
    return metadata.get("Content-Encoding");
  }

  public List <String> getLanguages() {
    return metadata.get("Content-Language");
  }

  public Date getLastModified() {
    return null;
  }

  public long getLength() {
    return new Long(metadata.getFirst("Content-Length"));
  }

  public String getMediaType() {
    return metadata.getFirst("Content-Type");
  }

  public void setEncodings(List <String> encodings) {
    metadata.put("Content-Encoding", encodings);
  }

  public void setLanguages(List <String> languages) {
    metadata.put("Content-Language", languages);
  }

  public void setLastModified(Date lastModified) {
    // TODO Auto-generated method stub
    
  }

  public void setLength(long length) {
    metadata.putSingle("Content-Length", new Long(length).toString());
  }

  public void setMediaType(String mediaType) {
    metadata.putSingle("Content-Type", mediaType);
  }
}

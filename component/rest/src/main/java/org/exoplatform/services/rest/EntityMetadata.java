/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.util.Date;
import java.text.DateFormat;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class EntityMetadata {
  
  protected MultivaluedMetadata metadata;

  public EntityMetadata(MultivaluedMetadata headers) {
    this.metadata = headers;
  }

//  public EntityMetadata() {
//    metadata = new MultivaluedMetadata();
//  }
//  public EntityMetadata(String mediaType) {
//    metadata = new MultivaluedMetadata();
//    setMediaType(mediaType);
//  }
  
  public String getContentLocation() {
    return metadata.getFirst("Content-Location");
  }

  public String getEncodings() {
    List <String> encodings = metadata.get("Content-Encoding");
    if(encodings == null)
      return null;
    if(encodings.size() != 0) {
      StringBuffer sb = new StringBuffer();
      for(String t : encodings)
        sb.append(t + ",");
      return sb.deleteCharAt(sb.length() - 1).toString();
    }
    return null;
  }

  public String getLanguages() {
    List <String> languages = metadata.get("Content-Language");
    if(languages == null)
      return null;
    if(languages.size() != 0) {
      StringBuffer sb = new StringBuffer();
      for(String t : languages)
        sb.append(t + ",");
      return sb.deleteCharAt(sb.length() - 1).toString();
    }
    return null;
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
  
//  public void setEncodings(List <String> encodings) {
//    metadata.put("Content-Encoding", encodings);
//  }

//  public void setLanguages(List <String> languages) {
//    metadata.put("Content-Language", languages);
//  }

//  public void setContentLocation(String contentLocation) {
//    metadata.putSingle("Content-Location", contentLocation);
//  }

//  public void setLength(int length) {
//    metadata.putSingle("Content-Length", new Integer(length).toString());
//  }

//  public void setMediaType(String mediaType) {
//    metadata.putSingle("Content-Type", mediaType);
//  }

//  public void setLastModified(Date lastModified) {
//    metadata.putSingle("Last-Modified",
//        DateFormat.getInstance().format(lastModified));
//  }

}

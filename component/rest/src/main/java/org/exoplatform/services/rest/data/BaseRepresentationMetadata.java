/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.data;

import java.util.Date;

import org.exoplatform.services.rest.RepresentationMetadata;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class BaseRepresentationMetadata implements
    RepresentationMetadata {

  protected String characterSet;
  protected String[] encodings;
  protected String[] languages;
  protected Date lastModified;
  protected long length = -1;

  protected String mediaType;

  public BaseRepresentationMetadata(String mediaType) {
    this.mediaType = mediaType;
  }


  public String getMediaType() {
    return mediaType;
  }
  
  public String[] getEncodings() {
    return encodings;
  }

  public String[] getLanguages() {
    return languages;
  }

  public Date getLastModifiedDate() {
    return lastModified;
  }

  public long getLength() {
    return length;
  }


  public Date getLastModified() {
    return lastModified;
  }


  public String getCharacterSet() {
    return characterSet;
  }


  public void setCharacterSet(String characterSet) {
    this.characterSet = characterSet;
  }


  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }


  public void setEncodings(String[] encodings) {
    this.encodings = encodings;
  }


  public void setLanguages(String[] languages) {
    this.languages = languages;
  }


//  public void setLength(long length) {
//    this.length = length;
//  }


  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }


}

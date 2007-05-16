/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.util.Date;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface RepresentationMetadata {
  
  String getMediaType();
  
  String getCharacterSet();
  
  Date getLastModified();
  
  String[] getEncodings();
  
  String[] getLanguages();
  
  long getLength();
  
  void setLastModified(Date lastModified);

  void setEncodings(String[] encodings);

  void setLanguages(String[] languages);

//  void setLength(long length);

  void setMediaType(String mediaType);

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.util.Date;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public interface Metadata {

  String getMediaType();
  
  String getCharacterSet();
  
  Date getLastModified();
  
  String[] getEncodings();
  
  String[] getLanguages();
  
  long getLength();
  
  void setLastModified(Date lastModified);

  void setLength(long length);

  void setEncodings(String[] encodings);

  void setLanguages(String[] languages);

  void setMediaType(String mediaType);

}

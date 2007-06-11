/**
 * Copyright 2001-2007 The eXo Platform SAS        All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;



/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface Representation<T> {
//  String getLocation();
//  void setLocation(String location);
  T getEntity();
//  InputStream getStream() throws IOException;
//  RepresentationMetadata getMetadata();
}

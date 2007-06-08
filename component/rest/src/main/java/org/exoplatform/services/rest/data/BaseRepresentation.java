/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

import org.exoplatform.services.rest.Metadata;
import org.exoplatform.services.rest.Representation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class BaseRepresentation<T> implements Representation<T> {

  T entity;
  String location;
  Metadata metaData;
  
  public BaseRepresentation(T entity, String mediaType) {
    this.entity = entity;
    this.metaData = new BaseMetadata(mediaType);
  }
  
  public BaseRepresentation(T entity) {
    this(entity, MimeTypes.ALL);
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public T getData() {
    return entity;
  }

  public Metadata getMetadata() {
    return metaData;
  }

}

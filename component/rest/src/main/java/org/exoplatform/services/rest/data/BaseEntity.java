/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

import org.exoplatform.services.rest.Entity;
import org.exoplatform.services.rest.Metadata;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public final class BaseEntity<T> implements Entity<T> {
  
  T entity;
  Metadata metaData;
  
  public BaseEntity(T entity, String mediaType) {
    this.entity = entity;
    this.metaData = new BaseMetadata(mediaType);
  }
  
  public BaseEntity(T entity) {
    this(entity, MimeTypes.ALL);
  }

  public T getData() {
    return entity;
  }
  
  public Metadata getMetadata() {
    return metaData;
  }

}

/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.OutputStream;



/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class Response extends Message {
  
  private Object entity;
  private int status;
  private EntityTransformer transformer;
  private EntityMetadata metadata;

  public Response(int status, 
      EntityMetadata metadata,
      Object entity,
      EntityTransformer transformer) { 
    this.status = status;
    this.entity = entity;
    this.transformer = transformer;
    this.metadata = metadata;
  }
  
  public Response(int status, EntityMetadata metadata) {
    this(status, metadata, null, null);
  }

  public Object getEntity() {
    return entity;
  }

  public void setEntity(Object entity) {
    this.entity = entity;
  }
  
  public int getStatus() {
    return status;
  }
  
  public void writeEntity(OutputStream entityDataStream) throws IOException {
    if(transformer != null)
      transformer.writeTo(entity, entityDataStream);
  }

  public EntityMetadata getMetadata() {
    return metadata;
  }

  
}

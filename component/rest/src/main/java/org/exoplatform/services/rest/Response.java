/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.rest.transformer.EntityTransformerFactory;


/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Response {
  
  private int status;
  private EntityMetadata metadata;
  private Object entity;
  private EntityTransformerFactory transformerFactory;
  private MultivaluedMetadata responseHeaders;

  protected Response(int status, 
      MultivaluedMetadata responseHeaders,
      Object entity,
      EntityTransformerFactory transformerFactory) {
    
    this.status = status;
    this.entity = entity;
    this.transformerFactory = transformerFactory;
    this.responseHeaders = responseHeaders;
    this.metadata = new EntityMetadata(responseHeaders);

  }
  
  public int getStatus() {
    return status;
  }
  
  public MultivaluedMetadata getResponseHeaders() {
    return responseHeaders;
  }
  
  public Object getEntity() {
    return entity;
  }
  
  public EntityMetadata getEntityMetadata() {
    return metadata;
  }

  public boolean isTransformerInitialized() {
    if(transformerFactory != null)
      return true;
    return false;
  }
  
  public boolean isEntityInitialized() {
    if(entity != null)
      return true;
    return false;
  }

  public void setTransformer(EntityTransformerFactory transformerFactory) {
    this.transformerFactory = transformerFactory;
  }
  
  public void writeEntity (OutputStream outputEntityStream) throws IOException {
    if(transformerFactory != null)
      transformerFactory.newTransformer().writeTo(entity, outputEntityStream);
  }

  
  
  public static class Builder {
    
    int status = -1;
    Object entity;
    MultivaluedMetadata responseHeaders = new MultivaluedMetadata();
    EntityTransformerFactory transformerFactory;
    
    protected Builder() {}
    
    protected static synchronized Builder newInstance() {
      return new Builder();
    }
    
    public Response build() {
      return new Response (status, responseHeaders, entity, transformerFactory);
    }
    
    public static Builder withStatus(int st) {
      Builder b = new Builder();
      b.status(st);
      return b;
    }
    
    public static Builder representation(Object e) {
      Builder b = new Builder ();
      b.status(RESTStatus.OK);
      b.entity(e);
      return b;
    }
    
    public static Builder representation(Object e, String type) {
      Builder b = representation(e);
      b.mediaType(type);
      return b;
    }

    public static Builder ok() {
      Builder b = newInstance();
      b.status(RESTStatus.OK);
      return b;
    }

    public static Builder ok(Object e) {
      Builder b = ok();
      b.entity(e);
      return b;
    }
    
    public static Builder ok(Object e, String mediaType) {
      Builder b = ok(e).mediaType(mediaType);
      return b;
    }
    
    public static Builder created(String location) {
      Builder b = newInstance();
      b.status(RESTStatus.CREATED);
      b.responseHeaders.putSingle("Location", location);
      return b;
    }

    public static Builder created(Object e, String location) {
      Builder b = created(location);
      b.entity(e);
      return b;
    }

    public static Builder accepted() {
      Builder b = newInstance();
      b.status(RESTStatus.ACCEPTED);
      return b;
    }

    public static Builder noContent() {
      Builder b = newInstance();
      b.status(RESTStatus.NO_CONTENT);
      return b;
    }

    public static Builder temporaryRedirect(String location) {
      Builder b = newInstance();
      b.status(RESTStatus.TEMP_REDIRECT);
      b.locations(location);
      return b;
    }

    public static Builder notModified() {
      Builder b = newInstance();
      b.status(RESTStatus.NOT_MODIFIED);
      return b;
    }

    public static Builder notModified(String tag) {
      Builder b = notModified();
      b.tag(tag);
      return b;
    }

    public static Builder forbidden() {
      Builder b = newInstance();
      b.status(RESTStatus.FORBIDDEN);
      return b;
    }
    
    public static Builder notFound() {
      Builder b = newInstance();
      b.status(RESTStatus.NOT_FOUND);
      return b;
    }

    public static Builder badRequest() {
      Builder b = newInstance();
      b.status(RESTStatus.BAD_REQUEST);
      return b;
    }

    public static Builder serverError() {
      Builder b = newInstance();
      b.status(RESTStatus.INTERNAL_ERROR);
      return b;
    }
    
    public Builder status(int st) {
      status = st;
      return this;
    }

   public Builder entity(Object e) {
      entity = e;
      return this;
    }
   
   public Builder entity(Object e, String mediaType) {
     entity = e;
     this.responseHeaders.putSingle("Content-Type", mediaType);
     return this;
   }

    public Builder mediaType(String mediaType) {
      this.responseHeaders.putSingle("Content-Type", mediaType);
      return this;
    }
    
    public Builder languages (List<String> languages) {
      this.responseHeaders.put("Content-Language", languages);
      return this;
    }
    
    public Builder encodings (List<String> encodings) {
      this.responseHeaders.put("Content-Encoding", encodings);
      return this;
    }
    
    public Builder locations (String location) {
      this.responseHeaders.putSingle("Location", location);
      return this;
    }
    
    public Builder lastModified (Date lastModified) {
      this.responseHeaders.putSingle("Last-Modified", 
          DateFormat.getInstance().format(lastModified));
      return this;
    }
    
    public Builder tag(String tag) {
      this.responseHeaders.putSingle("ETag", tag);
      return this;
    }
    
    public Builder transformer(EntityTransformerFactory trf) {
      this.transformerFactory = trf;
      return this;
    }
  }
  
}

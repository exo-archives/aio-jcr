/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.rest.transformer.EntityTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Response {
  
  private int status;
  private EntityMetadata metadata;
  private Object entity;
  private EntityTransformer transformer;
  private MultivaluedMetadata responseHeaders;

  protected Response(int status, 
      EntityMetadata metadata,
      Object entity,
      EntityTransformer transformer,
      MultivaluedMetadata responseHeaders) {
    
    this.status = status;
    this.entity = entity;
    this.transformer = transformer;
    this.metadata = metadata;
    this.responseHeaders = responseHeaders;
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
  
  public EntityMetadata getMetadata() {
    return metadata;
  }

  public boolean isTransformer() {
    if(transformer != null)
      return true;
    return false;
  }
  
  public void setTransformer(EntityTransformer transformer) {
    this.transformer = transformer;
  }
  
  public void writeEntity (OutputStream outputEntityStream) throws IOException {
    if(transformer != null)
      transformer.writeTo(entity, outputEntityStream);
  }

  
  
  public static class Builder {
    
    int status = -1;
  	Object entity;
  	EntityMetadata metadata = new EntityMetadata();
  	MultivaluedMetadata responseHeaders = new MultivaluedMetadata();
  	EntityTransformer transformer;
  	
  	protected Builder() {}
  	
  	protected static synchronized Builder newInstance() {
  		return new Builder();
  	}
  	
    public Response build() {
  		return new Response (status, metadata, entity, transformer, responseHeaders);
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
      b.type(type);
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
    
    public static Builder ok(Object e, EntityMetadata md) {
      Builder b = ok(e);
      b.metadata(md);
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

    public static Builder accpeted() {
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

    public Builder metadata(EntityMetadata md) {
      this.metadata = md;
      return this;
    }
    
    public Builder type(String type) {
      this.metadata.setMediaType(type);
      return this;
    }
    
    public Builder languages (List<String> languages) {
      this.metadata.setLanguages(languages);
      return this;
    }
    
    public Builder encodings (List<String> encodings) {
      this.metadata.setEncodings(encodings);
      return this;
    }
    
    public Builder locations (String location) {
      this.responseHeaders.putSingle("Location", location);
      return this;
    }
    
    public Builder lastModified (Date lastModified) {
      this.metadata.setLastModified(lastModified);
      return this;
    }
    
    public Builder tag(String tag) {
      this.responseHeaders.putSingle("ETag", tag);
      return this;
    }
    
    public Builder transformer(EntityTransformer tr) {
      this.transformer = tr;
      return this;
    }
  }
  
}

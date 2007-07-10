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

import org.exoplatform.services.rest.transformer.OutputEntityTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * 
 * REST Response
 */
public class Response {
  
  private int status;
  private EntityMetadata metadata;
  private Object entity;
  private OutputEntityTransformer transformer;
  private MultivaluedMetadata responseHeaders;

  /**
   * @param status - HTTP status 
   * @param responseHeaders - HTTP headers
   * @param entity - representation requested object
   * @param transformer - entity serializator 
   */
  protected Response(int status, 
      MultivaluedMetadata responseHeaders,
      Object entity,
      OutputEntityTransformer transformer) {
    
    this.status = status;
    this.entity = entity;
    this.transformer = transformer;
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
    if(transformer != null)
      return true;
    return false;
  }
  
  public boolean isEntityInitialized() {
    if(entity != null)
      return true;
    return false;
  }

  public void setTransformer(OutputEntityTransformer transformer) {
    this.transformer = transformer;
  }
  
  /**
   * write entity to output stream
   * 
   * @param outputEntityStream
   * @throws IOException
   */
  public void writeEntity (OutputStream outputEntityStream) throws IOException {
  	if(transformer != null) 
  		transformer.writeTo(entity, outputEntityStream);
  }

  
  
  /**
   * REST Response builder
   */
  public static class Builder {
    
    int status = -1;
    Object entity;
    MultivaluedMetadata responseHeaders = new MultivaluedMetadata();
    OutputEntityTransformer transformer;
    
    protected Builder() {}
    
    protected static synchronized Builder newInstance() {
      return new Builder();
    }
    
    /**
     * create REST Response with seted parameters
     * @return REST Response
     */
    public Response build() {
      return new Response (status, responseHeaders, entity, transformer);
    }
    
    /**
     * create Builder with selected status
     * @param st - HTTP status
     * @return Builder object whith status: st
     */
    public static Builder withStatus(int st) {
      Builder b = new Builder();
      b.status(st);
      return b;
    }
    
    /**
     * create Builder with HTTP status 200 (OK) and entity
     * @param e - entity
     * @return Builder
     */
    public static Builder representation(Object e) {
      Builder b = new Builder ();
      b.status(RESTStatus.OK);
      b.entity(e);
      return b;
    }
    
    /**
     * create Builder with HTTP status 200 (OK),entity and set entity type
     * @param e - entity
     * @param type - entity type
     * @return Builder
     */
    public static Builder representation(Object e, String type) {
      Builder b = representation(e);
      b.mediaType(type);
      return b;
    }

    /**
     * create Builder with HTTP status 200 (OK)
     * @return Builder
     */
    public static Builder ok() {
      Builder b = newInstance();
      b.status(RESTStatus.OK);
      return b;
    }

    /**
     * create Builder with HTTP status 200 (OK) and entity
     * @param e - entity
     * @return Builder
     */
    public static Builder ok(Object e) {
      Builder b = ok();
      b.entity(e);
      return b;
    }
    
    /**
     * create Builder with HTTP status 200 (OK),entity and set entity type
     * @param e - entity
     * @param type - entity type
     * @return Builder
     */
    public static Builder ok(Object e, String mediaType) {
      Builder b = ok(e).mediaType(mediaType);
      return b;
    }
    
    /**
     * create Builder with status 201 (CREATED)
     * @param location - location created resources
     * @return Builder
     */
    public static Builder created(String location) {
      Builder b = newInstance();
      b.status(RESTStatus.CREATED);
      b.responseHeaders.putSingle("Location", location);
      return b;
    }

    /**
     * create Builder with status 201 (CREATED) and entity
     * @param e - entity
     * @param location - location created resources
     * @return Builder
     */
    public static Builder created(Object e, String location) {
      Builder b = created(location);
      b.entity(e);
      return b;
    }

    /**
     * create Builder with HTTP status 202 (ACCEPTED)
     * @return Builder
     */
    public static Builder accepted() {
      Builder b = newInstance();
      b.status(RESTStatus.ACCEPTED);
      return b;
    }

    /**
     * create Builder with HTTP status 204 (NO CONTENT)
     * @return Builder
     */
    public static Builder noContent() {
      Builder b = newInstance();
      b.status(RESTStatus.NO_CONTENT);
      return b;
    }

    /**
     * created Builder with HTTP status 307 (TEMPORARY REDIRECT)
     * @param location - new resource location
     * @return Builder
     */
    public static Builder temporaryRedirect(String location) {
      Builder b = newInstance();
      b.status(RESTStatus.TEMP_REDIRECT);
      b.locations(location);
      return b;
    }

    /**
     * create Builder with HTTP status 304 (NOT MODIFIED)
     * @return Builder
     */
    public static Builder notModified() {
      Builder b = newInstance();
      b.status(RESTStatus.NOT_MODIFIED);
      return b;
    }

    /**
     * create Builder with HTTP status 304 (NOT MODIFIED) and HTTP EntityTag
     * @param tag - HTTP EntityTag
     * @return Builder
     */
    public static Builder notModified(String tag) {
      Builder b = notModified();
      b.tag(tag);
      return b;
    }

    /**
     * create Builder with HTTP status 403 (FORBIDDEN)
     * @return Builder
     */
    public static Builder forbidden() {
      Builder b = newInstance();
      b.status(RESTStatus.FORBIDDEN);
      return b;
    }
    
    /**
     * create Builder with HTTP status 404 (NOT FOUND)
     * @return Builder
     */
    public static Builder notFound() {
      Builder b = newInstance();
      b.status(RESTStatus.NOT_FOUND);
      return b;
    }

    /**
     * create Builder with HTTP status 400 (BAD REQUEST)
     * @return Builder
     */
    public static Builder badRequest() {
      Builder b = newInstance();
      b.status(RESTStatus.BAD_REQUEST);
      return b;
    }

    /**
     * create Builder with HTTP status 500 (INTERNAL SERVER ERROR)
     * @return Builder
     */
    public static Builder serverError() {
      Builder b = newInstance();
      b.status(RESTStatus.INTERNAL_ERROR);
      return b;
    }
    
    /**
     * return Buider with changed status
     * @param st
     * @return Builder
     */
    public Builder status(int st) {
      status = st;
      return this;
    }

   /**
   * add entity to the Builder
   * @param e
   * @return Builder
   */
  public Builder entity(Object e) {
      entity = e;
      return this;
    }
   
   /**
   * add entity to the Buider and set entity Content-Type
   * @param e
   * @param mediaType
   * @return Builder
   */
  public Builder entity(Object e, String mediaType) {
     entity = e;
     this.responseHeaders.putSingle("Content-Type", mediaType);
     return this;
   }

    /**
     * set entity Content-Type
     * @param mediaType
     * @return Builder
     */
    public Builder mediaType(String mediaType) {
      this.responseHeaders.putSingle("Content-Type", mediaType);
      return this;
    }
    
    /**
     * set Content-Language
     * @param languages
     * @return Builder
     */
    public Builder languages (List<String> languages) {
      this.responseHeaders.put("Content-Language", languages);
      return this;
    }
    
    /**
     * set Content-Encoding
     * @param encodings
     * @return Builder
     */
    public Builder encodings (List<String> encodings) {
      this.responseHeaders.put("Content-Encoding", encodings);
      return this;
    }
    
    /**
     * set Location
     * @param location
     * @return Builder
     */
    public Builder locations (String location) {
      this.responseHeaders.putSingle("Location", location);
      return this;
    }
    
    /**
     * set Last-Modified
     * @param lastModified
     * @return Builder
     */
    public Builder lastModified (Date lastModified) {
      this.responseHeaders.putSingle("Last-Modified", 
          DateFormat.getInstance().format(lastModified));
      return this;
    }
    
    /**
     * set HTTP EntityTag
     * @param tag
     * @return
     */
    public Builder tag(String tag) {
      this.responseHeaders.putSingle("ETag", tag);
      return this;
    }
    
    /**
     * set OutputEntityTransformer
     * @param trf
     * @return Buider
     */
    public Builder transformer(OutputEntityTransformer trf) {
      this.transformer = trf;
      return this;
    }
    
    /**
     * add response header
     * @param key
     * @param value
     * @return Builder
     */
    public Builder header(String key, String value) {
      this.responseHeaders.putSingle(key, value);
      return this;
    }
    
    /**
     * add response headers
     * @param headers
     * @return Builder
     */
    public Builder headers(MultivaluedMetadata headers) {
      this.responseHeaders = headers;
      return this;
    }
  }
  
}

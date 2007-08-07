/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;

/**
 * REST Response.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Response {
  private int status;
  private EntityMetadata metadata;
  private Object entity;
  private OutputEntityTransformer transformer;
  private MultivaluedMetadata responseHeaders;

  /**
   * @param status the HTTP status
   * @param responseHeaders the HTTP headers
   * @param entity the representation requested object
   * @param transformer the entity serializator
   */
  protected Response(int status, MultivaluedMetadata responseHeaders, Object entity,
      OutputEntityTransformer transformer) {
    this.status = status;
    this.entity = entity;
    this.transformer = transformer;
    this.responseHeaders = responseHeaders;
    this.metadata = new EntityMetadata(responseHeaders);

  }

  /**
   * HTTP status of response.
   * @return the HTTP status
   */
  public int getStatus() {
    return status;
  }

  /**
   * HTTP headers.
   * @return the HTTP headers for response
   */
  public MultivaluedMetadata getResponseHeaders() {
    return responseHeaders;
  }

  /**
   * Body of HTTP response. Entity is a representation of requeted resource.
   * @return the entity
   */
  public Object getEntity() {
    return entity;
  }

  /**
   * Content-Length header.
   * @return content length in bytes
   */
  private long length = 0;
  public long countContentLength() {
    // check is transformer available and is not
    // entity represented by InputStream. Can't read
    // from Stream!!! (after reading Stream is empty)
    if (transformer != null && !InputStream.class.isInstance(entity)) {
      final PipedOutputStream pou = new PipedOutputStream();
      final PipedInputStream pin = new PipedInputStream();

      try {
        pin.connect(pou);
        new Thread() {
          public void run() {
            try {
              transformer.writeTo(entity, pou);
            } catch (IOException e) {
              length = 0;
            } finally {
              try {
                pou.flush();
                pou.close();
              } catch (IOException ioe) {
                ;
              }
            }
          }
        }.start();

        int rd = -1;
        byte[] buff = new byte[1024];
        while ((rd = pin.read(buff)) != -1) {
          length += rd;
        }
      } catch (IOException ioe) {
        length = 0;
      } finally {
        try {
          pin.close();
        } catch (IOException ioe) {
          length = 0;
        }
      }
    }
    return length;
  }

  /**
   * EntityMetadata gives possibility to view some response headers by use method get.<br/>
   * For example:<br/><pre>metadata.getMediaType()</pre> - Content-Type header.
   * @return @see org.exoplatform.services.rest.EntityMetadata
   */
  public EntityMetadata getEntityMetadata() {
    return metadata;
  }

  /**
   * Is transformer initialized.
   * @return the state of OutputEntityTransformer
   */
  public boolean isTransformerInitialized() {
    if (transformer != null) {
      return true;
    }
    return false;
  }

  /**
   * Is entity initialized. For HTTP it means has request entity body or not.
   * @return the entity state
   */
  public boolean isEntityInitialized() {
    if (entity != null) {
      return true;
    }
    return false;
  }

  /**
   * Set entity transformer.
   * @param transformer the transformer which can serialize entity.
   */
  public void setTransformer(OutputEntityTransformer transformer) {
    this.transformer = transformer;
  }

  /**
   * Write entity to output stream.
   * @param outputEntityStream the output stream for writing entity
   * @throws IOException Input/Output Exception
   */
  public void writeEntity(OutputStream outputEntityStream) throws IOException {
    if (transformer != null) {
      transformer.writeTo(entity, outputEntityStream);
    }
  }

  /**
   * REST Response builder.
   */
  public static class Builder {

    int status = -1;
    Object entity;
    MultivaluedMetadata responseHeaders = new MultivaluedMetadata();
    OutputEntityTransformer transformer;

    protected Builder() {
    }

    /**
     * Create a new instance of builder.
     * @return the new instance of Builder
     */
    protected static synchronized Builder newInstance() {
      return new Builder();
    }

    /**
     * create REST Response with parameters collected by Builder.
     * @return the REST Response
     */
    public Response build() {
      return new Response(status, responseHeaders, entity, transformer);
    }

    /**
     * create Builder with selected status.
     * @param st the HTTP status
     * @return the new instance of Builder whith status: st
     */
    public static Builder withStatus(int st) {
      Builder b = newInstance();
      b.status(st);
      return b;
    }

    /**
     * create Builder with HTTP status 200 (OK) and entity.
     * @param e the entity
     * @return the new Builder instance with status 200 (OK) and entity
     */
    public static Builder representation(Object e) {
      Builder b = newInstance();
      b.status(HTTPStatus.OK);
      b.entity(e);
      return b;
    }

    /**
     * create Builder with HTTP status 200 (OK), entity and set entity type.
     * @param e the entity
     * @param type the entity type
     * @return the new instance of Builder with status 200 (OK), entity and
     *         media type
     */
    public static Builder representation(Object e, String type) {
      Builder b = representation(e);
      b.mediaType(type);
      return b;
    }

    /**
     * create Builder with HTTP status 200 (OK).
     * @return new instance of Builder with status 200 (OK)
     */
    public static Builder ok() {
      Builder b = newInstance();
      b.status(HTTPStatus.OK);
      return b;
    }

    /**
     * create Builder with HTTP status 200 (OK) and entity.
     * @param e the entity
     * @return the new Builder instance with status 200 (OK) and entity
     */
    public static Builder ok(Object e) {
      Builder b = ok();
      b.entity(e);
      return b;
    }

    /**
     * create Builder with HTTP status 200 (OK),entity and set entity type.
     * @param e the entity
     * @param mediaType the entity type
     * @return the new instance of Builder with status 200 (OK), entity and media type
     */
    public static Builder ok(Object e, String mediaType) {
      Builder b = ok(e).mediaType(mediaType);
      return b;
    }

    /**
     * create Builder with status 201 (CREATED).
     * @param location the location created resources
     * @return the new instance of Builder with status 201 (CREATED) and header Location
     */
    public static Builder created(String location) {
      Builder b = newInstance();
      b.status(HTTPStatus.CREATED);
      b.responseHeaders.putSingle("Location", location);
      return b;
    }

    /**
     * create Builder with status 201 (CREATED) and entity.
     * @param e the entity
     * @param location the location created resources
     * @return the new instance of Builder with status 201 (CREATED),
     * header Location and Location in entity body
     */
    public static Builder created(Object e, String location) {
      Builder b = created(location);
      b.entity(e);
      return b;
    }

    /**
     * create Builder with HTTP status 202 (ACCEPTED).
     * @return the new instance of Builder with status 202 (ACCEPTED)
     */
    public static Builder accepted() {
      Builder b = newInstance();
      b.status(HTTPStatus.ACCEPTED);
      return b;
    }

    /**
     * create Builder with HTTP status 204 (NO CONTENT).
     * @return the new instance of Builder with status 204 (NO CONTENT)
     */
    public static Builder noContent() {
      Builder b = newInstance();
      b.status(HTTPStatus.NO_CONTENT);
      return b;
    }

    /**
     * created Builder with HTTP status 307 (TEMPORARY REDIRECT).
     * @param location the new resource location
     * @return the new instance of Builder with status 307 (TEMPORARY REDIRECT)
     */
    public static Builder temporaryRedirect(String location) {
      Builder b = newInstance();
      b.status(HTTPStatus.TEMP_REDIRECT);
      b.locations(location);
      return b;
    }

    /**
     * create Builder with HTTP status 304 (NOT MODIFIED).
     * @return the new instance of Builder with status 304 (NOT MODIFIED)
     */
    public static Builder notModified() {
      Builder b = newInstance();
      b.status(HTTPStatus.NOT_MODIFIED);
      return b;
    }

    /**
     * create Builder with HTTP status 304 (NOT MODIFIED) and HTTP EntityTag.
     * @param tag the HTTP EntityTag
     * @return the new instance of Builder with status 304 (NOT MODIFIED) and HTTP EntityTag
     */
    public static Builder notModified(String tag) {
      Builder b = notModified();
      b.tag(tag);
      return b;
    }

    /**
     * create Builder with HTTP status 403 (FORBIDDEN).
     * @return the new instance of Builder with status 403 (FORBIDDEN)
     */
    public static Builder forbidden() {
      Builder b = newInstance();
      b.status(HTTPStatus.FORBIDDEN);
      return b;
    }

    /**
     * create Builder with HTTP status 404 (NOT FOUND).
     * @return the new instance of Builder with status 404 (NOT FOUND)
     */
    public static Builder notFound() {
      Builder b = newInstance();
      b.status(HTTPStatus.NOT_FOUND);
      return b;
    }

    /**
     * create Builder with HTTP status 400 (BAD REQUEST).
     * @return the new instance of Builder with status 400 (BAD REQUEST)
     */
    public static Builder badRequest() {
      Builder b = newInstance();
      b.status(HTTPStatus.BAD_REQUEST);
      return b;
    }

    /**
     * create Builder with HTTP status 500 (INTERNAL SERVER ERROR).
     * @return the new instance of Builder with status 500 (INTERNAL SERVER ERROR)
     */
    public static Builder serverError() {
      Builder b = newInstance();
      b.status(HTTPStatus.INTERNAL_ERROR);
      return b;
    }

    /**
     * return Buider with changed status.
     * @param st the HTTP status
     * @return the Builder with changed status
     */
    public Builder status(int st) {
      status = st;
      return this;
    }

    /**
     * add entity to the Builder.
     * @param e the entity (object with represents requested resource)
     * @return the Builder with added entity
     */
    public Builder entity(Object e) {
      entity = e;
      return this;
    }

    /**
     * add entity to the Buider and set entity Content-Type.
     * @param e the entity (object with represents requested resource)
     * @param mediaType the entity media type
     * @return the Builder with added entity and media type
     */
    public Builder entity(Object e, String mediaType) {
      entity = e;
      this.responseHeaders.putSingle("Content-Type", mediaType);
      return this;
    }

    /**
     * set entity Content-Type.
     * @param mediaType the entity media type
     * @return the Builder with added media type
     */
    public Builder mediaType(String mediaType) {
      this.responseHeaders.putSingle("Content-Type", mediaType);
      return this;
    }

    /**
     * set Content-Language.
     * @param languages the List &lt; String &gt; of language
     * @return the Builder with added header "Content-Language"
     */
    public Builder languages(List < String > languages) {
      this.responseHeaders.put("Content-Language", languages);
      return this;
    }

    /**
     * set Content-Encoding.
     * @param encodings the List &lt; String &gt; of content encoding
     * @return the Builder with added header "Content-Encoding"
     */
    public Builder encodings(List < String > encodings) {
      this.responseHeaders.put("Content-Encoding", encodings);
      return this;
    }

    /**
     * set Location header.
     * @param location the Location string
     * @return the Builder with added "Location" header
     */
    public Builder locations(String location) {
      this.responseHeaders.putSingle("Location", location);
      return this;
    }

    /**
     * set Last-Modified header.
     * @param lastModified the date of last change in resource
     * @return the Builder with added "Last-Modified" header
     */
    public Builder lastModified(Date lastModified) {
      this.responseHeaders
          .putSingle("Last-Modified", DateFormat.getInstance().format(lastModified));
      return this;
    }

    /**
     * set Content-Length header.
     * @param Content-Length size of data in bytes
     * @return the Builder with added "Content-Length" header
     */
    public Builder contentLenght(long length) {
      this.responseHeaders.putSingle("Content-Length", length + "");
      return this;
    }

    /**
     * set HTTP EntityTag header.
     * @param tag the HTTP entity tag
     * @return the Builder with added "ETag" header
     */
    public Builder tag(String tag) {
      this.responseHeaders.putSingle("ETag", tag);
      return this;
    }

    /**
     * set OutputEntityTransformer.
     * @param trf the output transformer
     * @see org.exoplatform.services.rest.transformer.OutputEntityTransformer
     * @return the Buider with added entity transformer
     */
    public Builder transformer(OutputEntityTransformer trf) {
      this.transformer = trf;
      return this;
    }

    /**
     * add response single header.
     * @param key the key
     * @param value the value
     * @return the Builder with added single header
     */
    public Builder header(String key, String value) {
      this.responseHeaders.putSingle(key, value);
      return this;
    }

    /**
     * set response headers.
     * @param headers key-values pair of HTTP reponse header
     * @return the Builder with new sets of HTTP headers
     */
    public Builder headers(MultivaluedMetadata headers) {
      this.responseHeaders = headers;
      return this;
    }
    
    /**
     * @param c @see org.exoplatform.services.rest.CacheControl
     * @return the Builder with added Cache-Control header
     */
    public Builder cacheControl(CacheControl c) {
      this.responseHeaders.putSingle("Cache-Control", c.getAsString());
      return this;
    }
  }

}

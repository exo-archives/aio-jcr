/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class EntityMetadata {

  protected MultivaluedMetadata metadata;

  /**
   * Constructor for EntityMetadata.
   * @param headers MultivaluedMetadata object with HTTP headers
   */
  public EntityMetadata(MultivaluedMetadata headers) {
    this.metadata = headers;
  }

  /**
   * The Content-Location entity-header field MAY be used to supply the resource
   * location for the entity enclosed in the message when that entity is
   * accessible from a location separate from the requested resource's URI.
   * @return Content-Location header
   */
  public String getContentLocation() {
    return metadata.getFirst("Content-Location");
  }

  /**
   * The Content-Encoding entity-header field is used as a modifier to the
   * media-type. When present, its value indicates what additional content
   * codings have been applied to the entity-body, and thus what decoding
   * mechanisms must be applied in order to obtain the media-type referenced by
   * the Content-Type header field. Content-Encoding is primarily used to allow
   * a document to be compressed without losing the identity of its underlying
   * media type.
   * @return Content-Encoding header
   */
  public String getEncodings() {
    return metadata.get("Content-Encoding");
  }

  /**
   * The Content-Language entity-header field describes the natural language(s)
   * of the intended audience for the enclosed entity. Note that this might not
   * be equivalent to all the languages used within the entity-body.
   * @return Content-Language header
   */
  public String getLanguages() {
    return metadata.get("Content-Language");
  }

  /**
   * The Last-Modified entity-header field indicates the date and time at which
   * the origin server believes the variant was last modified.
   * @return Last-Modified header
   */
  public String getLastModified() {
    return metadata.getFirst("Last-Modified");
  }

  /**
   * The Content-Length entity-header field indicates the size of the
   * entity-body, in decimal number of OCTETs, sent to the recipient or, in the
   * case of the HEAD method, the size of the entity-body that would have been
   * sent had the request been a GET.
   * @return Content-Length leader
   */
  public int getLength() {
    if (metadata.getFirst("Content-Length") != null) {
      return new Integer(metadata.getFirst("Content-Length"));
    }
    return 0;
  }

  /**
   * The Content-Type entity-header field indicates the media type of the
   * entity-body sent to the recipient or, in the case of the HEAD method, the
   * media type that would have been sent had the request been a GET.
   * @return Content-Type header
   */
  public String getMediaType() {
    return metadata.getFirst("Content-Type");
  }
  
  public String getCacheControl() {
    return metadata.getFirst("Cache-Control");
  }
}

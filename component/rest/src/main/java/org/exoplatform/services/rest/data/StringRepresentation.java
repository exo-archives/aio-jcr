/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.data;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class StringRepresentation extends BaseRepresentation<String> {

  public StringRepresentation(String content) {
    this(content, MimeTypes.ALL);
  }

  public StringRepresentation(String content, String mediaType) {
    super(content, mediaType);
    this.setLength(this.entity.length());
  }

//  public InputStream getStream() {
//    return new ByteArrayInputStream(entity.getBytes());
//  }

  public String getEntity() {
    return entity;
  }
}

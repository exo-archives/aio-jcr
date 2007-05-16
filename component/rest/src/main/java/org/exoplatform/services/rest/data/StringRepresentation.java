/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.exoplatform.services.rest.Representation;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class StringRepresentation extends BaseRepresentationMetadata implements Representation {

  private String content;

  public StringRepresentation(String content) {
    this(content, "text/plain");
  }

  public StringRepresentation(String content, String mediaType) {
    super(mediaType);
    this.content = content;
    this.length = this.content.length();
  }

  public InputStream getStream() {
    return new ByteArrayInputStream(content.getBytes());
  }

  public String getString() {
    return content;
  }

}

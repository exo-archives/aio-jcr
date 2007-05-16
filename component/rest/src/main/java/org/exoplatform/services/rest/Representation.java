/**
 * Copyright 2001-2007 The eXo Platform SAS        All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface Representation extends RepresentationMetadata {
  
  InputStream getStream() throws IOException;
//  void write(OutputStream stream);
  String getString() throws IOException;

}

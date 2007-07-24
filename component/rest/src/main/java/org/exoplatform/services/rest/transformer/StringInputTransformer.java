/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;

/**
 * This type of transformer can read java.lang.String objects from input stream.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class StringInputTransformer extends InputEntityTransformer {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.InputEntityTransformer#readFrom(
   *      java.io.InputStream)
   */
  @Override
  public final String readFrom(InputStream entityDataStream) throws IOException {
    StringBuffer sb = new StringBuffer();
    int rd = -1;
    while ((rd = entityDataStream.read()) != -1) {
      sb.append((char) rd);
    }
    return sb.toString();
  }

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This type of transformer can write java.lang.String objects in output stream.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class StringOutputTransformer extends OutputEntityTransformer {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.OutputEntityTransformer#writeTo(
   *      java.lang.Object, java.io.OutputStream)
   */
  @Override
  public final void writeTo(Object entity, OutputStream entityDataStream) throws IOException {
    String e = (String) entity;
    entityDataStream.write(e.getBytes());
  }

}

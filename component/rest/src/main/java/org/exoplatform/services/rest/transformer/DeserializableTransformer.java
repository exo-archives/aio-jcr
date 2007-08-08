/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.InputStream;
import java.io.IOException;


/**
 * This type of transformers can work whith objects which implement interface
 * DeserializableEntity. Transformer use own method of Object for reading object
 * from input stream.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class DeserializableTransformer extends InputEntityTransformer {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.InputEntityTransformer#readFrom(java.io.InputStream)
   */
  public final DeserializableEntity readFrom(InputStream entityDataStream) throws IOException {
    try {
      DeserializableEntity e = (DeserializableEntity) entityType.newInstance();
      e.readObject(entityDataStream);
      return e;
    } catch (IllegalAccessException iae) {
      throw new IOException("Can't read from input stream. Exception: " + iae);
    } catch (InstantiationException ie) {
      throw new IOException("Can't read from input stream. Exception: " + ie);
    }
  }
}

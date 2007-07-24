/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.InputStream;
import java.io.IOException;

import org.exoplatform.services.rest.SerializableEntity;

/**
 * This type of transformers can work which objects which implement interface
 * SerializableEntity. Transformer use own method of Object for reading object
 * from input stream.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SerializableInputTransformer extends InputEntityTransformer {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.InputEntityTransformer#readFrom(java.io.InputStream)
   */
  public final SerializableEntity readFrom(InputStream entityDataStream) throws IOException {
    try {
      SerializableEntity se = (SerializableEntity) entityType.newInstance();
      se.readObject(entityDataStream);
      return se;
    } catch (IllegalAccessException iae) {
      throw new IOException("Can't read from input stream. Exception: " + iae);
    } catch (InstantiationException ie) {
      throw new IOException("Can't read from input stream. Exception: " + ie);
    }
  }
}

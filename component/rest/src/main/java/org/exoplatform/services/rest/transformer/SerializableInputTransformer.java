/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.InputStream;
import java.io.IOException;

import org.exoplatform.services.rest.SerializableEntity;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SerializableInputTransformer extends InputEntityTransformer {

  public SerializableEntity readFrom(InputStream entityDataStream) throws IOException {
    try {
      SerializableEntity se = (SerializableEntity)entityType.newInstance();
      se.readObject(entityDataStream);
      return se;
    } catch(IllegalAccessException iae) {
      throw new IOException("Can't read from input stream. Exception: " + iae);
    } catch(InstantiationException ie) {
      throw new IOException("Can't read from input stream. Exception: " + ie);
    }
  }
}

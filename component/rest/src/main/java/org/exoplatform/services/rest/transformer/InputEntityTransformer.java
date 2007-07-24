/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Basic implementations of GenericInputEntityTransformer.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class InputEntityTransformer implements GenericInputEntityTransformer {

  /**
   * type of Objects which InputEntityTransformer can serve.
   */
  protected Class < ? > entityType;

  /**
   * Set the type of Objects which should be serve by InputEntityTransformer.
   * @param entityType the type of entity
   */
  public final void setType(Class < ? > entityType) {
    this.entityType = entityType;
  }

  /**
   * Get the type of served Objects.
   * @return type of served object
   */
  public final Class < ? > getType() {
    return entityType;
  }

  /**
   * Build Objects from given InputStream.
   * @param entityDataStream from this InputStream Object should be readed
   * @return Object builded Object
   * @throws IOException Input/Output Exception
   */
  abstract public Object readFrom(InputStream entityDataStream) throws IOException;

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.OutputStream;
import java.io.IOException;

import org.exoplatform.services.rest.SerializableEntity;

/**
 * This type of transformers can work with objects which implement
 * interface SerializableEntity. Transformer use own method of
 * Object for writing object in output stream.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SerializableOutputTransformer extends OutputEntityTransformer {
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.OutputEntityTransformer#writeTo(java.lang.Object, java.io.OutputStream)
   */
  public void writeTo(Object entity, OutputStream entityDataStream) throws IOException{
    SerializableEntity se = (SerializableEntity)entity;
    se.writeObject(entityDataStream);
  }
  
}

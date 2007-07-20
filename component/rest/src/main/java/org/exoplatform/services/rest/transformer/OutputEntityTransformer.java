/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Basic implementation of GenericOutputEntityTransformer
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class OutputEntityTransformer implements GenericOutputEntityTransformer {
	
  /**
   * Write entity to OutputStream.
   * @param entity the Object which should be writed
   * @param entityDataStream the OutputStream
   * @throws IOException
   */
  abstract public void writeTo(Object entity, OutputStream entityDataStream) throws IOException;
  
  /**
   * Try count content length after serialization
   * @param entity the Object for serialization
   * @return size of Object in bytes
   */
  abstract public long getContentLength(Object entity); 
	
}

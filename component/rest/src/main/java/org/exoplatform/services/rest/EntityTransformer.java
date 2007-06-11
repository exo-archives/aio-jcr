/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public interface EntityTransformer<T> {
  
//  public boolean support(Class<?> type);
  
  T readFrom(InputStream entityDataStream) throws IOException;

  void writeTo(T entity, OutputStream entityDataStream) throws IOException;

}

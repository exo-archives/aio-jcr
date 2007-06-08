/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.exoplatform.services.rest.EntityTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class StringEntityTransformer implements EntityTransformer<String> {
  
  public boolean support(Class<?> type) {
    if(type.isInstance(new String()))
      return true;
    return false;
  }
  
  public String readFrom(InputStream entityDataStream) throws IOException {
    
    StringBuffer sb = new StringBuffer();
    int rd = -1;
    while((rd = entityDataStream.read()) != -1) {
      sb.append((char)rd);
    }
    return sb.toString();
  }

  public void writeTo(String entityData, OutputStream entityDataStream)
  throws IOException {
    
    entityDataStream.write(entityData.getBytes());
  }

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class DummyEntityTransformer implements EntityTransformer<InputStream> {

  public InputStream readFrom(InputStream entityDataStream) throws IOException {
    return entityDataStream;
  }

  public void writeTo(InputStream entity, OutputStream entityDataStream) throws IOException {
    byte[] buf = new byte[4096];
    int rd = -1;
    while((rd = entity.read(buf)) != -1)
      entityDataStream.write(buf, 0, rd);    
 //   entityDataStream.flush();
 //   entityDataStream.close();
  }

}

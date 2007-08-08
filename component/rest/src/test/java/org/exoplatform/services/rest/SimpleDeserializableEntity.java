/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.rest.transformer.DeserializableEntity;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SimpleDeserializableEntity implements DeserializableEntity  {

  String data;
  
  public SimpleDeserializableEntity() {
  }
  
  public void readObject(InputStream in) throws IOException {
    StringBuffer sb = new StringBuffer();
    int rd = -1;
    while((rd = in.read()) != -1) {
      sb.append((char)rd);
    }
    data = sb.toString();
  }

}

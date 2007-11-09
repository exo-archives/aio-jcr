/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs.server.filesys;

import java.io.ByteArrayOutputStream;

/**
 * Created by The eXo Platform SAS
 * Author : Sergey Karpenko <sergey.karpenko@exoplatform.com.ua>
 * @version $Id: $
 */

public class ExtByteArrayOutputStream extends ByteArrayOutputStream {
  
  ExtByteArrayOutputStream(){
    super();
  }
  
  ExtByteArrayOutputStream(int size){
    super(size);
  }
  
  
  public byte[] getBuf(){
    return this.buf;
  }
}

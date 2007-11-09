/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class RangedInputStream extends InputStream {
  
  private InputStream nativeInputStream;
  
  private long endRange;
  
  private long position = 0;

  public RangedInputStream(InputStream nativeInputStream, long startRange, long endRange) throws IOException {
    this.nativeInputStream = nativeInputStream;
    this.endRange = endRange;
    
    byte []buff = new byte[4096];

    while (position < (startRange - 1)) {        
      long needToRead = buff.length;
      if (needToRead > (startRange - position)) {
        needToRead = startRange - position;
      }
      
      long readed = nativeInputStream.read(buff, 0, (int)needToRead);

      if (readed < 0) {
        break;        
      }
      
      position += readed;
    }
  }

  public int read() throws IOException {
    if (position > endRange) {
      return -1;
    }
    
    int curReaded = nativeInputStream.read();
    if (curReaded >= 0) {
      position++;
    }    
    return curReaded; 
  }
  
  public int read(byte []buffer) throws IOException {    
    return read(buffer, 0, buffer.length);
  }
  
  public int read(byte []buffer, int offset, int size) throws IOException {
    long needsToRead = size;
    
    if (needsToRead > (endRange - position + 1)) {
      needsToRead = endRange - position + 1;
    }
    
    if (needsToRead == 0) {
      return -1;
    }
    
    int curReaded = nativeInputStream.read(buffer, offset, (int)needsToRead);    
    position += curReaded;
    return curReaded;
  }
  
  public long skip(long skipVal) throws IOException {
    return nativeInputStream.skip(skipVal);
  }
  
  public int available() throws IOException {
    return nativeInputStream.available();
  }
  
  public void close() throws IOException {
    nativeInputStream.close();
  }
  
  public synchronized void mark(int markVal) {
    nativeInputStream.mark(markVal);
  }
  
  public synchronized void reset() throws IOException {
    nativeInputStream.reset();
  }
  
  public boolean markSupported() {
    return nativeInputStream.markSupported();
  }  
  
  
}


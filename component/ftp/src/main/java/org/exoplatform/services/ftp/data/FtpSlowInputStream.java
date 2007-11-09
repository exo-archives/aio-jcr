/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.ftp.data;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpSlowInputStream extends InputStream {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpSlowInputStream");
  
  private InputStream nativeInputStream;
  
  private int blockSize = 0;  
  private int readed = 0;

  public FtpSlowInputStream(InputStream nativeInputStream, int bytesPerSec) {
    this.nativeInputStream = nativeInputStream;
    blockSize = bytesPerSec / 10;
  }

  protected void tryWaiting() {
    if (readed >= blockSize) {
      try {
        Thread.sleep(100);
      } catch (Exception exc) {
        log.info("Unhandled exception until Thread.sleep(...). " + exc.getMessage(), exc);
      }
      readed = 0;
    }    
  }  

  public int read() throws IOException {
    tryWaiting();
    int curReaded = nativeInputStream.read();
    if (curReaded >= 0) {
      readed++;
    }
    return curReaded; 
  }
  
  public int read(byte []buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }
  
  public int read(byte []buffer, int offset, int size) throws IOException {
    tryWaiting();
    
    int curBlockSize = blockSize - readed;
    if (curBlockSize > size) {
      curBlockSize = size;
    }
    
    int curReaded = nativeInputStream.read(buffer, offset, curBlockSize);
    
    readed += curReaded;
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

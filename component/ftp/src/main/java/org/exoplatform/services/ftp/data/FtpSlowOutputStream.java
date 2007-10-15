/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.ftp.data;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpSlowOutputStream extends OutputStream {
  
  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpSlowOutputStream");

  private OutputStream nativeOutputStream;
  
  private int blockSize;
  private int writed = 0;  
  
  public FtpSlowOutputStream(OutputStream nativeOutputStream, int bytesPerSec) {
    this.nativeOutputStream = nativeOutputStream;    
    blockSize = bytesPerSec / 10;
  }
  
  protected void tryWaiting() {
    if (writed >= blockSize) {
      try {
        Thread.sleep(100);
      } catch (Exception exc) {
        log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
      writed = 0;
    }
  }
  
  public void write(int dataByte) throws IOException {
    tryWaiting();
    nativeOutputStream.write(dataByte);
    writed++;
  }
  
  public void write(byte []dataBytes) throws IOException {
    write(dataBytes, 0, dataBytes.length);   
  }
  
  public void write(byte []dataBytes, int offset, int len) throws IOException {
    int allWrited = 0;
    int curOffset = offset;
    
    while (allWrited < len) {      
      tryWaiting();
      
      int curBlockSize = blockSize - writed;
      if ((curBlockSize + allWrited) > len) {
        curBlockSize = len - allWrited;
      }
      
      nativeOutputStream.write(dataBytes, curOffset, curBlockSize);
      
      allWrited += curBlockSize;
      writed += curBlockSize;
      curOffset += curBlockSize;
    }
  }
  
  public void flush() throws IOException {
    nativeOutputStream.flush();
  }
  
  public void close() throws IOException {
    nativeOutputStream.close();
  }

}

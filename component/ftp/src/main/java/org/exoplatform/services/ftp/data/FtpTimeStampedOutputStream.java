/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.ftp.data;

import java.io.IOException;
import java.io.OutputStream;

import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.ftp.client.FtpClientSession;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id$
 */

public class FtpTimeStampedOutputStream extends OutputStream {

  private OutputStream     nativeOutputStream;

  private FtpClientSession clientSession;

  public FtpTimeStampedOutputStream(OutputStream nativeOutputStream, FtpClientSession clientSession) {
    this.nativeOutputStream = nativeOutputStream;
    this.clientSession = clientSession;
  }

  public void write(int dataByte) throws IOException {
    clientSession.refreshTimeOut();
    nativeOutputStream.write(dataByte);
  }

  public void write(byte[] dataBytes) throws IOException {
    write(dataBytes, 0, dataBytes.length);
  }

  public void write(byte[] dataBytes, int offset, int len) throws IOException {
    int allWrited = 0;
    int curOffset = offset;

    clientSession.refreshTimeOut();

    while (allWrited < len) {
      int curBlockSize = FtpConst.FTP_TIMESTAMPED_BLOCK_SIZE;
      if ((curBlockSize + allWrited) > len) {
        curBlockSize = len - allWrited;
      }

      nativeOutputStream.write(dataBytes, curOffset, curBlockSize);
      clientSession.refreshTimeOut();

      allWrited += curBlockSize;
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

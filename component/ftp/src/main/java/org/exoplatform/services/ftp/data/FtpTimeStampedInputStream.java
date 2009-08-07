/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.ftp.data;

import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.ftp.client.FtpClientSession;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id$
 */

public class FtpTimeStampedInputStream extends InputStream {

  private InputStream      nativeInputStream;

  private FtpClientSession clientSession;

  public FtpTimeStampedInputStream(InputStream nativeInputStream, FtpClientSession clientSession) {
    this.nativeInputStream = nativeInputStream;
    this.clientSession = clientSession;
  }

  public int read() throws IOException {
    clientSession.refreshTimeOut();
    return nativeInputStream.read();
  }

  public int read(byte[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public int read(byte[] buffer, int offset, int size) throws IOException {
    clientSession.refreshTimeOut();
    int curBlockSize = FtpConst.FTP_TIMESTAMPED_BLOCK_SIZE;
    if (curBlockSize > size) {
      curBlockSize = size;
    }
    int readed = nativeInputStream.read(buffer, offset, curBlockSize);
    clientSession.refreshTimeOut();
    return readed;
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

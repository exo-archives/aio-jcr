package org.exoplatform.frameworks.ftpclient.data;

import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.log.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.services.log.ExoLogger;

public class FtpSlowInputStream1 extends InputStream {

  private static Log    log       = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpSlowInputStream");

  protected InputStream nativeInputStream;

  protected int         bytesPerSec;

  protected int         blockSize = 0;

  protected int         readed    = 0;

  public FtpSlowInputStream1(InputStream nativeInputStream, int bytesPerSec) {
    this.nativeInputStream = nativeInputStream;
    this.bytesPerSec = bytesPerSec;
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

  public int read(byte[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public int read(byte[] buffer, int offset, int size) throws IOException {
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

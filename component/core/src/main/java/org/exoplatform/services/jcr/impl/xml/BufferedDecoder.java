/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.ws.commons.util.Base64;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class BufferedDecoder extends Base64.Decoder {
  private final static int DEFAULT_BUFFER_SIZE = 4096;

  //
  private final int        BUFFER_SIZE;

  private File             fileBuffer;

  private OutputStream     out;

  public BufferedDecoder() {
    super(DEFAULT_BUFFER_SIZE);
    BUFFER_SIZE = DEFAULT_BUFFER_SIZE;
    out = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
  }

  public BufferedDecoder(int bufferSize) {
    super(bufferSize);
    BUFFER_SIZE = bufferSize;
    out = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
  }

  public InputStream getInputStream() throws IOException {
    flush();
    if (out instanceof ByteArrayOutputStream) {
      return new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
    } else if (out instanceof BufferedOutputStream) {

      out.close();
      return new BufferedInputStream(new FileInputStream(fileBuffer));
    } else {
      throw new IOException("unexpected change of buffer");
    }
  }

  public void remove() throws IOException {
    if ((fileBuffer != null) && fileBuffer.exists()) {
      if (!fileBuffer.delete()) {
        throw new IOException("Cannot remove file " + fileBuffer.getAbsolutePath()
            + " Close all streams.");
      }
    }
  }

  @Override
  public String toString() {
    if (out instanceof ByteArrayOutputStream) {
      return ((ByteArrayOutputStream) out).toString();
    } else if (out instanceof BufferedOutputStream) {
      try {
        out.close();
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileBuffer));
        // StringBuffer stringBuffer = new StringBuffer((int)
        // fileBuffer.length());
        StringBuffer fileData = new StringBuffer(1000);

        byte[] buf = new byte[BUFFER_SIZE];
        int numRead = 0;
        while ((numRead = is.read(buf)) != -1) {

          fileData.append(new String(buf, 0, numRead));

        }
        is.close();
        return fileData.toString();
      } catch (IOException e) {
        return null;
      }

    } else {
      return null;
    }
  }

  private void swapBuffers() throws IOException {
    byte[] data = ((ByteArrayOutputStream) out).toByteArray();
    fileBuffer = File.createTempFile("decoderBuffer", ".tmp");
    fileBuffer.deleteOnExit();
    out = new BufferedOutputStream(new FileOutputStream(fileBuffer), BUFFER_SIZE);
    out.write(data);
  }

  @Override
  protected void writeBuffer(byte[] buffer, int start, int length) throws IOException {
    if (out instanceof ByteArrayOutputStream) {
      if (((ByteArrayOutputStream) out).size() + length > BUFFER_SIZE) {
        swapBuffers();
      }
    }
    out.write(buffer, start, length);
  }

}
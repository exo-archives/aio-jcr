/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS. <br/>Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRObjectInputImpl.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ObjectReaderImpl implements ObjectReader {

  private final InputStream in;

  public ObjectReaderImpl(InputStream in) {
    this.in = new BufferedInputStream(in, 2*1024);
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws IOException {
    in.close();
   
  }

  /**
   * {@inheritDoc}
   */
  public boolean readBoolean() throws IOException {
    int v = in.read();
    if (v < 0) {
      throw new EOFException();
    }
    return (v != 0);
  }

  /**
   * {@inheritDoc}
   */
  public void readFully(byte[] b) throws IOException {
    int l = in.read(b);

    if (l < 0)
     throw new EOFException();
    if (l < b.length && l > 0)
      throw new StreamCorruptedException ("Unexpected EOF in middle of data block.");
  }

  
  /**
   * {@inheritDoc}
   */
  //public byte readByte() throws IOException {
  //  return (byte)in.read();
  //}

  
  /**
   * {@inheritDoc}
   */
  public int readInt() throws IOException {
    byte[] readBuffer = new byte[4];
    readFully(readBuffer);
    return ((readBuffer[0] & 255) << 24) + ((readBuffer[1] & 255) << 16) + 
        ((readBuffer[2] & 255) << 8) + ((readBuffer[3] & 255));
  }

  /**
   * {@inheritDoc}
   */
  public long readLong() throws IOException {
    byte[] readBuffer = new byte[8];

    readFully(readBuffer);
    return (((long) readBuffer[0] << 56) + ((long) (readBuffer[1] & 255) << 48)
        + ((long) (readBuffer[2] & 255) << 40) + ((long) (readBuffer[3] & 255) << 32)
        + ((long) (readBuffer[4] & 255) << 24) + ((readBuffer[5] & 255) << 16)
        + ((readBuffer[6] & 255) << 8) + ((readBuffer[7] & 255) << 0));
  }

  /**
   * {@inheritDoc}
   */
  public long skip(long n) throws IOException {

    long remaining = n;
    int nr;
    byte[] skipBuffer = new byte[1024*1024];

    byte[] localSkipBuffer = skipBuffer;
      
    if (n <= 0) {
        return 0;
    }

    while (remaining > 0) {
        nr = in.read(localSkipBuffer, 0,
            (int) Math.min(1024, remaining));
        if (nr < 0) {
      break;
        }
        remaining -= nr;
    }
    
    return n - remaining;
  }

  /**
   * {@inheritDoc}
   */
  public String readString() throws IOException {
    
    int length = readInt();
    byte[] buf = new byte[length];
    readFully(buf);
    return new String(buf, Constants.DEFAULT_ENCODING);
  }
}

/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

package org.exoplatform.services.jcr.webdav.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class RangedInputStream extends InputStream {

  private InputStream nativeInputStream;

  private long        endRange;

  private long        position = 0;

  public RangedInputStream(InputStream nativeInputStream, long startRange, long endRange) throws IOException {
    this.nativeInputStream = nativeInputStream;
    this.endRange = endRange;

    byte[] buff = new byte[0x1000];

    while (position < (startRange - 1)) {
      long needToRead = buff.length;
      if (needToRead > (startRange - position)) {
        needToRead = startRange - position;
      }

      long readed = nativeInputStream.read(buff, 0, (int) needToRead);

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

  public int read(byte[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public int read(byte[] buffer, int offset, int size) throws IOException {
    long needsToRead = size;

    if (needsToRead > (endRange - position + 1)) {
      needsToRead = endRange - position + 1;
    }

    if (needsToRead == 0) {
      return -1;
    }

    int curReaded = nativeInputStream.read(buffer, offset, (int) needsToRead);
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

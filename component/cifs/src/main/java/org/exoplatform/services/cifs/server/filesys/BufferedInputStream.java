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
package org.exoplatform.services.cifs.server.filesys;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * The goal of this class is to provide raw write of large amount of data to jcr
 * property, without use EditableBinaryValue. It's much more faster (setValue)
 * but provide only sequential write (non random write).
 * 
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class BufferedInputStream extends InputStream implements Runnable {
  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs.smb.filesys.BufferedInputStream");

  private static final int BufferSize = 2 * 64 * 1024;

  private Node node;

  // flag for stop thread
  private boolean isStop = false;

  private byte[] buffer;

  private int currentBufPosition = -1;

  private int currentBufSize = 0;

  private boolean newBuffer = false;

  private boolean readyToAppload = true;

  public BufferedInputStream(Node n) {
    node = n;

    if (buffer == null) {
      buffer = new byte[BufferSize];
      
    }
    newBuffer = false;
    isStop = false;
  }

  public BufferedInputStream() {
    if (buffer == null) {
      buffer = new byte[BufferSize];
    }
    newBuffer = false;
    isStop = false;
  }

  public void setNodeRef(Node n) {
    node = n;
  }

  public int read() throws IOException {
    logger.debug("read");
    synchronized (buffer) {
      // wait for buffer upload
      while (!newBuffer) {
        try {
          buffer.wait(100);
          if (isStop == true) {
            return -1; // signal the end of stream
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      int ret = buffer[currentBufPosition];
      if (currentBufPosition == currentBufSize - 1) {
        // set flag to ready update buffer
        readyToAppload = true;
        newBuffer = false;
      }

      currentBufPosition++;
      return (ret);
    }

  }

  public void uploadBuffer(byte[] buf) {
    logger.debug("upload bufer: size " + buf.length);
    synchronized (buffer) {
      while (!readyToAppload) {
        try {
          buffer.wait(100);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (buffer == null) {
        buffer = new byte[BufferSize];
      }

      System.arraycopy(buf, 0, buffer, 0, buf.length);
      currentBufSize = buf.length;

      currentBufPosition = 0;
      newBuffer = true;
      readyToAppload = false;
    }

  }

  public void run() {
    logger.debug("run");
    try {
      node.getNode("jcr:content").getProperty("jcr:data").setValue(this);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void stop() {
    isStop = true;
  }

}

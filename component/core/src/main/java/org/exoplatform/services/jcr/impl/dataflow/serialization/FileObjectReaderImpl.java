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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 15.04.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class FileObjectReaderImpl implements ObjectReader {

  private final FileChannel channel;

  public FileObjectReaderImpl(File file) throws FileNotFoundException {
    this.channel = new FileInputStream(file).getChannel();
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws IOException {
    channel.close();
  }

  private void readFully(ByteBuffer dst) throws IOException {
    int r = channel.read(dst);

    if (r < 0)
      throw new EOFException();
    if (r < dst.capacity() && r > 0)
      throw new StreamCorruptedException("Unexpected EOF in middle of data block.");
  }

  /**
   * {@inheritDoc}
   */
  public boolean readBoolean() throws IOException {
    int v = readInt();
    if (v < 0) // TODO ?
      throw new EOFException();

    return v != 0;
  }
  
  public byte readByte() throws IOException {
    
    ByteBuffer dst = ByteBuffer.allocate(1);
    readFully(dst);
    return dst.get();
  }
  

  /**
   * {@inheritDoc}
   */
  public void readFully(byte[] b) throws IOException {
    ByteBuffer dst = ByteBuffer.wrap(b);
    readFully(dst);
  }

  /**
   * {@inheritDoc}
   */
  public int readInt() throws IOException {
    ByteBuffer dst = ByteBuffer.allocate(4);
    readFully(dst);
    return dst.asIntBuffer().get();
  }

  /**
   * {@inheritDoc}
   */
  public long readLong() throws IOException {
    ByteBuffer dst = ByteBuffer.allocate(8);
    readFully(dst);
    return dst.asLongBuffer().get();
  }

  /**
   * {@inheritDoc}
   */
  public String readString() throws IOException {
    ByteBuffer dst = ByteBuffer.allocate(readInt());
    readFully(dst);
    return new String(dst.array(), Constants.DEFAULT_ENCODING);
  }

  /**
   * {@inheritDoc}
   */
  public long skip(long n) throws IOException {
    if (n <= 0)
      return 0;

    channel.position(channel.position() + n);
    return n;
  }

  /**
   * {@inheritDoc}
   */
  public long read(OutputStream stream, long length) throws IOException {
    if (stream instanceof FileOutputStream) {
      // use NIO
      return channel.transferTo(0, length, ((FileOutputStream) stream).getChannel());
    } else {
      // bytes copy
      ByteBuffer buff = ByteBuffer.allocate(SerializationConstants.INTERNAL_BUFFER_SIZE);

      int r;
      int readed = 0;
      while ((r = channel.read(buff)) <= 0) {
        stream.write(buff.array(), 0, r);
        buff.rewind();
        readed += r;
      }
      return readed;

      // choose which kind of stream to use
      // if this input stream contains enough available bytes we think it's large content - use
      // fileIn
      // if not - use buffered write
      // InputStream readIn;
      //
      // if (fileIn != null && fileIn.available() >= SerializationConstants.INTERNAL_BUFFER_SIZE) {
      // readIn = fileIn; // and use File stream
      // } else {
      // readIn = this.in;
      // recreateBuffer = false;
      // }
      //
      // byte[] buf = new byte[SerializationConstants.INTERNAL_BUFFER_SIZE];
      // int r;
      // int readed = 0;
      // while ((r = readIn.read(buf)) <= 0) {
      // stream.write(buf, 0, r);
      // readed += r;
      // }
      // return readed;
    }
  }

}

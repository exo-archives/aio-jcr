/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.transport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesPacket.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesPacket extends AbstractPacket {

  /**
   * Time stamp.
   */
  private long   timeStamp;

  /**
   * CRC.
   */
  private String crc;

  /**
   * ChangesLog file count
   */
  private int    fileCount;

  /**
   * Current data position in total data byte array.
   */
  private long   offset;

  /**
   * Current data.
   */
  private byte[] buffer;

  /**
   * Constructor.
   * 
   * @param type
   *          see AsyncPacketTypes
   * @param priority
   * @param crc
   * @param timeStamp
   * @param fileCount
   * @param offset
   * @param buffer
   */
  public ChangesPacket(int type,
                       int priority,
                       String crc,
                       long timeStamp,
                       int fileCount,
                       long offset,
                       byte[] buffer) {
    super(type, priority);
    this.crc = crc;
    this.timeStamp = timeStamp;
    this.fileCount = fileCount;
    this.offset = offset;
    this.buffer = buffer;
  }

  /**
   * ChangesPacket constructor.
   * 
   */
  public ChangesPacket() {
    super();
  }

  public String getCRC() {
    return this.crc;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public long getFileCount() {
    return this.fileCount;
  }

  public long getOffset() {
    return this.offset;
  }

  public byte[] getBuffer() {
    return this.buffer;
  }

  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);

    out.writeInt(priority);

    if (crc != null) {
      byte[] b = crc.getBytes(Constants.DEFAULT_ENCODING);
      out.writeInt(NOT_NULL_VALUE);
      out.writeInt(b.length);
      out.write(b);
    } else {
      out.writeInt(NULL_VALUE);
    }
    out.writeLong(timeStamp);

    out.writeInt(fileCount);
    out.writeLong(offset);

    if (buffer != null) {
      out.writeInt(NOT_NULL_VALUE);
      out.writeInt(buffer.length);
      out.write(buffer);
    } else
      out.writeInt(NULL_VALUE);
  }

  /**
   * {@inheritDoc}
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);

    priority = in.readInt();
    if (in.readInt() == NOT_NULL_VALUE) {
      byte[] buf = new byte[in.readInt()];
      in.readFully(buf);
      crc = new String(buf, Constants.DEFAULT_ENCODING);
    } else {
      crc = null;
    }
    timeStamp = in.readLong();

    fileCount = in.readInt();
    offset = in.readLong();

    if (in.readInt() == NOT_NULL_VALUE) {
      int bufSize = in.readInt();
      buffer = new byte[bufSize];
      in.readFully(buffer);
    } else
      buffer = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return super.toString() + " [fc:" + getFileCount() + ", t:" + getTimeStamp() + "]";
  }

}

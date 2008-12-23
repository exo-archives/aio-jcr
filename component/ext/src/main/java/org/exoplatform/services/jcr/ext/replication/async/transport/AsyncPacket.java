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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncPacket.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncPacket implements Externalizable {
  
  /**
   * Constant will be used for serialization 'null' value.
   */
  private static final int NULL_VALUE = -1;
  
  /**
   * Constant will be used for serialization not 'null' value.
   */
  private static final int NOT_NULL_VALUE = 1;

  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -138895618077433063L;
  
  /**
   * The definition of max packet size.
   */
  public static final int MAX_PACKET_SIZE = 1024 * 16;

  /**
   * Array of byte to binary data.
   */
  private byte[]          buffer;

  /**
   * Field to size.   
   */
  private long            size;

  /**
   * Packet type.
   */
  private int             type;

  /**
   * Offset to large file.
   */
  private long            offset;
  
  /**
   * CRC check sum.
   */
  private String          crc;
  
  /**
   * Time stamp.
   */
  private long            timeStamp;
  
  /**
   * The priority of transmitter. 
   */
  private int             transmitterPriority;
  
  /**
   * Packet  constructor.
   * The empty constructor need for Externalizable 
   */
  public AsyncPacket() {
  }
  
  /**
   * Packet  constructor.
   *
   * @param type 
   *          packet type
   * @param size 
   *          size value 
   * @param crc
   *          check sum value
   * @param timeStamp
   *          the timeStampValue
   * @param transmitterPriority
   *          the priority value of transmitters         
   */
  public AsyncPacket(int type, 
                     long size, 
                     String crc, 
                     long timeStamp, 
                     int transmitterPriority) {
    this.type = type;
    this.size = size;
    this.crc = crc;
    this.timeStamp = timeStamp;
    this.transmitterPriority = transmitterPriority;
  }
  
  /**
   * Packet  constructor.
   *
   * @param type 
   *          packet type
   * @param size 
   *          size value 
   * @param crc
   *          check sum value
   * @param timeStamp
   *          the timeStampValue
   * @param transmitterPriority
   *          the priority value of transmitters   
   * @param buf
   *          binary data
   * @param offset
   *          offset value
   */
  public AsyncPacket(int type, 
                     long size, 
                     String crc, 
                     long timeStamp, 
                     int transmitterPriority,
                     byte[] buf,
                     long offset) {
    this(type, size, crc, timeStamp, transmitterPriority);
    
    buffer = new byte[buf.length];

    System.arraycopy(buf, 0, buffer, 0, buf.length);
    
    this.offset = offset;
  }
  
  
  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    if (buffer != null) {
      out.writeInt(NOT_NULL_VALUE);
      out.writeInt(buffer.length);
      out.write(buffer);
    } else 
      out.writeInt(NULL_VALUE);
    
    out.writeLong(size);
    out.writeInt(type);
    out.writeLong(offset);
    out.writeLong(timeStamp);
    out.writeInt(transmitterPriority);
    
    if (crc != null) {
      out.writeInt(NOT_NULL_VALUE); 
      out.writeInt(crc.length());
      out.write(crc.getBytes());
    } else 
      out.writeInt(NULL_VALUE);
  }

  /**
   * {@inheritDoc}
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    if (in.readInt() == NOT_NULL_VALUE) {
      int bufSize = in.readInt();
      buffer = new byte[bufSize];
      in.readFully(buffer);
    } else
      buffer = null;

    size = in.readLong();
    type = in.readInt();
    offset = in.readLong();
    timeStamp = in.readLong();
    transmitterPriority = in.readInt();

    if (in.readInt() == NOT_NULL_VALUE) {
      byte[] buf = new byte[in.readInt()];
      in.readFully(buf);
      crc = new String(buf);
    } else
      crc = null;
    
  }

  public byte[] getBuffer() {
    return buffer;
  }

  public long getSize() {
    return size;
  }

  public int getType() {
    return type;
  }

  public long getOffset() {
    return offset;
  }

  public String getCRC() {
    return crc;
  }

  public int getTransmitterPriority() {
    return transmitterPriority;
  }

  public long getTimeStamp() {
    return timeStamp;
  }
}

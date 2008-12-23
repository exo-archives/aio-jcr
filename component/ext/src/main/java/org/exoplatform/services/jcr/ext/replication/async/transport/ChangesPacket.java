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
 * Created by The eXo Platform SAS Author : Karpenko Sergiy
 * karpenko.sergiy@gmail.com 23 Ãðó 2008
 */
public class ChangesPacket extends AsyncPacket {

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
   * Changes log root node Id.
   */
  private String nodeId;
  

  /**
   * Constructor.
   * 
   * @param type see AsyncPacketTypes
   * @param crc
   * @param timeStamp
   * @param transmitterPriority
   * @param fileCount
   * @param offset
   * @param buffer
   */
  public ChangesPacket(int type,
                          int transmitterPriority,
                          String crc,
                          long timeStamp,
                          String nodeId,
                          int fileCount,
                          long offset,
                          byte[] buffer) {
    super(type, transmitterPriority);
    this.crc = crc;
    this.timeStamp = timeStamp;
    this.fileCount = fileCount;
    this.offset = offset;
    this.buffer = buffer;
    this.nodeId = nodeId;
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

  public String getNodeId() {
    return this.nodeId;
  }
  
  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
        
    if (crc != null) {
      byte[] b = crc.getBytes(Constants.DEFAULT_ENCODING);
      out.writeInt(NOT_NULL_VALUE);
      out.writeInt(b.length);
      out.write(b);
    } else {
      out.writeInt(NULL_VALUE);
    }
    out.writeLong(timeStamp);
    
    if (nodeId != null) {
      byte[] b = nodeId.getBytes(Constants.DEFAULT_ENCODING);
      out.writeInt(NOT_NULL_VALUE);
      out.writeInt(b.length);
      out.write(b);
    } else {
      out.writeInt(NULL_VALUE);
    }
    
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

    if (in.readInt() == NOT_NULL_VALUE) {
      byte[] buf = new byte[in.readInt()];
      in.readFully(buf);
      crc = new String(buf, Constants.DEFAULT_ENCODING);
    } else {
      crc = null;
    }
    timeStamp = in.readLong();

    if (in.readInt() == NOT_NULL_VALUE) {
      byte[] buf = new byte[in.readInt()];
      in.readFully(buf);
      nodeId = new String(buf, Constants.DEFAULT_ENCODING);
    } else {
      nodeId = null;
    }
    
    fileCount = in.readInt();
    offset = in.readLong();

    if (in.readInt() == NOT_NULL_VALUE) {
      int bufSize = in.readInt();
      buffer = new byte[bufSize];
      in.readFully(buffer);
    } else
      buffer = null;
  }

}

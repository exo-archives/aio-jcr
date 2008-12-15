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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.services.jcr.ext.replication.FixupStream;
import org.exoplatform.services.jcr.ext.replication.Packet;

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
   * serialVersionUID.
   */
  private static final long serialVersionUID = -138895618077433063L;
  
  /**
   * The definition of max packet size.
   */
  public static final int MAX_PACKET_SIZE = 1024 * 16;

  /**
   * The packet identifier.
   */
  private String          identifier;
  
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
   * Owner name.
   */
  private String          ownName = "";
  
  /**
   * Name of file.
   */
  private String          fileName = " ";
  
  /**
   * The system identifier.
   */
  private String          systemId = " ";
  
  /**
   * Time stamp.
   */
  private Calendar        timeStamp = Calendar.getInstance();
  
  /**
   * The names of files .
   */
  private List<String>    fileNameList = new ArrayList<String>();
  
  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

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
   * @param buf 
   *          binary data
   * @param identifier 
   *          packet identifier
   */
  public AsyncPacket(int type, long size, byte[] buf, String identifier) {
    this.identifier = identifier;
    this.type = type;
    this.size = size;
    buffer = new byte[buf.length];

    for (int i = 0; i < buf.length; i++)
      buffer[i] = buf[i];
  }
  
  /**
   * Packet  constructor.
   *
   * @param type
   *          packet type
   * @param identifier
   *          packet identifier
   * @param ownName
   *          owner name
   */
  public AsyncPacket(int type, String identifier, String ownName) {
    this.type = type;
    this.identifier = identifier;
    buffer = new byte[1];
    this.ownName = ownName;
  }
  
  /**
   * Packet  constructor.
   *
   * @param type
   *          packet type
   * @param identifier
   *          packet identifier
   * @param ownName
   *          owner name
   * @param fileName
   *          file name
   */
  public AsyncPacket(int type, String identifier, String ownName, String fileName) {
    this(type, identifier, ownName);
    this.fileName = fileName;
  }
  

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public byte[] getBuffer() {
    return buffer;
  }


  public void setBuffer(byte[] buf) {
    buffer = new byte[buf.length];
    for (int i = 0; i < buf.length; i++)
      buffer[i] = buf[i];
  }


  public long getSize() {
    return size;
  }


  public void setSize(long size) {
    this.size = size;
  }


  public int getType() {
    return type;
  }


  public void setType(int type) {
    this.type = type;
  }


  public long getOffset() {
    return offset;
  }


  public void setOffset(long offset) {
    this.offset = offset;
  }


  public String getOwnName() {
    return ownName;
  }


  public void setOwnName(String ownName) {
    this.ownName = ownName;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  
  /**
   * getAsByteArray.
   *
   * @param packet
   *          Packet object 
   * @return byte[]
   *           the binary value
   * @throws IOException
   *           generate the IOExaption
   */
  public static byte[] getAsByteArray(AsyncPacket packet) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(packet);

    byte[] bArray = os.toByteArray();
    return bArray;
  }
  
  /**
   * getAsPacket.
   *
   * @param byteArray
   *          binary data
   * @return Packet
   *           the Packet object from bytes
   * @throws IOException
   *           generate the IOExeption
   * @throws ClassNotFoundException
   *           generate the ClassNotFoundException 
   */
  public static AsyncPacket getAsPacket(byte[] byteArray) throws IOException, ClassNotFoundException {
    ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
    ObjectInputStream ois = new ObjectInputStream(is);
    AsyncPacket objRead = (AsyncPacket) ois.readObject();

    return objRead;
  }
  
  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(buffer.length);
    out.write(buffer);
    out.writeLong(size);
    out.writeInt(type);
    out.writeLong(offset);

    out.writeInt(identifier.getBytes().length);
    out.write(identifier.getBytes());

    out.writeInt(ownName.getBytes().length);
    out.write(ownName.getBytes());

    // write timeStamp
    out.writeLong(timeStamp.getTimeInMillis());

    out.writeInt(fileName.getBytes().length);
    out.write(fileName.getBytes());

    // write list
    out.writeInt(fileNameList.size());
    for (String fName : fileNameList) {
      out.writeInt(fName.getBytes().length);
      out.write(fName.getBytes());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    int bufSize = in.readInt();
    buffer = new byte[bufSize];
    in.readFully(buffer);

    size = in.readLong();
    type = in.readInt();
    offset = in.readLong();

    byte[] buf = new byte[in.readInt()];
    in.readFully(buf);
    identifier = new String(buf);

    buf = new byte[in.readInt()];
    in.readFully(buf);
    ownName = new String(buf);

    // set timeStamp
    timeStamp.setTimeInMillis(in.readLong());

    buf = new byte[in.readInt()];
    in.readFully(buf);
    fileName = new String(buf);

    // read list
    int listSize = in.readInt();
    for (int i = 0; i < listSize; i++) {
      buf = new byte[in.readInt()];
      in.readFully(buf);
      fileNameList.add(new String(buf));
    }
  }
}

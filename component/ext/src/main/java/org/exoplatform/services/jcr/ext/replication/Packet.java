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
package org.exoplatform.services.jcr.ext.replication;

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

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class Packet implements Externalizable {

  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -238898618077133064L;

  /**
   * PacketType.
   * Definition of Packet types
   */
  public final class PacketType {
    /**
     * CHANGESLOG.
     *   the pocket type for ChangesLog without stream
     */
    public static final int CHANGESLOG                           = 1;
    
    public static final int FIRST_CHANGESLOG_WITH_STREAM         = 2;

    public static final int FIRST_PACKET_OF_STREAM               = 3;

    public static final int PACKET_OF_STREAM                     = 4;

    public static final int LAST_PACKET_OF_STREAM                = 5;

    public static final int LAST_CHANGESLOG_WITH_STREAM          = 6;

    public static final int CHANGESLOG_FIRST_PACKET              = 7;

    public static final int CHANGESLOG_MIDDLE_PACKET             = 8;

    public static final int CHANGESLOG_LAST_PACKET               = 9;

    public static final int CHANGESLOG_WITH_STREAM_FIRST_PACKET  = 10;

    public static final int CHANGESLOG_WITH_STREAM_MIDDLE_PACKET = 11;

    public static final int CHANGESLOG_WITH_STREAM_LAST_PACKET   = 12;

    /**
     * ADD_OK.
     *   the pocket type for information of successful save 
     */
    public static final int ADD_OK                               = 13;

    /**
     * GET_CHANGESLOG_UP_TO_DATE.
     *   the pocket type for initialize synchronization mechanism  
     */
    public static final int GET_CHANGESLOG_UP_TO_DATE            = 14;

    /**
     * BINARY_FILE_FIRST_PACKET.
     *   the pocket type for first packet to binary file
     */
    public static final int BINARY_FILE_FIRST_PACKET             = 15;

    /**
     * BINARY_FILE_MIDDLE_PACKET.
     *  the pocket type for middle packet to binary file
     */  
    public static final int BINARY_FILE_MIDDLE_PACKET            = 16;

    /**
     * BINARY_FILE_LAST_PACKET.
     *   the pocket type for last packet to binary file
     */
    public static final int BINARY_FILE_LAST_PACKET              = 17;

    /**
     * ALL_BINARY_FILE_TRANSFERRED_OK.
     *   the pocket type for information of all files was transferred
     */
    public static final int ALL_BINARY_FILE_TRANSFERRED_OK       = 18;

    /**
     * ALL_CHANGESLOG_SAVED_OK.
     *   the pocket type for information of all ChangesLogs was saved 
     */
    public static final int ALL_CHANGESLOG_SAVED_OK              = 19;

    /**
     * SYNCHRONIZED_OK.
     *   the pocket type for information of synchronized well 
     */
    public static final int SYNCHRONIZED_OK                      = 20;

    /**
     * INITED_IN_CLUSTER.
     *   the pocket type for information of member was initialized 
     */
    public static final int INITED_IN_CLUSTER                    = 21;

    /**
     * ALL_INITED.
     *   the pocket type for information of all members was initialized
     */
    public static final int ALL_INITED                           = 22;

    /**
     * OLD_CHANGESLOG_REMOVED_OK.
     *   the pocket type for information of old ChangesLogs was removed
     */
    public static final int OLD_CHANGESLOG_REMOVED_OK            = 23;

    /**
     * NEED_TRANSFER_COUNTER.
     *   the pocket type for information of how much ChangesLogs will be transfered
     */  
    public static final int NEED_TRANSFER_COUNTER                = 24;

    public static final int REMOVED_OLD_CHANGESLOG_COUNTER       = 25;

    public static final int MEMBER_STARTED                       = 26;

    public static final int BIG_PACKET_FIRST                     = 27;

    public static final int BIG_PACKET_MIDDLE                    = 28;

    public static final int BIG_PACKET_LAST                      = 29;

    public static final int GET_ALL_PRIORITY                     = 30;

    public static final int OWN_PRIORITY                         = 31;

    public static final int BINARY_CHANGESLOG_FIRST_PACKET       = 32;

    public static final int BINARY_CHANGESLOG_MIDDLE_PACKET      = 33;

    public static final int BINARY_CHANGESLOG_LAST_PACKET        = 34;

    /**
     * Private PacketType constructor.
     */
    private PacketType() {
    }
  }

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
   * The packet identifier.
   */
  private String          identifier;

  /**
   * FixupStream  to large file.
   */
  private FixupStream     fixupStream;

  /**
   * Owner name.
   */
  private String          ownName         = new String(" ");

  /**
   * Time stamp.
   */
  private Calendar        timeStamp       = Calendar.getInstance();

  /**
   * Name of file.
   */
  private String          fileName        = new String(" ");

  /**
   * The system identifier.
   */
  private String          systemId        = new String(" ");

  /**
   * The names of files .
   */
  private List<String>    fileNameList    = new ArrayList<String>();

  /**
   * Packet  constructor.
   * The empty constructor need for Externalizable 
   */
  public Packet() {
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
  public Packet(int type, long size, byte[] buf, String identifier) {
    this.identifier = identifier;
    this.type = type;
    this.size = size;
    buffer = new byte[buf.length];

    for (int i = 0; i < buf.length; i++)
      buffer[i] = buf[i];

    fixupStream = new FixupStream();
  }

  /**
   * Packet  constructor.
   *
   * @param type 
   *          packet type
   * @param identifier
   *          packet identifier
   */
  public Packet(int type, String identifier) {
    this.type = type;
    this.identifier = identifier;
    buffer = new byte[1];
    fixupStream = new FixupStream();
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
  public Packet(int type, String identifier, String ownName) {
    this.type = type;
    this.identifier = identifier;
    buffer = new byte[1];
    fixupStream = new FixupStream();
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
  public Packet(int type, String identifier, String ownName, String fileName) {
    this(type, identifier, ownName);
    this.fileName = fileName;
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
   * @param fileNameList
   *          the list with files name 
   */
  public Packet(int type, String identifier, String ownName, List<String> fileNameList) {
    this(type, identifier, ownName);
    this.fileNameList = fileNameList;
  }

  /**
   * Packet  constructor.
   *
   * @param type
   *          packet type
   * @param fs
   *          the FixupStream for ChangesLog with stream 
   * @param identifier
   *          packet identifier
   */
  public Packet(int type, FixupStream fs, String identifier) {
    this.type = type;
    fixupStream = fs;
    this.identifier = identifier;
    buffer = new byte[1];
  }

  /**
   * Packet  constructor.
   *
   * @param type
   *          packet type
   * @param fs
   *          the FixupStream for ChangesLog with stream
   * @param identifier
   *          packet identifier
   * @param buf
   *          binary data
   */
  public Packet(int type, FixupStream fs, String identifier, byte[] buf) {
    this.type = type;
    fixupStream = fs;
    this.identifier = identifier;

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
   * @param timeStamp
   *          the Calendar object with "time"
   */
  public Packet(int type, String identifier, String ownName, Calendar timeStamp) {
    this(type, identifier, ownName);
    this.timeStamp = timeStamp;
  }

  /**
   * Packet  constructor.
   *
   * @param type
   *          packet type
   * @param ownName
   *          owner name
   * @param size
   *          the size value
   * @param identifier
   *          packet identifier
   */
  public Packet(int type, String ownName, long size, String identifier) {
    this(type, identifier, ownName);
    this.size = size;
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

    out.writeInt(fixupStream.getItemSateId());
    out.writeInt(fixupStream.getValueDataId());

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

    int item = in.readInt();
    int value = in.readInt();
    fixupStream = new FixupStream(item, value);

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

  /**
   * getIdentifier.
   *
   * @return String 
   *           the packet identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * getByteArray.
   *
   * @return byte[]
   *           the binary data
   */
  public byte[] getByteArray() {
    return buffer;
  }

  /**
   * getSize.
   *
   * @return long
   *           the size value
   */
  public long getSize() {
    return size;
  }

  /**
   * setSize.
   *
   * @param size 
   *          size value
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * getPacketType.
   *
   * @return integer
   *           the packet type
   */
  public int getPacketType() {
    return type;
  }

  /**
   * getOffset.
   *
   * @return long
   *           the offset value
   */
  public long getOffset() {
    return offset;
  }

  /**
   * setOffset.
   *
   * @param offset
   *          the offset value
   */
  public void setOffset(long offset) {
    this.offset = offset;
  }

  /**
   * getFixupStream.
   *
   * @return FixupStream
   *           the FixupStream object 
   */
  public FixupStream getFixupStream() {
    return fixupStream;
  }

  /**
   * setFixupStream.
   *
   * @param fs
   *          FixupStream object
   */
  public void setFixupStream(FixupStream fs) {
    fixupStream = fs;
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
  public static byte[] getAsByteArray(Packet packet) throws IOException {
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
  public static Packet getAsPacket(byte[] byteArray) throws IOException, ClassNotFoundException {
    ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
    ObjectInputStream ois = new ObjectInputStream(is);
    Packet objRead = (Packet) ois.readObject();

    return objRead;
  }

  /**
   * getOwnerName.
   *
   * @return String
   *           the owner name
   */
  public String getOwnerName() {
    return ownName;
  }

  /**
   * setOwnName.
   *
   * @param ownName
   *          owner name
   */
  public void setOwnName(String ownName) {
    this.ownName = ownName;
  }

  /**
   * getTimeStamp.
   *
   * @return Calendar
   *            the timeStamp
   */
  public Calendar getTimeStamp() {
    return timeStamp;
  }

  /**
   * setTimeStamp.
   *
   * @param timeStamp
   *          set the timeStamp (Calendar)
   */
  public void setTimeStamp(Calendar timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   * getFileName.
   *
   * @return String
   *           the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * setFileName.
   *
   * @param fileName
   *          the file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * getFileNameList.
   *
   * @return List
   *           the list of fileNames
   */
  public List<String> getFileNameList() {
    return fileNameList;
  }

  /**
   * getSystemId.
   *
   * @return String
   *           the systemId
   */
  public String getSystemId() {
    return systemId;
  }

  /**
   * setSystemId.
   *
   * @param systemId
   *          the systemId
   */
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }
}

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

  private static final long serialVersionUID = -238898618077133064L;

  public final class PacketType {
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

    public static final int ADD_OK                               = 13;

    public static final int GET_CHANGESLOG_UP_TO_DATE            = 14;

    public static final int BINARY_FILE_FIRST_PACKET             = 15;

    public static final int BINARY_FILE_MIDDLE_PACKET            = 16;

    public static final int BINARY_FILE_LAST_PACKET              = 17;

    public static final int ALL_BINARY_FILE_TRANSFERRED_OK       = 18;

    public static final int ALL_CHANGESLOG_SAVED_OK              = 19;

    public static final int SYNCHRONIZED_OK                      = 20;

    public static final int INITED_IN_CLUSTER                    = 21;

    public static final int ALL_INITED                           = 22;

    public static final int OLD_CHANGESLOG_REMOVED_OK            = 23;

    public static final int NEED_TRANSFER_COUNTER                = 24;

    public static final int REMOVED_OLD_CHANGESLOG_COUNTER       = 25;

    public static final int MEMBER_STARTED                       = 26;

    public static final int BIG_PACKET_FIRST                     = 27;

    public static final int BIG_PACKET_MIDDLE                    = 28;

    public static final int BIG_PACKET_LAST                      = 29;

    public static final int GET_ALL_PRIORITY                     = 30;

    public static final int OWN_PRIORITY                         = 31;

    private PacketType() {
    }
  }

  public static final int MAX_PACKET_SIZE = 1024 * 16;

  // TODO [rainf0x] need normalization the name of fields
  private byte[]          buffer;

  private long            size;

  private int             type;

  private long            offset;

  private String          identifier;

  private FixupStream     fixupStream;

  private String          ownName         = new String(" ");

  private Calendar        timeStamp       = Calendar.getInstance();

  private String          fileName        = new String(" ");

  private String          systemId        = new String(" ");

  private List<String>    fileNameList    = new ArrayList<String>();

  public Packet() {
  }

  public Packet(int type, long size, byte[] buf, String identifier) {
    this.identifier = identifier;
    this.type = type;
    this.size = size;
    buffer = new byte[buf.length];

    for (int i = 0; i < buf.length; i++)
      buffer[i] = buf[i];

    fixupStream = new FixupStream();
  }

  public Packet(int type, String identifier) {
    this.type = type;
    this.identifier = identifier;
    buffer = new byte[1];
    fixupStream = new FixupStream();
  }

  public Packet(int type, String identifier, String ownName) {
    this.type = type;
    this.identifier = identifier;
    buffer = new byte[1];
    fixupStream = new FixupStream();
    this.ownName = ownName;
  }

  public Packet(int type, String identifier, String ownName, String fileName) {
    this(type, identifier, ownName);
    this.fileName = fileName;
  }

  public Packet(int type, String identifier, String ownName, List<String> fileNameList) {
    this(type, identifier, ownName);
    this.fileNameList = fileNameList;
  }

  public Packet(int type, FixupStream fs, String identifier) {
    this.type = type;
    fixupStream = fs;
    this.identifier = identifier;
    buffer = new byte[1];
  }

  public Packet(int type, FixupStream fs, String identifier, byte[] buf) {
    this.type = type;
    fixupStream = fs;
    this.identifier = identifier;

    buffer = new byte[buf.length];
    for (int i = 0; i < buf.length; i++)
      buffer[i] = buf[i];
  }

  public Packet(int type, String identifier, String ownName, Calendar timeStamp) {
    this(type, identifier, ownName);
    this.timeStamp = timeStamp;
  }

  public Packet(int type, String ownName, long size, String identifier) {
    this(type, identifier, ownName);
    this.size = size;
  }

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

  public String getIdentifier() {
    return identifier;
  }

  public byte[] getByteArray() {
    return buffer;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public int getPacketType() {
    return type;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public FixupStream getFixupStream() {
    return fixupStream;
  }

  public void setFixupStream(FixupStream fs) {
    fixupStream = fs;
  }

  public static byte[] getAsByteArray(Packet packet) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(packet);

    byte[] bArray = os.toByteArray();
    return bArray;
  }

  public static Packet getAsPacket(byte[] byteArray) throws IOException, ClassNotFoundException {
    ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
    ObjectInputStream ois = new ObjectInputStream(is);
    Packet objRead = (Packet) ois.readObject();

    return objRead;
  }

  public String getOwnerName() {
    return ownName;
  }

  public void setOwnName(String ownName) {
    this.ownName = ownName;
  }

  public Calendar getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Calendar timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public List<String> getFileNameList() {
    return fileNameList;
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }
}

/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.dataflow.replication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua 24.11.2006
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class Packet implements Externalizable {
  public class PacketType {
    public static final int ItemDataChangesLog                    = 1;

    public static final int First_ItemDataChangesLog_with_Streams = 2;

    public static final int First_Packet_of_Stream                = 3;

    public static final int Packet_of_Stream                      = 4;

    public static final int Last_Packet_of_Stream                 = 5;

    public static final int Last_ItemDataChangesLog_with_Streams  = 6;
    
    public static final int ItemDataChangesLog_First_Packet       = 7;
    
    public static final int ItemDataChangesLog_Middle_Packet      = 8;
    
    public static final int ItemDataChangesLog_Last_Packet        = 9;
  }
  
  public static final int MAX_PACKET_SIZE = 1024*16;        

  private byte[] buffer_;

  private long   size_;

  private int    type_;

  private long   offset_;
  
  private String identifier;
  
  private FixupStream fixupStream;

  public Packet() {
  }

  public Packet(int type, long size, byte[] buffer, String identifier_) {
    identifier = identifier_;
    type_ = type;
    size_ = size;
    buffer_ = new byte[buffer.length];

    for (int i = 0; i < buffer.length; i++)
      buffer_[i] = buffer[i];
    
    fixupStream = new FixupStream();
  }

  public Packet(int type, String identifier_) {
    type_ = type;
    identifier = identifier_;
    buffer_ = new byte[1];
    fixupStream = new FixupStream();
  }
  
  public Packet(int type, FixupStream fs, String identifier_) {
    type_ = type;
    fixupStream = fs;
    identifier = identifier_;
    buffer_ = new byte[1];
  }
  
  public Packet(int type, FixupStream fs, String identifier_, byte[] buf) {
    type_ = type;
    fixupStream = fs;
    identifier = identifier_;
    
    buffer_ = new byte[buf.length];
    for (int i = 0; i < buf.length; i++)
      buffer_[i] = buf[i];
  }

  public void writeExternal(ObjectOutput out) throws IOException {
//    System.out.println("-->Packet--> writeExternal(ObjectOutput out)");
    out.writeInt(buffer_.length);
    out.write(buffer_);
    out.writeLong(size_);
    out.writeInt(type_);
    out.writeLong(offset_);
    
    out.writeInt(identifier.getBytes().length);
    out.write(identifier.getBytes());
    
    out.writeInt(fixupStream.getItemSateId());
    out.writeInt(fixupStream.getValueDataId());
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//    System.out.println("-->Packet--> readExternal(ObjectInput in)");
    int bufSize = in.readInt();
    buffer_ = new byte[bufSize];
    for (int i = 0; i < bufSize; i++)
      buffer_[i] = in.readByte();

    size_ = in.readLong();
    type_ = in.readInt();
    offset_ = in.readLong();
    
    byte[] buf = new byte[in.readInt()];
    in.read(buf);
    identifier = new String(buf/*, "UTF-8"*/);
    
    int item = in.readInt();
    int value = in.readInt();
    fixupStream = new FixupStream(item, value);
  }
  
  public String getIdentifier() {
    return identifier;
  }

  public byte[] getByteArray() {
    return buffer_;
  }

  public long getSize() {
    return size_;
  }

  public int getPacketType() {
    return type_;
  }

  public long getOffset() {
    return offset_;
  }

  public void setOffset(long offset) {
    offset_ = offset;
  }
  
  public FixupStream getFixupStream(){
    return fixupStream;
  }
  
  public void setFixupStream(FixupStream fs){
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

}
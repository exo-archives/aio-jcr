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
package org.exoplatform.services.jcr.ext.replication.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class helps read AsyncPackets from byte array and write it in byte array.
 * 
 * Created by The eXo Platform SAS Author : Karpenko Sergiy karpenko.sergiy@gmail.com
 */
public class PacketTransformer {

  /**
   * Returns byte array representation of AsyncPacket.
   * 
   * @param packet
   *          Packet object
   * @return byte[] the binary value
   * @throws IOException
   *           generate the IOExaption
   */
  public static byte[] getAsByteArray(AbstractPacket packet) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(packet);

    byte[] bArray = os.toByteArray();
    return bArray;
  }

  /**
   * Returns AsyncPacket read from byte array.
   * 
   * @param byteArray
   *          binary data
   * @return Packet the Packet object from bytes
   * @throws IOException
   *           generate the IOExeption
   * @throws ClassNotFoundException
   *           generate the ClassNotFoundException
   */
  public static AbstractPacket getAsPacket(byte[] byteArray) throws IOException,
                                                            ClassNotFoundException {
    ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
    ObjectInputStream ois = new ObjectInputStream(is);
    AbstractPacket objRead = (AbstractPacket) ois.readObject();

    return objRead;
  }
}

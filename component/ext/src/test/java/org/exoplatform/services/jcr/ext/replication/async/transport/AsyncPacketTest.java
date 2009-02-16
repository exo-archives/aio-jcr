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

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.12.2008
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: AsyncPacketTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncPacketTest extends TestCase {

  public void testAsyncPacket() throws Exception {
    //create content
    /*
    
    byte[] buf = new byte[AbstractPacket.MAX_PACKET_SIZE];
    for (int i = 0; i < buf.length; i++) 
      buf[i] = (byte)(Math.random()*255);
    
    AbstractPacket srcPacket = new AbstractPacket(AsyncPacketTypes.GET_EXPORT_CHAHGESLOG,
                                            120214245,
                                            "8bec9d407f00010101bb60adbcdef058",
                                            Calendar.getInstance().getTimeInMillis(),
                                            100,
                                            buf,
                                            245852);
    //1.
    //serialize srcPacket
    byte[] serializabelSrcPacket = PacketTransformer.getAsByteArray(srcPacket);
    
    //deserialize serializabelSrcPacket
    AbstractPacket destPacket = PacketTransformer.getAsPacket(serializabelSrcPacket);
    
    //compare
    compareAsyncPacket(srcPacket, destPacket);
    
    //2.
    //writeExternal
    File serializabeSrcFile = File.createTempFile("as_file", "tmp");
    serializabeSrcFile.deleteOnExit();
    
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(serializabeSrcFile));
    
    srcPacket.writeExternal(objectOutputStream);
    objectOutputStream.flush();
    objectOutputStream.close();
    
    //readExternal
    AbstractPacket destAsyncPacketFromFile = new AbstractPacket();
    
    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializabeSrcFile));
    
    destAsyncPacketFromFile.readExternal(objectInputStream);
    
    //compare
    compareAsyncPacket(srcPacket, destAsyncPacketFromFile);
    */
  }
  
  /*private void compareAsyncPacket(AbstractPacket srcPacket, AbstractPacket destPacket) {
    assertEquals(destPacket.getOffset(), srcPacket.getOffset());
    assertEquals(destPacket.getOffset(), 245852);
    
    assertEquals(destPacket.getSize(), srcPacket.getSize());
    assertEquals(destPacket.getSize(), 120214245);
    
    assertEquals(destPacket.getBuffer().length, srcPacket.getBuffer().length);
    assertEquals(destPacket.getBuffer().length, AbstractPacket.MAX_PACKET_SIZE);
    
    for (int i = 0; i < srcPacket.getBuffer().length; i++) 
      assertEquals(destPacket.getBuffer()[i], srcPacket.getBuffer()[i]);
    
    assertEquals(destPacket.getCRC(), srcPacket.getCRC());
    assertEquals(destPacket.getCRC(), "8bec9d407f00010101bb60adbcdef058");
    
    assertEquals(destPacket.getTimeStamp(), srcPacket.getTimeStamp());
    
    assertEquals(destPacket.getType(), srcPacket.getType());
    assertEquals(destPacket.getType(), AsyncPacketTypes.GET_EXPORT_CHAHGESLOG);
  }*/
}

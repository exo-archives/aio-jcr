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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.ext.replication.async.AsyncPacket;
import org.exoplatform.services.jcr.ext.replication.async.AsyncPacketTypes;

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
    AsyncPacket srcPacket = new AsyncPacket();
    
    byte[] buf = new byte[AsyncPacket.MAX_PACKET_SIZE];
    for (int i = 0; i < buf.length; i++) 
      buf[i] = (byte)(Math.random()*255);
    
    srcPacket.setBuffer(buf);
    srcPacket.setFileName("exo.jcr.component.ext_1.11-SNAPSHOT");
    
    List<String> fileNameList = new ArrayList<String>();
    for (int i = 0; i < 6750; i++) 
      fileNameList.add("/home/rainf0x/java/exo-dependencies/repository/org/exoplatform/jcr/exo.jcr.component.ext/1.11-SNAPSHOT/exo.jcr.component.ext-1.11-SNAPSHOT-sources.jar");
    
    srcPacket.setFileNameList(fileNameList);
    srcPacket.setIdentifier("8bec9d407f00010101bb60adbcdef058");
    srcPacket.setOffset(245852);
    srcPacket.setOwnName("node_name_1");
    srcPacket.setSize(120214245);
    srcPacket.setSystemId("8bf2ef6a7f0001010014a616966a30a4");
    srcPacket.setTimeStamp(Calendar.getInstance());
    srcPacket.setType(AsyncPacketTypes.GET_CHANGESLOG_UP_TO_DATE);
    
    //1.
    //serialize srcPacket
    byte[] serializabelSrcPacket = AsyncPacket.getAsByteArray(srcPacket);
    
    //deserialize serializabelSrcPacket
    AsyncPacket destPacket = AsyncPacket.getAsPacket(serializabelSrcPacket);
    
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
    AsyncPacket destAsyncPacketFromFile = new AsyncPacket();
    
    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializabeSrcFile));
    
    destAsyncPacketFromFile.readExternal(objectInputStream);
    
    //compare
    compareAsyncPacket(srcPacket, destAsyncPacketFromFile);
  }
  
  private void compareAsyncPacket(AsyncPacket srcPacket, AsyncPacket destPacket) {
    assertEquals(destPacket.getOffset(), srcPacket.getOffset());
    assertEquals(destPacket.getOffset(), 245852);
    
    assertEquals(destPacket.getSize(), srcPacket.getSize());
    assertEquals(destPacket.getSize(), 120214245);
    
    assertEquals(destPacket.getBuffer().length, srcPacket.getBuffer().length);
    assertEquals(destPacket.getBuffer(), AsyncPacket.MAX_PACKET_SIZE);
    
    for (int i = 0; i < srcPacket.getBuffer().length; i++) 
      assertEquals(destPacket.getBuffer()[i], srcPacket.getBuffer()[i]);
    
    assertEquals(destPacket.getFileName(), srcPacket.getFileName());
    assertEquals(destPacket.getFileName(), "exo.jcr.component.ext_1.11-SNAPSHOT");
    
    assertEquals(destPacket.getFileNameList().size(), srcPacket.getFileNameList().size());
    assertEquals(destPacket.getFileNameList().size(), 6750);
    
    for (int i = 0; i < srcPacket.getFileNameList().size(); i++) { 
      assertEquals(destPacket.getFileNameList().get(i), srcPacket.getFileNameList().get(i));
      assertEquals(destPacket.getFileNameList().get(i), "/home/rainf0x/java/exo-dependencies/repository/org/exoplatform/jcr/exo.jcr.component.ext/1.11-SNAPSHOT/exo.jcr.component.ext-1.11-SNAPSHOT-sources.jar");
    }
    
    assertEquals(destPacket.getIdentifier(), srcPacket.getIdentifier());
    assertEquals(destPacket.getIdentifier(), "8bec9d407f00010101bb60adbcdef058");
    
    assertEquals(destPacket.getOwnName(), srcPacket.getOwnName());
    assertEquals(destPacket.getOwnName(), "node_name_1");
    
    assertEquals(destPacket.getSystemId(), srcPacket.getSystemId());
    assertEquals(destPacket.getSystemId(), "8bf2ef6a7f0001010014a616966a30a4");
    
    assertEquals(destPacket.getTimeStamp().getTimeInMillis(), srcPacket.getTimeStamp().getTimeInMillis());
    
    assertEquals(destPacket.getType(), srcPacket.getType());
    assertEquals(destPacket.getType(), AsyncPacketTypes.GET_CHANGESLOG_UP_TO_DATE);
  }
}

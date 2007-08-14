/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.storage.value.fs.FileIOChannel;
import org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileIOChannel;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: TestFileIOChannel.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestFileIOChannel extends TestCase {
  
  private FileCleaner cleaner = new FileCleaner(1000);
  
  public void testReadFromIOChannel() throws Exception {
    
    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testReadFromIOChannel0");
    file.deleteOnExit();
    if(file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();
    
    buf = "01234567890123456789".getBytes();
    
    file = new File("target/testReadFromIOChannel1");
    if(file.exists())
      file.delete();
    out = new FileOutputStream(file);
    out.write(buf);
    out.close();

    
    
    File root = new File("target");
    FileIOChannel channel = new SimpleFileIOChannel(root, cleaner);
    if(!root.exists())
      throw new Exception("Folder does not exist "+root.getAbsolutePath());
    
    // first value - to buffer (length`=10), second - file (length`=20)
    //List <ValueData> values = channel.read("testReadFromIOChannel", 11);
    
    //assertEquals(2, values.size());
    ValueData v0 =  channel.read("testReadFromIOChannel",0, 11);
    assertEquals(10, v0.getLength());
    assertEquals(0, v0.getOrderNumber());
    assertEquals(10, v0.getAsByteArray().length);
    assertTrue(v0.isByteArray());
    assertNotNull(v0.getAsStream());
    

    ValueData v1 = channel.read("testReadFromIOChannel",1, 11);
    assertEquals(20, v1.getLength());
    assertEquals(1, v1.getOrderNumber());
    assertFalse(v1.isByteArray());
    assertNotNull(v1.getAsStream());
    
    try {
      v1.getAsByteArray();
      fail("IllegalStateException should have been thrown");
    } catch (IllegalStateException e) {
    }
    channel.delete("testReadFromIOChannel");
  }

  public void testWriteToIOChannel() throws Exception {
    File root = new File("target");
    
    FileIOChannel channel = new SimpleFileIOChannel(root, cleaner);
    if(!root.exists())
      throw new Exception("Folder does not exist "+root.getAbsolutePath());
    
    byte[] buf = "0123456789".getBytes();
    List <ValueData> values = new ArrayList <ValueData> ();
    values.add(new  ByteArrayPersistedValueData(buf, 0));
    values.add(new  ByteArrayPersistedValueData(buf, 1));
    values.add(new  ByteArrayPersistedValueData(buf, 2));
    
    for (ValueData valueData : values) {
      channel.write("testWriteToIOChannel", valueData);
    }
    
    
    assertTrue( new File("target/testWriteToIOChannel0").exists());
    assertTrue( new File("target/testWriteToIOChannel1").exists());
    assertTrue( new File("target/testWriteToIOChannel2").exists());
    
    assertEquals(10, new File("target/testWriteToIOChannel0").length());
    
    
    channel.delete("testWriteToIOChannel");
    // try to read
//    values = channel.read("testWriteToIOChannel", 5);
//    assertEquals(3, values.size());
  }
  
  public void testDeleteFromIOChannel() throws Exception {
    File root = new File("target");
    FileIOChannel channel = new SimpleFileIOChannel(root, cleaner);
    if(!root.exists())
      throw new Exception("Folder does not exist "+root.getAbsolutePath());
    
    byte[] buf = "0123456789".getBytes();
    List <ValueData> values = new ArrayList <ValueData> ();
    values.add(new  ByteArrayPersistedValueData(buf, 0));
    values.add(new  ByteArrayPersistedValueData(buf, 1));
    values.add(new  ByteArrayPersistedValueData(buf, 2));
    
    for (ValueData valueData : values) {
      channel.write("testDeleteFromIOChannel", valueData);
    }
    
    assertTrue( new File("target/testDeleteFromIOChannel0").exists());
    assertTrue( new File("target/testDeleteFromIOChannel1").exists());
    assertTrue( new File("target/testDeleteFromIOChannel2").exists());
    
    channel.delete("testDeleteFromIOChannel");

    assertFalse( new File("target/testDeleteFromIOChannel0").exists());
    assertFalse( new File("target/testDeleteFromIOChannel1").exists());
    assertFalse( new File("target/testDeleteFromIOChannel2").exists());

    channel.delete("testDeleteFromIOChannel");
    // try to read
//    values = channel.read("testDeleteFromIOChannel", 5);
//    assertEquals(0, values.size());

  }  
  
  public void testConcurrentReadFromIOChannel() throws Exception {
    File root = new File("target");
    FileIOChannel channel = new SimpleFileIOChannel(root, cleaner);
    if(!root.exists())
      throw new Exception("Folder does not exist "+root.getAbsolutePath());
    
    List <ValueData> values = new ArrayList <ValueData> ();
    byte[] buf = new byte[100*100];
    for(int i=0; i<buf.length; i++) {
      buf[i] = 48;
    }
    
    // approx. 10Kb file
    values.add(new  ByteArrayPersistedValueData(buf, 0));    
    for (ValueData valueData : values) {
      channel.write("testConcurrentReadFromIOChannel", valueData);
    }
    

    File f = new File("target/testConcurrentReadFromIOChannel0");
    if(!f.exists())
      throw new Exception("File does not exist "+f.getAbsolutePath());

    Probe[] p = new Probe[10];
    for(int i=0; i<10; i++) {
      p[i] = new Probe(f);
      p[i].start();
    }
    
//    // should be enough to start read but not finish all the threads
//    Thread.sleep(100);
//    channel.delete("testConcurrentReadFromIOChannel");
    
    // should be enough to finish all the threads
    Thread.sleep(1000);
    
    for(int i=0; i<10; i++) {
      assertEquals(100*100, p[i].getLen());
    }
    channel.delete("testConcurrentReadFromIOChannel");
  }  

  public void testDeleteLockedFileFromIOChannel() throws Exception {
    File root = new File("target");
    FileIOChannel channel = new SimpleFileIOChannel(root, cleaner);
    if(!root.exists())
      throw new Exception("Folder does not exist "+root.getAbsolutePath());
    
    List <ValueData> values = new ArrayList <ValueData> ();
    byte[] buf = new byte[100000];
    for(int i=0; i<buf.length; i++) {
      buf[i] = 48;
    }
    
    // approx. 1Mb file
    values.add(new  ByteArrayPersistedValueData(buf, 0));    
    for (ValueData valueData : values) {
      channel.write("testDeleteLockedFileFromIOChannel", valueData);
    }
    
    
    File f = new File("target/testDeleteLockedFileFromIOChannel0");
    new Probe(f).start();
    
    f = null;
    
    Thread.sleep(100);
    if(channel.delete("testDeleteLockedFileFromIOChannel"))
      fail("File target/testDeleteLockedFileFromIOChannel0 is deleted! try to increase sleep value below!");
    
    // removed by FileCleaner
    Thread.sleep(3000);
    f = new File("target/testDeleteLockedFileFromIOChannel0");
    //assertFalse(f.exists());
    System.out.println(">>>>>>>>>>>>>"+f.canRead()+" "+f.exists()+" "+f.canWrite());
    System.out.println(">>>>>>>>>>>>>"+new FileInputStream(f).read());
    
//    new Probe(f).start();
    
  }
}

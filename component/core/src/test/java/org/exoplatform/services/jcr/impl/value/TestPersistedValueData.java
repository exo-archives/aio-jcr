/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.value;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.FileStreamPersistedValueData;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: TestPersistedValueData.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestPersistedValueData extends TestCase {
  
  public void testCreateByteArrayValueData() throws Exception {
    byte[] buf = "0123456789".getBytes();
    ByteArrayPersistedValueData vd = new  ByteArrayPersistedValueData(buf, 0);
    assertTrue(vd.isByteArray());
    assertEquals(10, vd.getLength());
    assertEquals(0, vd.getOrderNumber());
    assertEquals(10, vd.getAsByteArray().length);
    assertTrue(vd.getAsStream() instanceof ByteArrayInputStream);
  }

  public void testCreateFileStreamValueData() throws Exception {
    
    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testCreateFileStreamValueData");
    if(file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();
    
    FileStreamPersistedValueData vd = new  FileStreamPersistedValueData(file, 0, false);
    assertFalse(vd.isByteArray());
    assertEquals(10, vd.getLength());
    assertEquals(0, vd.getOrderNumber());
    try {
      vd.getAsByteArray();
      fail("IllegalStateException should have been thrown!");
    } catch (IllegalStateException e) {
    }
    assertTrue(vd.getAsStream() instanceof FileInputStream);
  }

  public void testIfFinalizeRemovesTempFileStreamValueData() throws Exception {
    
    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testIfFinalizeRemovesTempFileStreamValueData");
    if(file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();
    
    FileStreamPersistedValueData vd = new  FileStreamPersistedValueData(file, 0, true);
    assertTrue(file.exists());
    
    vd = null;
    System.gc();
    
    // allows GC to call finalize on vd
    Thread.sleep(1000);
    
    assertFalse(file.exists());
  }

  public void testConcurrentFileStreamValueDataReading() throws Exception {
    
    byte[] buf = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes();
    File file = new File("target/testConcurrentFileStreamValueDataReading");
    if(file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    // approx. 10Kb file
    for(int i=0; i<100; i++) {
      out.write(buf);
    }
    out.close();
    
    Probe[] p = new Probe[10];
    for(int i=0; i<10; i++) {
      p[i] = new Probe(file);
      p[i].start();
    }
    
    // should be enough to finish all the threads
    Thread.sleep(1000);
    
    for(int i=0; i<10; i++) {
      assertEquals(100*100, p[i].getLen());
    }
  }

}

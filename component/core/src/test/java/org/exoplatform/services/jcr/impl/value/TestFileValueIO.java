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

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.FileStreamPersistedValueData;
import org.exoplatform.services.jcr.impl.util.io.FileValueIOUtil;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: TestFileValueIO.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestFileValueIO extends TestCase {
  

  public void testReadByteArrayValueData() throws Exception {
    
    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testReadByteArrayValueData");
    if(file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();
    
    // max buffer size = 50 - so ByteArray will be created
    ValueData vd = FileValueIOUtil.readValue(file, 0, 50, false);
    
    assertTrue(vd instanceof ByteArrayPersistedValueData);
    assertTrue(vd.isByteArray());
    assertEquals(10, vd.getLength());
    assertEquals(0, vd.getOrderNumber());
    assertEquals(10, vd.getAsByteArray().length);
    assertTrue(vd.getAsStream() instanceof ByteArrayInputStream);
  }
  
  public void testReadFileValueData() throws Exception {
    
    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testReadFileValueData");
    if(file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();
    
    // max buffer size = 5 - so File will be created
    ValueData vd = FileValueIOUtil.readValue(file, 0, 5, false);
    
    assertTrue(vd instanceof FileStreamPersistedValueData);
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
  
  public void testWriteFileValueData() throws Exception {
    
    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testWriteFileValueData");
    if(file.exists())
      file.delete();

    ByteArrayPersistedValueData vd = new  ByteArrayPersistedValueData(buf, 0);
  
    FileValueIOUtil.writeValue(file, vd);
    
    // max buffer size = 5 - so File will be created
    ValueData vd1 = FileValueIOUtil.readValue(file, 0, 5, false);
    
    assertFalse(vd1.isByteArray());
    assertEquals(10, vd1.getLength());
    assertEquals(0, vd1.getOrderNumber());
    assertTrue(vd1.getAsStream() instanceof FileInputStream);
  }


/*
  private class Probe extends Thread {
    private File file;
    private int len = 0;
    
    public Probe(File file) {
      super();
      this.file = file;
    }

    public void run() {
      System.out.println("Thread started "+this.getName());
      try {
        FileInputStream is = new FileInputStream(file);
        while(is.read()>0) {
          len++;
        }
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println("Thread finished "+this.getName()+" read: "+len);
    }

    public int getLen() {
      return len;
    }
  }
*/  
}

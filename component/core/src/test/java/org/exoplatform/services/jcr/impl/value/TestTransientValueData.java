/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.value;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.Uuid;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: TestTransientValueData.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestTransientValueData extends TestCase {
  
  public void testCreateByteArrayTransientValueData() throws Exception {
    
//    byte[] buf = "0123456789".getBytes();
    TransientValueData vd = new  TransientValueData("0123456789");
    // normal
    assertTrue(vd.isByteArray());
    assertEquals(10, vd.getLength());
    assertEquals(0, vd.getOrderNumber());
    assertEquals(10, vd.getAsByteArray().length);
    
    // as stream
    assertTrue(vd.getAsStream() instanceof ByteArrayInputStream);
    assertTrue(vd.isByteArray());

    // no spool file as spooled to byte array
    assertNull(vd.getSpoolFile());
    
  }

  public void testCreateFileStreamTransientValueData() throws Exception {
    
    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testCreateFileStreamTransientValueData");
    if(file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();
    
    FileInputStream fs1 = new FileInputStream(file);
    TransientValueData vd = new  TransientValueData(fs1);

    // spool to file
    vd.setMaxBufferSize(5);
    vd.setFileCleaner(new FileCleaner());
    InputStream fs2 = vd.getAsStream();
    assertEquals(10, vd.getLength());
    assertTrue(fs2 instanceof FileInputStream);
    
    // not the same object as new is is from spool file 
    assertNotSame(fs1, fs2);
    // spooled to file so not a byte array
    assertFalse(vd.isByteArray());

    // next call return not the same object as well 
    // (new stream every time)
    assertNotSame(vd.getAsStream(), fs2);
    assertEquals(10, vd.getLength());
    
    // gets as byte array
    assertEquals(10, vd.getAsByteArray().length);
    // but still spooled to file
    assertFalse(vd.isByteArray());
    
  }
  
  public void testIfTransientValueDataReturnsNewData () throws Exception {
    //byte[] buf = "0123456789".getBytes();
    TransientValueData vd = new  TransientValueData("0123456789");
    assertNotSame(vd.getAsByteArray(), vd.getAsByteArray());
    assertNotSame(vd.getAsStream(), vd.getAsStream());
  }
  
  public void testCreateTransientValueDataFromByteArray() throws Exception {
   // byte[] buf = "0123456789".getBytes();
    TransientValueData vd = new  TransientValueData("0123456789");
    
    //// not influenced here as will be spooled to byte array anyway 
    vd.setMaxBufferSize(5);
    vd.setFileCleaner(new FileCleaner());
    ////
    InputStream fs2 = vd.getAsStream();
    assertEquals(10, vd.getLength());
    assertTrue(fs2 instanceof ByteArrayInputStream);
  }
  
  public void testNewStringValueData() throws Exception {
    TransientValueData vd = new  TransientValueData("string");
    assertEquals(6, vd.getLength());
    assertEquals("string", new String(vd.getAsByteArray()));
    
    // default encoded string (utf-8)
    vd = new  TransientValueData("гена");
    assertEquals(8, vd.getLength());

  }
  
  
  public void testNewBooleanValueData() throws Exception {
    TransientValueData vd = new  TransientValueData(true);
    assertEquals("true", new String(vd.getAsByteArray()));
  }  

  public void testNewDateValueData() throws Exception {
    
    Calendar cal = Calendar.getInstance();
    long time = cal.getTimeInMillis();
    //String str = ISO8601.format(cal);
    TransientValueData vd = new  TransientValueData(cal);
    /// ????????????????
    //assertEquals(time, ISO8601.parse(new String(vd.getAsByteArray())).getTimeInMillis());
    fail("How to compare Date Value ?????");
    
  }  

  public void testNewDoubleValueData() throws Exception {
    TransientValueData vd = new  TransientValueData(3.14);
    assertEquals("3.14", new String(vd.getAsByteArray()));
  }  

  public void testNewLongValueData() throws Exception {
    TransientValueData vd = new  TransientValueData(314);
    assertEquals("314", new String(vd.getAsByteArray()));
  }  

  public void testNewPathValueData() throws Exception {
    QPath path = QPath.parse("[]:1[]test:1");
    TransientValueData vd = new  TransientValueData(path);
    assertEquals(path, QPath.parse(new String(vd.getAsByteArray())));
  }
  
  public void testNewNameValueData() throws Exception {
    InternalQName name = InternalQName.parse("[]test");
    TransientValueData vd = new  TransientValueData(name);
    assertEquals(name, InternalQName.parse(new String(vd.getAsByteArray())));
  }  

  public void testNewUuidValueData() throws Exception {
    
    TransientValueData vd = new  TransientValueData(new Uuid("1234"));
    assertEquals("1234", new String(vd.getAsByteArray()));
  }  


}

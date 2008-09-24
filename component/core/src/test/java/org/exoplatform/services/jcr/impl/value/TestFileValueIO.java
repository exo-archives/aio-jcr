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
package org.exoplatform.services.jcr.impl.value;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.FileStreamPersistedValueData;
import org.exoplatform.services.jcr.impl.storage.value.fs.FileIOChannel;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: TestFileValueIO.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class TestFileValueIO extends TestCase {

  static class FileValueIOUtil extends FileIOChannel {

    FileValueIOUtil() {
      super(null, null, "Test #1");
    }

    @Override
    protected String makeFilePath(String propertyId, int orderNumber) {
      return null;
    }

    @Override
    protected File getFile(String propertyId, int orderNumber) {
      return null;
    }

    @Override
    protected File[] getFiles(String propertyId) {
      return null;
    }

    static public ValueData testReadValue(File file, int orderNum, int maxBufferSize, boolean temp) throws IOException {

      return new FileValueIOUtil().readValue(file, orderNum, maxBufferSize, temp);
    }

    static public void testWriteValue(File file, ValueData value) throws IOException {
      new FileValueIOUtil().writeValue(file, value);
    }
  }

  public void testReadByteArrayValueData() throws Exception {

    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testReadByteArrayValueData");
    if (file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();

    // max buffer size = 50 - so ByteArray will be created
    ValueData vd = FileValueIOUtil.testReadValue(file, 0, 50, false);

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
    if (file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();

    // max buffer size = 5 - so File will be created
    ValueData vd = FileValueIOUtil.testReadValue(file, 0, 5, false);

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
    if (file.exists())
      file.delete();

    ByteArrayPersistedValueData vd = new ByteArrayPersistedValueData(buf, 0);

    FileValueIOUtil.testWriteValue(file, vd);

    // max buffer size = 5 - so File will be created
    ValueData vd1 = FileValueIOUtil.testReadValue(file, 0, 5, false);

    assertFalse(vd1.isByteArray());
    assertEquals(10, vd1.getLength());
    assertEquals(0, vd1.getOrderNumber());
    assertTrue(vd1.getAsStream() instanceof FileInputStream);
  }

  /*
   * private class Probe extends Thread { private File file; private int len = 0; public Probe(File
   * file) { super(); this.file = file; } public void run() {
   * System.out.println("Thread started "+this.getName()); try { FileInputStream is = new
   * FileInputStream(file); while(is.read()>0) { len++; } } catch (Exception e) {
   * e.printStackTrace(); } System.out.println("Thread finished "+this.getName()+" read: "+len); }
   * public int getLen() { return len; } }
   */
}

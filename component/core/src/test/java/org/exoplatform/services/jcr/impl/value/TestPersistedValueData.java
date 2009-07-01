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

import junit.framework.TestCase;

import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CleanableFileStreamValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.FileStreamPersistedValueData;
import org.exoplatform.services.jcr.impl.storage.value.fs.Probe;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: TestPersistedValueData.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class TestPersistedValueData extends TestCase {

  public void testCreateByteArrayValueData() throws Exception {
    byte[] buf = "0123456789".getBytes();
    ByteArrayPersistedValueData vd = new ByteArrayPersistedValueData(buf, 0);
    assertTrue(vd.isByteArray());
    assertEquals(10, vd.getLength());
    assertEquals(0, vd.getOrderNumber());
    assertEquals(10, vd.getAsByteArray().length);
    assertTrue(vd.getAsStream() instanceof ByteArrayInputStream);
  }

  public void testCreateFileStreamValueData() throws Exception {

    byte[] buf = "0123456789".getBytes();
    File file = new File("target/testCreateFileStreamValueData");
    if (file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();

    FileStreamPersistedValueData vd = new FileStreamPersistedValueData(file, 0);
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
    SwapFile file = SwapFile.get(new File("target"), "testIfFinalizeRemovesTempFileStreamValueData");
    //File file = new File("target/testIfFinalizeRemovesTempFileStreamValueData");
    //if (file.exists())
    //  file.delete();
    FileOutputStream out = new FileOutputStream(file);
    out.write(buf);
    out.close();

    CleanableFileStreamValueData vd = new CleanableFileStreamValueData(file, 0, new FileCleaner(1000, true));
    assertTrue(file.exists());

    vd = null;
    System.gc();

    // allows GC to call finalize on vd
    Thread.sleep(2500);
    System.gc();

    assertFalse(file.exists());
  }

  public void testConcurrentFileStreamValueDataReading() throws Exception {

    byte[] buf = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes();
    File file = new File("target/testConcurrentFileStreamValueDataReading");
    if (file.exists())
      file.delete();
    FileOutputStream out = new FileOutputStream(file);
    // approx. 10Kb file
    for (int i = 0; i < 100; i++) {
      out.write(buf);
    }
    out.close();

    Probe[] p = new Probe[10];
    for (int i = 0; i < 10; i++) {
      p[i] = new Probe(file);
      p[i].start();
    }

    // should be enough to finish all the threads
    Thread.sleep(4000);

    for (int i = 0; i < 10; i++) {
      assertEquals(100 * 100, p[i].getLen());
    }
  }

}

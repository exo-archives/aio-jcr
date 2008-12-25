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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Random;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TestChangesFile.java 111 2008-11-11 11:11:11Z serg $
 */
public class TestChangesFile extends BaseStandaloneTest {

  private static final String CRC = "CRC";

  public void testWriteFile() throws Exception {
    ChangesFile file = new ChangesFile(CRC, System.currentTimeMillis());

    int size1 = 316;
    int size2 = 45;
    int size3 = 126;

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    byte[] buf2 = createBLOBTempData(size2);
    file.writeData(buf2, size1);
    System.arraycopy(buf2, 0, bufetalon, size1, size2);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);
    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getDataStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }

  public void testRandomWriteFile() throws Exception {
    ChangesFile file = new ChangesFile(CRC, System.currentTimeMillis());

    int size1 = 10;
    int size2 = 5;
    int size3 = 10;

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);

    byte[] buf2 = createBLOBTempData(size2);
    file.writeData(buf2, size1);
    System.arraycopy(buf2, 0, bufetalon, size1, size2);

    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getDataStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }

  public void testCorruptedWriteFile() throws Exception {
    ChangesFile file = new ChangesFile(CRC, System.currentTimeMillis());

    int size1 = 316;
    int size2 = 45;
    int size3 = 126;

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    // byte[] buf2 = createBLOBTempData(size2);
    // file.writeData(buf2, size1);
    // System.arraycopy(buf2, 0, bufetalon,size1,size2);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);
    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getDataStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }

  public void testWriteAlreadyOpenedFile() throws Exception {
    int size1 = 316;
    int size2 = 45;
    int size3 = 126;

    File f = File.createTempFile("TestChangesFile", "suf");

    RandomAccessFile ac = new RandomAccessFile(f, "rw");

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf2 = createBLOBTempData(size2);
    ac.seek(size1);
    ac.write(buf2);
    System.arraycopy(buf2, 0, bufetalon, size1, size2);

    ac.close();

    ChangesFile file = new ChangesFile(f, CRC, System.currentTimeMillis());

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);
    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getDataStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }

  protected byte[] createBLOBTempData(int size) throws IOException {
    byte[] data = new byte[size]; // 1Kb
    Random random = new Random();
    random.nextBytes(data);
    return data;
  }

}

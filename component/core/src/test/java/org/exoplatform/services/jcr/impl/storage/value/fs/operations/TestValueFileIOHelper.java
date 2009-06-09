/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.storage.value.fs.operations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.impl.storage.value.fs.FileIOChannel;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 28.05.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestValueFileIOHelper.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class TestValueFileIOHelper extends JcrImplBaseTest {

  private ValueFileIOHelper io;

  private File              testDir;

  private File              dest;

  private static File       src;

  public void setUp() throws Exception {
    super.setUp();

    io = new ValueFileIOHelper();

    if (src == null || !src.exists()) {
      src = createBLOBTempFile(7 * 1024); // 7M
      src.deleteOnExit();
    }

    testDir = new File("target/TestValueFileIOHelper");
    testDir.mkdirs();

    dest = File.createTempFile("vdftest", "", testDir);
  }

  public void tearDown() throws Exception {
    dest.delete();

    super.tearDown();
  }

  public void testCopyFileToFile() throws Exception {

    io.copyClose(new FileInputStream(src), new FileOutputStream(dest));

    // check length
    assertEquals(src.length(), dest.length());

    // check content
//    InputStream srcin = new FileInputStream(src);
//    InputStream destin = new FileInputStream(dest);
//    try {
//      compareStream(srcin, destin);
//    } finally {
//      srcin.close();
//      destin.close();
//    }
  }

  public void testCopyBytesToFile() throws Exception {

    // copy via InputStream
    long start = System.currentTimeMillis();

    InputStream in = new FileInputStream(src);
    //InputStream in = new URL("http://jboss1.exoua-int:8089/browser/02.zip").openStream();
    OutputStream out = new FileOutputStream(dest);
    try {
      int r = 0;
      byte[] buff = new byte[ValueFileIOHelper.IOBUFFER_SIZE];
      while ((r = in.read(buff)) >= 0) {
        out.write(buff, 0, r);
      }
      out.flush();
    } finally {
      in.close();
      out.close();
    }
    // print time
    System.out.println("=== IO time  " + (System.currentTimeMillis() - start));

    // clean and recreate file
    dest.delete();
    dest = File.createTempFile("vdftest", "", testDir);

    // copy via NIO
    start = System.currentTimeMillis();
    io.copyClose(new BufferedInputStream(new FileInputStream(src)), new FileOutputStream(dest));
    //io.copyClose(new URL("http://jboss1.exoua-int:8089/browser/02.zip").openStream(), new FileOutputStream(dest));
    System.out.println("=== NIO time " + (System.currentTimeMillis() - start));

    // check length
    assertEquals(src.length(), dest.length());

    // check content
//    InputStream srcin = new FileInputStream(src);
//    InputStream destin = new FileInputStream(dest);
//    try {
//      compareStream(srcin, destin);
//    } finally {
//      srcin.close();
//      destin.close();
//    }
  }
}

/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Date;

import junit.framework.TestCase;

import org.exoplatform.maven2.plugin.Utils;

/**
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Jan 3, 2006
 */
public class TestUtils extends TestCase {
  public TestUtils(String testname) {
    super(testname);
  }

  public void testCopyDirectoryStructure() throws IOException {
    String basedir = System.getProperty("basedir");
    int counter = 0;
    File srcDir = new File(basedir + "/src/test");
    File dstFile = new File(basedir + "/target/destinationDir");
    counter = Utils.copyDirectoryStructure(srcDir, dstFile, Utils.getDefaultIgnoreFiles(), true);
    //assertEquals("Check number of file copied ", counter, 10);

    // Modify the files in source directory
    File in = new File(srcDir + "/web.xml");
    FileInputStream fis = new FileInputStream(in); 
    FileChannel channel = fis.getChannel();
    java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate((int)channel.size());    
    channel.read(buffer);
    buffer.rewind();
    channel.close();
    fis.close();
    File out = new File(dstFile + "/web.xml");
    FileOutputStream fos = new FileOutputStream(out); 
    FileChannel channel2 = fos.getChannel();
    channel2.write( buffer);
    buffer.clear();
    channel2.close();
    fos.close();
    counter = Utils.copyDirectoryStructure(srcDir, dstFile, Utils.getDefaultIgnoreFiles(), true);
    assertEquals(" Modify the files in source directory .", counter, 1);
  }
}

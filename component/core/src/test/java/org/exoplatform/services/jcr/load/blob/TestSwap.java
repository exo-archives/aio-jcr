/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.blob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.load.blob.thread.CreateThread;
import org.exoplatform.services.jcr.load.blob.thread.DeleteThread;
import org.exoplatform.services.jcr.load.blob.thread.ReadThread;


/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 19.10.2006
 *
 * Subjetc of the test it's to test BLOB data storing in eXo JCR 
 * with/without swap/binary.temp storages in concurent environment.
 * 
 * Also can be used for test eXo JCR without values storage. 
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestSwap.java 12535 2007-02-02 15:39:26Z peterit $
 */
public class TestSwap extends JcrAPIBaseTest {

  private Node          testBinaryValue   = null;

  //public final static String LOCAL_BIG_FILE    = "src/test/resources/BigFile.zip";
  public final static String URL_BIG_MEDIA_FILE= "ftp://exoua.dnsalias.net/pub/video/Lost.Season3.Preview.rus.avi";
  //public final static String LOCAL_SMALL_FILE  = "src/test/resources/SmallFile.zip";
  //public final static byte[] LOCAL_SMALL_FILE_DATA  = LOCAL_SMALL_FILE.getBytes();
  public final static String TEST_ROOT  = "blob_test";
  
  public static String TEST_FILE = null;
  
  public static long TEST_FILE_SIZE;
  
  public static Set<String> consumedNodes = Collections.synchronizedSet(new HashSet<String>());
  
  private File testFile = null;
  
  public void setUp() throws Exception {
    super.setUp();
    Session testSession = repository.login(session.getCredentials(), "ws1");
    testBinaryValue = testSession.getRootNode().addNode(TEST_ROOT);
    testSession.save();
    
    int dataSize = 0;
    if (TEST_FILE == null) {
      // create test file
      testFile = createBLOBTempFile(3);
      dataSize = (int) testFile.length();
      TEST_FILE = testFile.getAbsolutePath();
    } else {
      // calc stream size
      byte[] buff = new byte[1024 * 4];
      int read = 0;
      //InputStream dataStream = new URL(TEST_FILE).openStream();
      InputStream dataStream = new FileInputStream(TEST_FILE);
      while ((read = dataStream.read(buff))>=0) {
        dataSize += read;
      }
      dataStream.close();
    }
    TEST_FILE_SIZE = dataSize;
  }  
  
  @Override
  protected void tearDown() throws Exception {
    
    try {
      if (testFile != null)
        testFile.delete();
    } catch(Throwable e) {
      log.error("Temp test file error of delete: " + e.getMessage(), e);
    } finally {
      testBinaryValue.remove();
      testBinaryValue.getSession().save();
      super.tearDown();
    }
  }

  public void testSmallValue() throws Exception {
    // creators
    CreateThread creator = new CreateThread(repository.login(session.getCredentials(), "ws1"));
    creator.start();
    try {
      log.info("Wait 20 sec. for CreateThread");
      Thread.sleep(20000); // 30 sec.
    } catch(InterruptedException e) {
      log.error("Creator wait. Sleep error: " + e.getMessage(), e);
    }
    
    List<ReadThread> readers = new ArrayList<ReadThread>();
    
    log.info("Begin readers...");
    for (int i=0; i<5; i++) {
      ReadThread readed = new ReadThread(repository.login(session.getCredentials(), "ws1"));
      readed.start();
      readers.add(readed);
      try {
        Thread.sleep(1000);
      } catch(InterruptedException e) {
        log.error("Start reader. Sleep error: " + e.getMessage(), e);
      }
    }
    
    log.info("Begin cleaner...");
    DeleteThread cleaner = new DeleteThread(repository.login(session.getCredentials(), "ws1"));
    cleaner.start();
    
    // 360 - 60 min
    // 4320 - 12 hours
    int cycles = 1;
    while (cycles >= 0) {
      Thread.yield();
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        log.error("Test lifecycle. Sleep error: " + e.getMessage(), e);
      }
      log.info("<<<<<<<<<<<<<<<<<<<< Cycle " + cycles + " >>>>>>>>>>>>>>>>>>>>>");
      cycles--;
    }

    log.info("<<<<<<<<<<<<<<<<<<<< Stopping >>>>>>>>>>>>>>>>>>>>>");
    
    for (ReadThread reader: readers) {
      try {
        reader.testStop();
        reader.join(3000);
        Thread.yield();
      } catch (InterruptedException e) {
        log.error("Test lifecycle. Readed stop error: " + e.getMessage(), e);
      } 
    }
    
    try {
      creator.testStop();
      creator.join();
      Thread.yield();
    } catch (InterruptedException e) {
      log.error("Test lifecycle. Creator stop error: " + e.getMessage(), e);
    } 
    
    try {
      cleaner.testStop();
      cleaner.join();
      Thread.yield();
    } catch (InterruptedException e) {
      log.error("Test lifecycle. Cleaner stop error: " + e.getMessage(), e);
    } 
    
    log.info("<<<<<<<<<<<<<<<<<<<< Stopped >>>>>>>>>>>>>>>>>>>>>");
    try {
      Thread.sleep(15000);
    } catch (InterruptedException e) {
      log.error("Test stop. Sleep error: " + e.getMessage(), e);
    }
  }
  
}

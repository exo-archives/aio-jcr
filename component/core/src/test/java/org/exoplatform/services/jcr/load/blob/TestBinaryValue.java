/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.load.blob;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SARL.
 * 
 * NOTE: Make sure you have the files pointed below!
 */

public class TestBinaryValue extends JcrAPIBaseTest {
  
  private Node          testBinaryValue   = null;

  private static int    FILES_COUNT       = 1;
  
  //private static String REMOTE_BIG_FILE   = "\\\\Exooffice\\public\\Tmp\\resources\\BigFile.zip";

  //private static String REMOTE_SMALL_FILE = "\\\\Exooffice\\public\\Tmp\\resources\\SmallFile.zip";
  
  //private static String URL_BIG_FILE      = "http://localhost:8080/ecm/jcr?workspace=production&path=/BigFile.zip";
  
  //private static String URL_SMALL_FILE      = "http://localhost:8080/ecm/jcr?workspace=production&path=/SmallFile.zip";
  
  //private static String URL_BIG_FILE      = "http://localhost:8080/ecm/jcr?workspace=production&path=/testBinaryValue/BigFile.zip";
  //private static String URL_BIG_FILE      = "http://exooffice:8080/jcr-webdav/repository/production/Lost.Season3.Preview.rus.avi";
  //private static String URL_BIG_FILE      = "ftp://exoua.dnsalias.net/jcr/test/BigFile.zip";
  //private static String URL_BIG_MEDIA_FILE= "ftp://exoua.dnsalias.net/pub/video/Lost.Season3.Preview.rus.avi";
  //private static String URL_VERYBIG_MEDIA_FILE = "ftp://exoua.dnsalias.net/pub/video/9_11_xchcoin.avi";
  
  //private static String URL_SMALL_FILE    = "http://localhost:8080/ecm/jcr?workspace=production&path=/testBinaryValue/SmallFile.zip";
  //private static String URL_SMALL_FILE    = "http://exooffice:8080/jcr-webdav/repository/production/SmallFile.zip";
  //private static String URL_SMALL_FILE    = "ftp://exoua.dnsalias.net/jcr/test/SmallFile.zip";
  
  // -------------- TEST FILE ------------------
  private static String TEST_FILE    = null; // URL_SMALL_FILE

  public void setUp() throws Exception {
    super.setUp();
    testBinaryValue = root.addNode("testBinaryValue");
    session.save();
    
    if (TEST_FILE == null) {
      // create test file
      TEST_FILE = createBLOBTempFile(3).getAbsolutePath();
    }
  }

  public void testLocalBigFiles() throws Exception {
    Node testLocalBigFiles = testBinaryValue.addNode("testLocalBigFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    
    // 300 MB
    TEST_FILE = createBLOBTempFile(300000).getAbsolutePath();

    for (int i = 0; i < FILES_COUNT; i++) {
      Node localBigFile = testLocalBigFiles.addNode("bigFile" + i, "nt:file");
      Node contentNode = localBigFile.addNode("jcr:content", "nt:resource");
      //contentNode.setProperty("jcr:encoding", "UTF-8");
      InputStream is = new FileInputStream(TEST_FILE);
      contentNode.setProperty("jcr:data", is); 
      contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
      is.close();
      System.out.println("Data is set: "+TEST_FILE);
      //contentNode.setProperty("jcr:mimeType", "video/avi");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }
    System.out.println("Saving: "+TEST_FILE+" "+Runtime.getRuntime().freeMemory());
    session.save();
    System.out.println("Saved: "+TEST_FILE+" "+Runtime.getRuntime().freeMemory());
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving (local big):" 
        + ((endTime - startTime) / 1000) + "s");
    
    // check streams
//    compareStream(new FileInputStream(TEST_FILE), 
//      testLocalBigFiles.getProperty("bigFile0/jcr:content/jcr:data").getStream());
  }
  
  /*public void testLocalBigMediaFiles() throws Exception {
    Node testLocalBigMediaFiles = testBinaryValue.addNode("testLocalBigMediaFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    for (int i = 0; i < 1; i++) {
      Node localBigMediaFile = testLocalBigMediaFiles.addNode("bigFile" + i, "nt:file");
      Node contentNode = localBigMediaFile.addNode("jcr:content", "nt:resource");
      //contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new FileInputStream(LOCAL_BIG_MEDIA_FILE)); 
      contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
      //contentNode.setProperty("jcr:mimeType", "video/avi");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }
    session.save();
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving (local big):" 
        + ((endTime - startTime) / 1000) + "s");
    
  }
  
  // --------------- local stuff for anvanced testing -------------

  public void testLocalSmallFiles() throws Exception {
    Node testLocalSmallFiles = testBinaryValue.addNode("testLocalSmallFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    for (int i = 0; i < FILES_COUNT; i++) {
      Node localSmallFile = testLocalSmallFiles.addNode("smallFile" + i, "nt:file");
      Node contentNode = localSmallFile.addNode("jcr:content", "nt:resource");
      //contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new FileInputStream(LOCAL_SMALL_FILE));
      contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }
    session.save();
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving: (local small)"
        + ((endTime - startTime) / 1000) + "s");
    
    // check streams
    //compareStream(new FileInputStream(LOCAL_SMALL_FILE), 
    //    testLocalSmallFiles.getProperty("smallFile0/jcr:content/jcr:data").getStream());    
  }

  public void testRemoteBigFiles() throws Exception {
    Node testRemoteBigFiles = testBinaryValue.addNode("testRemoteBigFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    for (int i = 0; i < FILES_COUNT; i++) {
      Node remoteBigFile = testRemoteBigFiles.addNode("bigFile" + i, "nt:file");
      Node contentNode = remoteBigFile.addNode("jcr:content", "nt:resource");
      //contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new FileInputStream(REMOTE_BIG_FILE));
      contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }
    session.save();
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving (remote big):"
        + ((endTime - startTime) / 1000) + "s");
    
    // check streams
    compareStream(new FileInputStream(REMOTE_BIG_FILE), 
        testRemoteBigFiles.getProperty("bigFile0/jcr:content/jcr:data").getStream());
  }

  public void testRemoteSmallFiles() throws Exception {
    Node testRemoteSmallFiles = testBinaryValue.addNode("testRemoteSmallFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    for (int i = 0; i < FILES_COUNT; i++) {
      Node remoteSmallFile = testRemoteSmallFiles.addNode("smallFile" + i, "nt:file");
      Node contentNode = remoteSmallFile.addNode("jcr:content", "nt:resource");
      //contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new FileInputStream(REMOTE_SMALL_FILE));
      contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }
    session.save();
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving: (remote small)"
        + ((endTime - startTime) / 1000) + "s");

    // check streams
    compareStream(new FileInputStream(LOCAL_SMALL_FILE), 
        testRemoteSmallFiles.getProperty("smallFile0/jcr:content/jcr:data").getStream());    
  }

  public void testUrlBigFiles() throws Exception {
    Node testUrlBigFiles = testBinaryValue.addNode("testUrlBigFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    for (int i = 0; i < FILES_COUNT; i++) {
      Node urlBigFile = testUrlBigFiles.addNode("bigFile" + i, "nt:file");
      Node contentNode = urlBigFile.addNode("jcr:content", "nt:resource");
      //contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new URL(URL_BIG_FILE).openStream());
      contentNode.setProperty("jcr:mimeType", "video/avi");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }
    session.save();
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving: (url big)" + ((endTime - startTime) / 1000)
        + "s");
  }

  public void testUrlSmallFiles() throws Exception {
    Node testUrlSmallFiles = testBinaryValue.addNode("testUrlSmallFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    for (int i = 0; i < FILES_COUNT; i++) {
      Node urlSmallFile = testUrlSmallFiles.addNode("smallFile" + i, "nt:file");
      Node contentNode = urlSmallFile.addNode("jcr:content", "nt:resource");
      //contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", new URL(URL_SMALL_FILE).openStream());
      contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }
    session.save();
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving: (url small)" + ((endTime - startTime) / 1000) + "s");
  }*/

  protected void tearDown() throws Exception {
    //root.save();
    //super.tearDown();
  }
}

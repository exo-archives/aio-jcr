/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.blob;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestExportBigFiles extends JcrAPIBaseTest {
  /**
   * Testing SysView import - export
   * 
   * @throws Exception
   */
  public void testBigExportSysView() throws Exception {
    // 300 MB creating file
    String TEST_FILE = createBLOBTempFile(1000*1000).getAbsolutePath();
    Node testLocalBigFiles = root.addNode("testLocalBigFiles");

    // add file to repository
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    Node localBigFile = testLocalBigFiles.addNode("bigFile" + 1, "nt:file");
    Node contentNode = localBigFile.addNode("jcr:content", "nt:resource");
    // contentNode.setProperty("jcr:encoding", "UTF-8");
    InputStream is = new FileInputStream(TEST_FILE);
    contentNode.setProperty("jcr:data", is);
    contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
    is.close();
    System.out.println("Data is set: " + TEST_FILE);
    // contentNode.setProperty("jcr:mimeType", "video/avi");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    session.save();

    System.out.println("Saved: " + TEST_FILE + " " + Runtime.getRuntime().freeMemory());
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving (local big):" + ((endTime - startTime) / 1000)
        + "s");

    // Exporting repository content
    File file = File.createTempFile("tesSysExport", ".xml");

    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    session.exportSystemView(testLocalBigFiles.getPath(), bufferedOutputStream, false, false);
    bufferedOutputStream.flush();
    bufferedOutputStream.close();
    assertTrue(file.length() > 0);

    // removing source node
    testLocalBigFiles.remove();
    session.save();

    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

    // importing content
    session.importXML(root.getPath(),
        bufferedInputStream,
        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);

    session.save();

    Node n1 = root.getNode("testLocalBigFiles");
    Node lbf = n1.getNode("bigFile" + 1);
    Node content = lbf.getNode("jcr:content");

    // comparing with source file
    compareStream(new BufferedInputStream(new FileInputStream(TEST_FILE)), content
        .getProperty("jcr:data").getStream());
    
    n1.remove();
    session.save();
    file.deleteOnExit();
    file.delete();

  }

  /**
   * Testing SysView import - export
   * 
   * @throws Exception
   */
  public void testBigExportDocView() throws Exception {
    // 300 MB creating file
    String TEST_FILE2 = createBLOBTempFile(1000*20).getAbsolutePath();
    Node testLocalBigFiles = root.addNode("testDocView");

    // add file to repository
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    Node localBigFile = testLocalBigFiles.addNode("bigFile" + 1, "nt:file");
    Node contentNode = localBigFile.addNode("jcr:content", "nt:resource");
    // contentNode.setProperty("jcr:encoding", "UTF-8");
    InputStream is = new FileInputStream(TEST_FILE2);
    contentNode.setProperty("jcr:data", is);
    contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
    is.close();
    System.out.println("Data is set: " + TEST_FILE2);
    // contentNode.setProperty("jcr:mimeType", "video/avi");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    session.save();

    System.out.println("Saved: " + TEST_FILE2 + " " + Runtime.getRuntime().freeMemory());
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving (local big):" + ((endTime - startTime) / 1000)
        + "s");

    // Exporting repository content
    File file = File.createTempFile("tesDocExport", ".xml");

    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    session.exportDocumentView(testLocalBigFiles.getPath(), bufferedOutputStream, false, false);
    bufferedOutputStream.flush();
    bufferedOutputStream.close();
    assertTrue(file.length() > 0);

    // removing source node
    testLocalBigFiles.remove();
    session.save();

    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

    // importing content
    session.importXML(root.getPath(),
        bufferedInputStream,
        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);

    session.save();

    Node n1 = root.getNode("testDocView");
    Node lbf = n1.getNode("bigFile" + 1);
    Node content = lbf.getNode("jcr:content");

    // comparing with source file
    compareStream(new BufferedInputStream(new FileInputStream(TEST_FILE2)), content
        .getProperty("jcr:data").getStream());
    
    n1.remove();
    session.save();
    file.deleteOnExit();
    file.delete();
  }

  public void testIEPdfFiles() throws Exception {
    //
    // 300 MB creating file
    String TEST_FILE = "D:/Dev/DOC/jsr170-1.0.pdf";
    Node testLocalBigFiles = root.addNode("testLocalBigFiles");

    // add file to repository
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start
    Node localBigFile = testLocalBigFiles.addNode("bigFile" + 1, "nt:file");
    Node contentNode = localBigFile.addNode("jcr:content", "nt:resource");
    // contentNode.setProperty("jcr:encoding", "UTF-8");
    InputStream is = new FileInputStream(TEST_FILE);
    contentNode.setProperty("jcr:data", is);
    contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
    is.close();
    System.out.println("Data is set: " + TEST_FILE);
    // contentNode.setProperty("jcr:mimeType", "video/avi");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    session.save();

    System.out.println("Saved: " + TEST_FILE + " " + Runtime.getRuntime().freeMemory());
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving (local big):" + ((endTime - startTime) / 1000)
        + "s");

    // Exporting repository content
    File file = File.createTempFile("tesSysExport", ".xml");

    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    session.exportSystemView(testLocalBigFiles.getPath(), bufferedOutputStream, false, false);
    bufferedOutputStream.flush();
    bufferedOutputStream.close();
    assertTrue(file.length() > 0);

    // removing source node
    testLocalBigFiles.remove();
    session.save();

    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

    // importing content
    session.importXML(root.getPath(),
        bufferedInputStream,
        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);

    session.save();

    Node n1 = root.getNode("testLocalBigFiles");
    Node lbf = n1.getNode("bigFile" + 1);
    Node content = lbf.getNode("jcr:content");

    // comparing with source file
    compareStream(new BufferedInputStream(new FileInputStream(TEST_FILE)), content
        .getProperty("jcr:data").getStream());
    
    n1.remove();
    session.save();
    file.deleteOnExit();
    file.delete();
    
  }
}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;



/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class testWriteFile extends BaseStandaloneTest {

  protected static Log logger = ExoLogger
      .getLogger("jcr.JCRTest.testWriteFile");

  protected String servername;

  private File testFileBig;
  private File testFileSmall;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();

    if (testFileBig == null) {
      testFileBig = createBLOBTempFile(this.getClass().getSimpleName() + "_",
          100 * 1024); // 100 Mb
      testFileBig.deleteOnExit();
    }
    
    if (testFileSmall == null) {
      testFileSmall = createBLOBTempFile(this.getClass().getSimpleName() + "_",
          1); // 1 Kb
      testFileSmall.deleteOnExit();
    }
  }

  
  /**
   * Process sequential write test
   * @param file
   * @throws Exception
   */
  private void processFile(File file)throws Exception{
    
    assertTrue(file.exists());
    FileInputStream fis = new FileInputStream(file);

    String filename = file.getName();

    SmbFile smbfile = new SmbFile("smb://" + servername + "/ws/" + filename);

    OutputStream os = smbfile.getOutputStream();

    byte[] b = new byte[0x4000];

    int i = 40;

    while (i > 0) {
      i = fis.read(b);
      if (i != -1)
        os.write(b, 0, i);
    }

    os.close();

    fis.close();
    
    // check changes in jcr;

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

    s.refresh(false);

    Node root = s.getRootNode();

    // create nt:file node
    Property createdNodeProp = root.getNode(filename).getNode("jcr:content")
        .getProperty("jcr:data");

    fis = new FileInputStream(file);

    InputStream jcris = createdNodeProp.getStream();

    long filesize = file.length();

    long jcrpropsize = createdNodeProp.getLength();

    assertEquals(filesize, jcrpropsize);

    compareStream(new FileInputStream(file), jcris, 0, 0, filesize);
    
    // delete test objects
    root.getNode(filename).remove();
    s.save();

    
  }
  
  public void testWriteSmallFile() throws Exception {
    
    processFile(testFileSmall);
/*
    File tf = new File("src/test/resources/test.txt");

    FileInputStream fis = new FileInputStream(tf);

    assertTrue(tf.exists());

    String filename = "wrnewfile.txt";

    SmbFile file = new SmbFile("smb://" + servername + "/ws/" + filename);

    // file.createNewFile();

    OutputStream os = file.getOutputStream();

    byte[] b = new byte[0x4000];

    int i = 40;

    while (i > 0) {
      i = fis.read(b);
      if (i != -1)
        os.write(b, 0, i);
    }

    os.close();

    fis.close();
    // check changes in jcr;

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

    s.refresh(false);

    Node root = s.getRootNode();

    // create nt:file node
    Property createdNodeProp = root.getNode(filename).getNode("jcr:content")
        .getProperty("jcr:data");

    fis = new FileInputStream(tf);

    InputStream jcris = createdNodeProp.getStream();

    long filesize = tf.length();

    long jcrpropsize = createdNodeProp.getLength();

    assertEquals(filesize, jcrpropsize);

    byte[] jcrbuf = new byte[(int) jcrpropsize];

    int temp1 = jcris.read(jcrbuf);

    jcris.close();

    byte[] tfbuf = new byte[(int) filesize];

    int temp2 = fis.read(tfbuf);
    fis.close();

    logger.debug("jcr:data length : " + jcrpropsize);

    // check if received data from node via jcr methods and smb equals

    assertTrue(java.util.Arrays.equals(jcrbuf, tfbuf));

    // delete test objects
    root.getNode(filename).remove();
    s.save();
*/
  }


  /**
   * Writes large file
   * <p>
   * I have a problems with commiting files over 100 Mb? so this test is
   * optional.
   * <p>
   * 
   * @throws Exception
   */
  public void testWriteLargeFile() throws Exception {
    processFile(testFileBig);
  }

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
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

public class testReadFile extends BaseStandaloneTest {
  protected static Log logger = ExoLogger.getLogger("jcr.JCRTest.testReadFile");

  protected String servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();
  }

  public void testReadSmallFile() throws Exception {

    File tf = new File("src/test/resources/test.txt");

    assertTrue(tf.exists());

    FileInputStream str = new FileInputStream(tf);

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

    s.refresh(false);

    Node root = s.getRootNode();

    String filename = "testfile.txt";

    // create nt:file node
    Node createdNodeRef = root.addNode(filename, "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType(filename);

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", str);

    str.close();

    // done
    s.save();

    SmbFile file = new SmbFile("smb://" + servername + "/ws/" + filename);

    assertTrue(file.exists());

    InputStream smbis = file.getInputStream();

    int smbsize = file.getContentLength();
    byte[] smbbuf = new byte[smbsize];
    int temp1 = smbis.read(smbbuf);
    smbis.close();
    // read length control
    assertEquals(smbsize, temp1);
    logger.debug("smbfile length : " + smbsize);

    InputStream jcrstream = dataNode.getProperty("jcr:data").getStream();

    long length = dataNode.getProperty("jcr:data").getLength();
    byte[] nbuf = new byte[(int) length];
    long temp2 = jcrstream.read(nbuf);
    jcrstream.close();
    // read length control
    assertEquals(length, temp2);
    logger.debug("jcr:data length : " + length);

    // check if received data from node via jcr methods and smb equals

    assertTrue(java.util.Arrays.equals(smbbuf, nbuf));

    // delete test objects
    createdNodeRef.remove();
    s.save();

  }

  public void testReadLargeFile() throws Exception {

    File tf = new File("src/test/resources/largefile.mp3");

    assertTrue(tf.exists());

    FileInputStream str = new FileInputStream(tf);

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);

    Node root = s.getRootNode();

    String filename = "largefile.mp3";

    assertFalse(s.itemExists("/" + filename));

    // create nt:file node
    Node createdNodeRef = root.addNode(filename, "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType(filename);

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", str);

    str.close();

    // done
    s.save();

    SmbFile file = new SmbFile("smb://" + servername + "/ws/" + filename);

    assertTrue(file.exists());

    InputStream smbis = file.getInputStream();

    int smbsize = file.getContentLength();
    byte[] smbbuf = new byte[smbsize];
    int temp1 = smbis.read(smbbuf);
    smbis.close();
    // read length control
    assertEquals(smbsize, temp1);
    logger.debug("smbfile length : " + smbsize);

    InputStream jcrstream = dataNode.getProperty("jcr:data").getStream();

    long length = dataNode.getProperty("jcr:data").getLength();
    byte[] nbuf = new byte[(int) length];
    long temp2 = jcrstream.read(nbuf);
    jcrstream.close();
    // read length control
    assertEquals(length, temp2);
    logger.debug("jcr:data length : " + length);

    // check if received data from node via jcr methods and smb equals

    assertTrue(java.util.Arrays.equals(smbbuf, nbuf));

    // delete test objects

    ((Node) s.getItem("/" + filename)).remove();
    s.save();

    assertFalse(s.itemExists("/" + filename));

  }

}

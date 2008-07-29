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
package org.exoplatform.services.cifs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Session;

import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

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

    File testFileSmall = createBLOBTempFile(this.getClass().getSimpleName() + "_",
          1); // 1 Kb
      testFileSmall.deleteOnExit();
    
    assertTrue(testFileSmall.exists());

    FileInputStream str = new FileInputStream(testFileSmall);

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

    SmbFile file = new SmbFile("smb://" + user + servername + "/ws/" + filename);

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

  /**
   * Reads large file
   * <p>
   * I have a problems with commiting files over 100 Mb? so this test is
   * optional.
   * <p>
   * You can put any large file in /test/resources and rename it to
   * largefile.mp3. after that - uncomment this test.
   * 
   * @throws Exception
   */
  /*
   * public void testReadLargeFile() throws Exception {
   * 
   * File tf = new File("src/test/resources/largefile.mp3");
   * 
   * assertTrue(tf.exists());
   * 
   * FileInputStream str = new FileInputStream(tf);
   * 
   * Session s = null; Credentials credentials = new CredentialsImpl("admin",
   * "admin" .toCharArray()); s = (SessionImpl)
   * (repositoryService.getDefaultRepository()).login( credentials, "ws");
   * s.refresh(false);
   * 
   * Node root = s.getRootNode();
   * 
   * String filename = "largefile.mp3";
   * 
   * assertFalse(s.itemExists("/" + filename));
   *  // create nt:file node Node createdNodeRef = root.addNode(filename,
   * "nt:file");
   * 
   * Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");
   * 
   * MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
   * mimetypeResolver.setDefaultMimeType("application/zip"); String mimeType =
   * mimetypeResolver.getMimeType(filename);
   * 
   * dataNode.setProperty("jcr:mimeType", mimeType);
   * dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
   * dataNode.setProperty("jcr:data", str);
   * 
   * str.close();
   *  // done s.save();
   * 
   * SmbFile file = new SmbFile("smb://" + servername + "/ws/" + filename);
   * 
   * assertTrue(file.exists());
   * 
   * InputStream smbis = file.getInputStream();
   * 
   * int smbsize = file.getContentLength(); byte[] smbbuf = new byte[smbsize];
   * int temp1 = smbis.read(smbbuf); smbis.close(); // read length control
   * assertEquals(smbsize, temp1); logger.debug("smbfile length : " + smbsize);
   * 
   * InputStream jcrstream = dataNode.getProperty("jcr:data").getStream();
   * 
   * long length = dataNode.getProperty("jcr:data").getLength(); byte[] nbuf =
   * new byte[(int) length]; long temp2 = jcrstream.read(nbuf);
   * jcrstream.close(); // read length control assertEquals(length, temp2);
   * logger.debug("jcr:data length : " + length);
   *  // check if received data from node via jcr methods and smb equals
   * 
   * assertTrue(java.util.Arrays.equals(smbbuf, nbuf));
   *  // delete test objects
   * 
   * ((Node) s.getItem("/" + filename)).remove(); s.save();
   * 
   * assertFalse(s.itemExists("/" + filename));
   *  }
   */

}

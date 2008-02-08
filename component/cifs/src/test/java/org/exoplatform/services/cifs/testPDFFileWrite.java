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
import java.io.OutputStream;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Test of pdf file write
 * 
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class testPDFFileWrite extends BaseStandaloneTest {

  protected static Log logger = ExoLogger
      .getLogger("jcr.JCRTest.testWriteFile");

  protected String servername;

  private File testPDFFile;

  public void setUp() throws Exception {
    super.setUp();

    testPDFFile = new File("src/test/resources/index/test_index.pdf");

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();
  }

  /**
   * Process sequential write test
   * 
   * @param file
   * @throws Exception
   */
  private void processFile(File file) throws Exception {

    assertTrue(file.exists());
    FileInputStream fis = new FileInputStream(file);
    String filename = file.getName();
    SmbFile smbfile = new SmbFile("smb://" + user + servername + "/ws/" +
        filename);
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

  public void testWriteFile() throws Exception {

    processFile(testPDFFile);
  }

}

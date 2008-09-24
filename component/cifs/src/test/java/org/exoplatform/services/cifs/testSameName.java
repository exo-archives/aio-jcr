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
 * Created by The eXo Platform SAS Author : Tuan Nguyen tuan.nguyen@exoplatform.com 30.10.2007
 */
public class testSameName extends BaseStandaloneTest {
  protected static Log logger = ExoLogger.getLogger("jcr.JCRTest.testCreateFileFolder");

  protected String     servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null
        ? serv.getConfiguration().getWin32ServerName()
        : serv.getConfiguration().getServerName();
  }

  /**
   * Test not fail if it run single.
   * 
   * @throws Exception
   */
  public void testCreateFile() throws Exception {

    String newname = "newfile.txt";

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(credentials, "ws");
    s.refresh(false);

    // create nt:file 1 node
    Node createdNodeRef = s.getRootNode().addNode(newname, "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType("testnode");

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", "");
    s.save();

    // create nt:file 2node
    createdNodeRef = s.getRootNode().addNode(newname, "nt:file");

    dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    mimeType = mimetypeResolver.getMimeType("testnode");

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", "");
    s.save();

    SmbFile file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[0]"); // ?
    assertTrue(file.exists());
    file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[1]");
    assertTrue(file.exists());
    file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[2]");
    assertTrue(file.exists());
    file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[3]");
    assertTrue(!file.exists());
    file = new SmbFile("smb://" + user + servername + "/ws/" + newname);
    assertTrue(file.exists());

    file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[2]");

    String ren = "rename.txt";
    SmbFile renfile = new SmbFile("smb://" + user + servername + "/ws/" + ren);

    file.renameTo(renfile);

    file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[0]"); // ?
    assertTrue(file.exists());
    file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[1]");
    assertTrue(file.exists());
    file = new SmbFile("smb://" + user + servername + "/ws/" + newname + "[2]");
    assertTrue(!file.exists());

    file = new SmbFile("smb://" + user + servername + "/ws/" + newname);
    assertTrue(file.exists());

    file = new SmbFile("smb://" + user + servername + "/ws/" + ren);
    assertTrue(file.exists());

    // rename first one
    file = new SmbFile("smb://" + user + servername + "/ws/" + newname);
    renfile = new SmbFile("smb://" + user + servername + "/ws/" + "ren1.txt");
    file.renameTo(renfile);

    file = new SmbFile("smb://" + user + servername + "/ws/" + newname);
    assertTrue(!file.exists());

    file = new SmbFile("smb://" + user + servername + "/ws/" + ren);
    assertTrue(file.exists());
    file = new SmbFile("smb://" + user + servername + "/ws/" + "ren1.txt");
    assertTrue(file.exists());

  }

}

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
 * Created by The eXo Platform SAS Author : Sergey Karpenko <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class testDeleteFileFolder extends BaseStandaloneTest {
  protected static Log logger = ExoLogger.getLogger("jcr.JCRTest.testDeleteFileFolder");

  protected String     servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null
        ? serv.getConfiguration().getWin32ServerName()
        : serv.getConfiguration().getServerName();
  }

  /**
   * Test delete file
   * 
   * @throws Exception
   */
  public void testDeleteExistFile() throws Exception {

    Session s = null;

    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(credentials, "ws");
    s.refresh(false);

    String newname = "newfile.dat";

    // create nt:file node
    Node createdNodeRef = s.getRootNode().addNode(newname, "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType("testnode");

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", "");
    s.save();

    SmbFile newfile = new SmbFile("smb://" + user + servername + "/ws/" + newname);

    assertTrue(newfile.exists());

    newfile.delete();

    assertFalse(newfile.exists());

    assertFalse(s.itemExists("/" + newname));

  }

  /**
   * Test delete unexist file
   * 
   * @throws Exception
   */
  public void testDeleteNotExistFile() throws Exception {

    String newname = "unexist.dat";

    SmbFile newfile = new SmbFile("smb://" + user + servername + "/ws/" + newname);

    assertFalse(newfile.exists());

    try {
      newfile.delete();
    } catch (jcifs.smb.SmbException e) {
      // here must be exception
      return;
    }

    fail();

  }

  /**
   * Test delete folder with conent
   * 
   * @throws Exception
   */
  public void testDeleteExistFoilder() throws Exception {

    Session s = null;

    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(credentials, "ws");
    s.refresh(false);

    String newname = "hhfolder";

    Node cr = s.getRootNode().addNode(newname);

    cr.addNode("subfolder1");
    cr.addNode("subfolder2");

    s.save();

    SmbFile newfile = new SmbFile("smb://" + user + servername + "/ws/" + newname + "/");

    assertTrue(newfile.exists());

    newfile.delete();

    assertFalse(newfile.exists());

    assertFalse(s.itemExists("/" + newname));

  }

}

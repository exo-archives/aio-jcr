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

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class testRenameMoveCopy extends BaseStandaloneTest {

  protected String servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null
        ? serv.getConfiguration().getWin32ServerName()
        : serv.getConfiguration().getServerName();
  }

  /**
   * Test renaime file
   * 
   * @throws Exception
   */
  public void testRenameFile() throws Exception {

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(credentials, "ws");

    s.refresh(false);

    Node root = s.getRootNode();

    String filename = "testnode.txt";

    // create nt:file node
    Node createdNodeRef = root.addNode(filename, "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType(filename);

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", "");

    // done
    s.save();

    SmbFile file = new SmbFile("smb://" + user + servername + "/ws/" + filename);

    assertTrue(file.exists());

    String newname = "renfile.txt";
    SmbFile renfile = new SmbFile("smb://" + user + servername + "/ws/" + newname);

    assertFalse(renfile.exists());
    file.renameTo(renfile);
    assertTrue(renfile.exists());
    assertFalse(file.exists());

    // check changes in repository

    assertFalse(s.itemExists("/" + filename));
    assertTrue(s.itemExists("/" + newname));

    Node n = ((Node) s.getItem("/" + newname));
    n.remove();

    s.save();

  }

  /**
   * Test renaime folder
   * 
   * @throws Exception
   */
  public void testRenameFolder() throws Exception {

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(credentials, "ws");

    s.refresh(false);

    Node root = s.getRootNode();

    String oldname = "folder";

    // create folder in repository
    root.addNode(oldname);

    // done
    s.save();

    SmbFile folder = new SmbFile("smb://" + user + servername + "/ws/" + oldname);

    assertTrue(folder.exists());

    String newname = "newfolder";
    SmbFile renfolder = new SmbFile("smb://" + user + servername + "/ws/" + newname);

    // check if new name not exist
    assertFalse(renfolder.exists());

    folder.renameTo(renfolder); // rename

    assertTrue(renfolder.exists());
    assertFalse(folder.exists()); // check if old file deleted

    // check changes in repository

    assertFalse(s.itemExists("/" + oldname));
    assertTrue(s.itemExists("/" + newname));

    Node n = ((Node) s.getItem("/" + newname));
    n.remove();

    s.save();

  }

  /**
   * Test copy file
   * 
   * @throws Exception
   */
  public void testCopyFile() throws Exception {

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(credentials, "ws");

    s.refresh(false);

    Node root = s.getRootNode();

    String oldname = "testnode.txt";

    // create nt:file node
    Node createdNodeRef = root.addNode(oldname, "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType(oldname);

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", "");

    // done
    s.save();

    SmbFile file = new SmbFile("smb://" + user + servername + "/ws/" + oldname);

    assertTrue(file.exists());

    String newname = "newfile.txt";
    SmbFile cpyfile = new SmbFile("smb://" + user + servername + "/ws/" + newname);

    // check if new name not exist
    assertFalse(cpyfile.exists());

    file.copyTo(cpyfile); // rename

    assertTrue(cpyfile.exists());
    assertTrue(file.exists()); // check that both files exist

    // check changes in repository

    assertTrue(s.itemExists("/" + oldname));
    assertTrue(s.itemExists("/" + newname));

    // delete test temporary objects
    Node n = ((Node) s.getItem("/" + newname));
    n.remove();
    n = ((Node) s.getItem("/" + oldname));
    n.remove();

    s.save();

  }

  /**
   * Test copy Folder
   * 
   * @throws Exception
   */
  public void testCopyFolder() throws Exception {

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin".toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(credentials, "ws");

    s.refresh(false);

    Node root = s.getRootNode();

    String oldname = "folder";

    // create folder node
    Node createdNodeRef = root.addNode(oldname);

    createdNodeRef.addNode("gipgip1");
    createdNodeRef.addNode("gipgip2");

    // done
    s.save();

    SmbFile folder = new SmbFile("smb://" + user + servername + "/ws/" + oldname + "/");

    assertTrue(folder.exists());

    String newname = "jcr'3a'system/newfolder";
    SmbFile cpyfolder = new SmbFile("smb://" + user + servername + "/ws/" + newname + "/");

    // check if new name not exist
    assertFalse(cpyfolder.exists());

    folder.copyTo(cpyfolder); // copy

    assertTrue(cpyfolder.exists());
    assertTrue(cpyfolder.exists()); // check that both files exist

    // check changes in repository

    assertTrue(s.itemExists("/" + oldname));
    assertTrue(s.itemExists("/jcr:system/newfolder"));
    assertTrue(s.itemExists("/jcr:system/newfolder/gipgip1"));

    // delete test temporary objects
    Node n = ((Node) s.getItem("/jcr:system/newfolder"));
    n.remove();
    n = ((Node) s.getItem("/" + oldname));
    n.remove();

    s.save();

  }

}

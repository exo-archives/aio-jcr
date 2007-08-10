/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Session;

import jcifs.smb.SmbFile;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class testRenameMoveCopy extends BaseStandaloneTest {

  protected String servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();
  }

  /**
   * Test renaime file
   * 
   * @throws Exception
   */
  public void testRenameFile() throws Exception {

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

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

    SmbFile file = new SmbFile("smb://" + servername + "/ws/" + filename);

    assertTrue(file.exists());

    String newname = "renfile.txt";
    SmbFile renfile = new SmbFile("smb://" + servername + "/ws/" + newname);

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
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    
    s.refresh(false);

    Node root = s.getRootNode();

    String oldname = "folder";

    // create folder in repository
    root.addNode(oldname);

    // done
    s.save();

    SmbFile folder = new SmbFile("smb://" + servername + "/ws/" + oldname);

    assertTrue(folder.exists());

    String newname = "newfolder";
    SmbFile renfolder = new SmbFile("smb://" + servername + "/ws/" + newname);

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
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

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
    
    SmbFile file = new SmbFile("smb://" + servername + "/ws/" + oldname);

    assertTrue(file.exists());

    String newname = "newfile.txt";
    SmbFile cpyfile = new SmbFile("smb://" + servername + "/ws/" + newname);

    // check if new name not exist
    assertFalse(cpyfile.exists());

    file.copyTo(cpyfile); // rename

    
    assertTrue(cpyfile.exists());
    assertTrue(file.exists()); // check that both files exist
    
    
    // check changes in repository

    assertTrue(s.itemExists("/" + oldname));
    assertTrue(s.itemExists("/" + newname));

    
    //delete test temporary objects
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
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

    s.refresh(false);
    
    Node root = s.getRootNode();

    String oldname = "folder";

    // create folder node
    Node createdNodeRef = root.addNode(oldname);

    createdNodeRef.addNode("gipgip1");
    createdNodeRef.addNode("gipgip2");

    // done
    s.save();

    SmbFile folder = new SmbFile("smb://" + servername + "/ws/" + oldname+"/");

    assertTrue(folder.exists());

    String newname = "jcr'3a'system/newfolder";
    SmbFile cpyfolder = new SmbFile("smb://" + servername + "/ws/" + newname+"/");

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

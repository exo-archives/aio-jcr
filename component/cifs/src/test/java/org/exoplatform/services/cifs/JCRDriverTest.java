/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs;

import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.StandaloneContainer;

import org.exoplatform.services.cifs.smb.server.JCRDriver;
import org.exoplatform.services.cifs.server.filesys.AccessMode;
import org.exoplatform.services.cifs.server.filesys.FileAction;
import org.exoplatform.services.cifs.server.filesys.FileAttribute;
import org.exoplatform.services.cifs.server.filesys.FileExistsException;
import org.exoplatform.services.cifs.server.filesys.FileInfo;
import org.exoplatform.services.cifs.server.filesys.FileOpenParams;
import org.exoplatform.services.cifs.server.filesys.NameCoder;
import org.exoplatform.services.cifs.server.filesys.TreeConnection;
import org.exoplatform.services.cifs.smb.SMBDate; // import
// org.exoplatform.services.cifs.smb.SMBStatus;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.security.impl.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

import junit.framework.TestCase;

// import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL Karpenko Sergey
 */

public class JCRDriverTest extends TestCase {

  private RepositoryService repositoryService = null;

  public JCRDriverTest() {
    super();
    InitializeRepositoryService();
  }

  public JCRDriverTest(String str) {
    super(str);
    InitializeRepositoryService();
  }

  protected void InitializeRepositoryService() {
    try {
      StandaloneContainer
          .addConfigurationPath("src/main/java/conf/standalone/cifs-configuration.xml");

      // obtain standalone container
      StandaloneContainer container = StandaloneContainer.getInstance();

      // set JAAS auth config
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config",
            "src/main/resources/login.conf");

      // obtain default repository
      repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);
    } catch (Exception e) {
    }

  }

  public void testCreateNode_File() {
    Session s = null;

    try {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      s = (SessionImpl) (repositoryService.getRepository("db1").login(credentials,
          "ws"));
    } catch (Exception e) {
      fail();
      return;
    }

    try {
      JCRDriver.createNode(s, "/subfolder/exp_test1.txt", true);
    } catch (PathNotFoundException e) {
      fail();
    } catch (RepositoryException e) {
      fail();
    } catch (Exception e) {
      fail();
    }

    try {
      Node n = (Node) s.getItem("/subfolder/exp_test1.txt");

      assertTrue(n.isNodeType("nt:file"));
      assertTrue(n.hasNode("jcr:content"));
      Node cont = n.getNode("jcr:content");
      assertTrue(cont.hasProperty("jcr:data"));

      cont = null;
      n.remove();
    } catch (Exception e) {
      fail();

    }
    s.logout();

  }

  public void testCreateNode_Folder() {
    Session s = null;

    try {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      s = (SessionImpl) (repositoryService.getRepository("db1").login(credentials,
          "ws"));
    } catch (Exception e) {
      fail();
      return;
    }

    try {
      JCRDriver.createNode(s, "/subfolder/folder_test", false);
    } catch (PathNotFoundException e) {
      fail();
    } catch (RepositoryException e) {
      fail();
    } catch (Exception e) {
      fail();
    }

    try {
      Node n = (Node) s.getItem("/subfolder/folder_test");

      assertFalse(n.isNodeType("nt:file"));

      n.remove();
    } catch (Exception e) {
      fail();

    }
    s.logout();
  }

  /**
   * Here is the test correct file (not directory) creation and suppose to be
   * check correct file attributes, date/time, and other setup.
   */
  public void testCreateFile_CorrectFile() {
    Session s = null;

    try {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      s = (SessionImpl) (repositoryService.getRepository("db1").login(credentials,
          "ws"));
    } catch (Exception e) {
      fail();
      return;
    }

    TreeConnection conn = new TreeConnection(null);
    conn.setSession(s);

    int access = AccessMode.DenyWrite + AccessMode.DenyRead
        + AccessMode.ReadWrite;
    int srchAttr = 0; // not used
    int fileAttr = FileAttribute.Normal;
    int crTime = 0; // not used
    int crDate = 0; // not used
    int openFunc = FileAction.NTOverwriteIf;
    int allocSiz = 0; // not used

    // Extract the filename string

    String fileName = "\\hello1.dat"; // path name for creation

    // convert name

    if (fileName.equals("")) {
      fileName = "/";
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = path.toString();
    }

    // DecodeName is in FileOpenParam constructors
    // Check if the file name contains a stream name
    String temp = fileName;
    String stream = null;
    int pos = temp.indexOf(":");
    if (pos == -1) {
      fileName = NameCoder.DecodeName(temp);
    } else {
      fileName = NameCoder.DecodeName(temp.substring(0, pos));
      stream = temp.substring(pos);
    }

    // Create the file open parameters

    SMBDate crDateTime = null;
    crDateTime = new SMBDate(crDate, crTime);

    FileOpenParams params = new FileOpenParams(fileName, stream, openFunc,
        access, srchAttr, fileAttr, allocSiz, crDateTime.getTime());

    try {
      JCRDriver.createFile(conn, params);

      assertTrue(conn.getSession().itemExists("/hello1.dat"));
      Node n = (Node) conn.getSession().getItem("/hello1.dat");
      assertTrue(n.isNodeType("nt:file"));

      n.remove();
      conn.getSession().save();
      conn.getSession().logout();

    } catch (FileExistsException e) {
      fail();
    } catch (LockException e) {
      fail();
    } catch (RepositoryException e) {
      fail();
    }
  }

  /**
   * Here is the test correct directory creation and suppose to be check correct
   * file attributes, date/time, and other setup.
   */
  public void testCreateFile_CorrectDir() {
    Session s = null;

    try {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      s = (SessionImpl) (repositoryService.getRepository("db1").login(credentials,
          "ws"));
    } catch (Exception e) {
      fail();
      return;
    }

    TreeConnection conn = new TreeConnection(null);
    conn.setSession(s);

    int access = AccessMode.DenyWrite + AccessMode.DenyRead
        + AccessMode.ReadWrite;
    int srchAttr = 0; // not used
    int fileAttr = FileAttribute.Directory;
    int crTime = 0; // not used
    int crDate = 0; // not used
    int openFunc = 0; // its important if requested file exist
    int allocSiz = 0; // not used

    // Extract the filename string

    String fileName = "\\sub\\directory"; // path name for creation

    // convert name

    if (fileName.equals("")) {
      fileName = "/";
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = path.toString();
    }

    // DecodeName is in FileOpenParam constructors
    // Check if the file name contains a stream name
    String temp = fileName;
    String stream = null;
    int pos = temp.indexOf(":");
    if (pos == -1) {
      fileName = NameCoder.DecodeName(temp);
    } else {
      fileName = NameCoder.DecodeName(temp.substring(0, pos));
      stream = temp.substring(pos);
    }

    // Create the file open parameters

    SMBDate crDateTime = null;
    crDateTime = new SMBDate(crDate, crTime);

    FileOpenParams params = new FileOpenParams(fileName, stream, openFunc,
        access, srchAttr, fileAttr, allocSiz, crDateTime.getTime());

    try {
      JCRDriver.createFile(conn, params);

      assertTrue(conn.getSession().itemExists("/sub/directory"));
      Node n = (Node) conn.getSession().getItem("/sub/directory");
      assertFalse(n.isNodeType("nt:file"));

      n.remove();
      ((Node) conn.getSession().getItem("/sub")).remove();
      conn.getSession().save();
      conn.getSession().logout();
    } catch (FileExistsException e) {
      fail();
    } catch (LockException e) {
      fail();
    } catch (RepositoryException e) {
      fail();
    }
  }

  /**
   * Here is the test correct directory creation and suppose to be checking
   * correct file attributes, date/time, and other setup.
   */
  public void testCreateFile_ExistingFile_fail() {
    Session s = null;

    try {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      s = (SessionImpl) (repositoryService.getRepository("db1").login(credentials,
          "ws"));
    } catch (Exception e) {
      fail();
      return;
    }

    String type = "nt:file";// : "nt:folder";
    String name = "ExistingFile_fail";
    try {
      Node createdNodeRef = s.getRootNode().addNode(name, type);
      if (type == "nt:file") {
        Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

        MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
        mimetypeResolver.setDefaultMimeType("application/zip");
        String mimeType = mimetypeResolver.getMimeType(name);

        dataNode.setProperty("jcr:mimeType", mimeType);
        dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
        dataNode.setProperty("jcr:data", "");
      }
    } catch (RepositoryException e) {
      fail();
    }

    TreeConnection conn = new TreeConnection(null);
    conn.setSession(s);

    // make COM_OPEN_ANDX request simulation
    int access = AccessMode.DenyWrite + AccessMode.DenyRead
        + AccessMode.ReadWrite;
    int srchAttr = 0; // not used
    int fileAttr = FileAttribute.Directory;
    int crTime = 0; // not used
    int crDate = 0; // not used
    int openFunc = FileAction.OpenIfExists;
    int allocSiz = 0; // not used

    // Extract the filename string

    String fileName = "\\ExistingFile_fail";

    // convert name

    if (fileName.equals("")) {
      fileName = "/";
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = path.toString();
    }

    // DecodeName is in FileOpenParam constructors
    // Check if the file name contains a stream name
    String temp = fileName;
    String stream = null;
    int pos = temp.indexOf(":");
    if (pos == -1) {
      fileName = NameCoder.DecodeName(temp);
    } else {
      fileName = NameCoder.DecodeName(temp.substring(0, pos));
      stream = temp.substring(pos);
    }

    // Create the file open parameters

    SMBDate crDateTime = null;
    crDateTime = new SMBDate(crDate, crTime);

    FileOpenParams params = new FileOpenParams(fileName, stream, openFunc,
        access, srchAttr, fileAttr, allocSiz, crDateTime.getTime());

    try {
      JCRDriver.createFile(conn, params);
      fail();
      // assertTrue(conn.getSession().itemExists("/ExistingFile_fail"));

    } catch (FileExistsException e) {
      assertTrue(true);
    } catch (LockException e) {
      fail();
    } catch (RepositoryException e) {
      fail();
    }
    try {
      conn.getSession().logout();
      ((Node) conn.getSession().getItem("/ExistingFile_fail")).remove();
    } catch (Exception e) {
      fail();
    }
  }

  /**
   * There is test for JCRDriver.getFileInformation(Node) which test's getting
   * correct FileInfo object for simple file
   */
  public void testGetFileInfo_normal_file() {
    Session s = null;
    try {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      s = (SessionImpl) (repositoryService.getDefaultRepository().login(
          credentials, "ws"));

    } catch (Exception e) {
      fail();
    }

    FileInfo inf = null;

    try {
      String type = "nt:file";
      String name = "getnode.dat";
      Node createdNodeRef = s.getRootNode().addNode(name, type);
      if (type == "nt:file") {
        Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

        MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
        mimetypeResolver.setDefaultMimeType("application/zip");
        String mimeType = mimetypeResolver.getMimeType(name);

        dataNode.setProperty("jcr:mimeType", mimeType);
        dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
        dataNode.setProperty("jcr:data", "");
      }

      inf = JCRDriver.getFileInformation(createdNodeRef);

      createdNodeRef.remove();

    } catch (RepositoryException e) {
      fail();
    }

    if (inf == null)
      fail();

    assertEquals("getnode.dat", inf.getFileName());
    assertEquals(FileAttribute.NTNormal, inf.getFileAttributes());

    // TODO date time check
  }

  /**
   * There is test for JCRDriver.getFileInformation(Node), which test's getting
   * correct FileInfo object for directory "jcr:system"
   */
  public void testGetFileInfo_directory() {
    Session s = null;
    try {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      s = (SessionImpl) (repositoryService.getDefaultRepository().login(
          credentials, "ws"));

    } catch (Exception e) {
      fail();
    }

    FileInfo inf = null;

    try {
      Node nodeRef = (Node) s.getItem("/jcr:system");

      inf = JCRDriver.getFileInformation(nodeRef);

    } catch (RepositoryException e) {
      fail();
    }

    if (inf == null)
      fail();

    assertEquals("jcr'3a'system", inf.getFileName());
    assertEquals(FileAttribute.Directory, inf.getFileAttributes());

    // TODO date time check
    // TODO access mode check - it's not realized in getFileInformation yet
  }

  public void open() {
    // crea
    /*
     * Node nd; try{ nd= s.getRootNode().addNode("sub");
     * 
     * nd.addNode("file_create.txt", "nt:file"); Node dataNode =
     * nd.addNode("jcr:content","nt:resource");
     * 
     * MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
     * mimetypeResolver.setDefaultMimeType("application/zip"); String mimeType =
     * mimetypeResolver.getMimeType("file_create.txt");
     * 
     * dataNode.setProperty("jcr:mimeType",mimeType);
     * dataNode.setProperty("jcr:lastModified",Calendar.getInstance());
     * dataNode.setProperty("jcr:data",""); } catch(Exception e){ fail(); }
     */
  }

}

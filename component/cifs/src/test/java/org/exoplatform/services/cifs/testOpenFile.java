/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Session;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Here is tests of openenig and creation files
 * 
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 */

public class testOpenFile extends BaseStandaloneTest {
  protected static Log logger = ExoLogger.getLogger("jcr.JCRTest.testOpenFile");

  protected String servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();
  }

  /**
   * Here is test openeig of existed simplenamed file;
   * <p>
   * Used commands: TRANS2_QUERY_PATH lev101, NT_CREATE, FILE_CLOSE
   * 
   * @throws Exception
   */
  public void testOpenExistFile() throws Exception {

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);
    
    Node root = s.getRootNode();

    String filename = "testnode";

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

    SmbFile recfile = new SmbFile("smb://" + servername + "/ws/" + filename); // TRANS2_QUERY_PATH
    // 101 used here
    // not NT_CREATE

    assertTrue(recfile.isFile());

    SmbFileInputStream stream = (SmbFileInputStream) recfile.getInputStream(); // NT_CREATE

    assertNotNull(stream);
    stream.close(); // FILE_CLOSE called here

    // remove temporary objects
    createdNodeRef.remove();
    s.save();
  }

  /**
   * Here is test opening of nonexisted simplenamed file;
   * <p>
   * Used commands: TRANS2_QUERY_PATH lev101, NT_CREATE
   * 
   * @throws Exception
   */
  public void testOpenUnExistFile() throws Exception {

    SmbFile recfile = new SmbFile("smb://" + servername + "/ws/unexists.txt"); // TRANS2_QUERY_PATH
    // 101 used here
    // not NT_CREATE

    assertFalse(recfile.exists());

    SmbFileInputStream stream = null;
    try {
      stream = (SmbFileInputStream) recfile.getInputStream(); // NT_CREATE
    } catch (SmbException e) {
      // All rigth, here must be exception
    }

    if (stream != null)
      fail("file exist");
  }

  /**
   * test open exist folder
   * 
   * @throws Exception
   */
  public void testOpenExistFolder() throws Exception {

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

    s.refresh(false);
    Node root = s.getRootNode();

    String foldername = "testfolder";

    // create nt:folder node
    Node createdNodeRef = root.addNode(foldername, "nt:folder");

    s.save();

    SmbFile folder = new SmbFile("smb://" + servername + "/ws/" + foldername); // TRANS2_QUERY_PATH

    assertTrue(folder.exists());
    assertTrue(folder.isDirectory());

    SmbFileInputStream stream = (SmbFileInputStream) folder.getInputStream(); // NT_CREATE
    stream.close();

    createdNodeRef.remove();
    s.save();
  }
  
  
  

}

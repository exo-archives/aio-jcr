/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.cifs.server.filesys.NameCoder;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * There are test of gettin the list of files as content of folder, device, or
 * search result.
 * <p>
 * 
 * The SMB_TRANS2_FIND_FIRST & SMB_TRANS2_FIND_NEXT are used here.
 * <p>
 * 
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 */

public class testReadFolderContent extends BaseStandaloneTest {
  protected static Log logger = ExoLogger
      .getLogger("jcr.JCRTest.testReadFolderContent");

  /**
   * Test getting file list - content of device (workspace)
   * 
   * @throws Excpetion
   */
  public void testReadDeviceContent() throws Exception {
    // get realy used server name, Win32ServerName may not be initialized
    String servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName()
        : serv.getConfiguration().getServerName();

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);
    
    Node root = s.getRootNode();

    // create nt:file node
    Node createdNodeRef = root.addNode("testnode", "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType("testnode");

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", "");

    // done
    s.save();

    SmbFile share = new SmbFile("smb://" + servername + "/ws/");

    long t1 = System.currentTimeMillis();
    SmbFile[] filelist = share.listFiles();
    long t2 = System.currentTimeMillis() - t1;

    logger.debug("received " + filelist.length + "files on " + t2 + "ms");

    NodeIterator it = root.getNodes();
    int nlsize = (int) it.getSize();

    Node[] nodelist = new Node[nlsize];

    for (int i = 0; i < nlsize; i++) {
      nodelist[i] = it.nextNode();
    }

    // check length
    assertEquals(filelist.length, nodelist.length);

    // check each node for the same in received
    for (int i = 0; i < nlsize; i++) {
      boolean isexist = false;
      for (int j = 0; j < filelist.length; j++) {
        String jcrname = NameCoder.EncodeName(nodelist[i].getName());
        String receivname = filelist[j].isDirectory() ? filelist[j].getName()
            .substring(0, filelist[j].getName().length() - 1) : filelist[j]
            .getName();

        if (jcrname.equals(receivname)) {
          isexist = true;

          // names are equals, so check the types;
          assertEquals(nodelist[i].isNodeType("nt:file"), filelist[j].isFile());
        }
      }
      assertTrue(isexist);
    }

    createdNodeRef.remove();
    s.save();
  }

  /**
   * Test read folder content - list of files and subfolders
   * 
   * @throws Excpetion
   */
  public void testGetFileListFromFolder() throws Exception {
    // get realy used server name, Win32ServerName may not be initialized
    String servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName()
        : serv.getConfiguration().getServerName();

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);
    
    Node root = s.getRootNode();

    // create test parent folder node
    Node folder = root.addNode("testfolder");

    // create nt:file node
    Node createdNodeRef = folder.addNode("testnode", "nt:file");

    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType("testnode");

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", "");

    // create test subfolder with simple name
    folder.addNode("sipmplefolder");

    // create test subfolder with complicated name
    folder.addNode("jcr:subfolder");

    s.save();

    SmbFile share = new SmbFile("smb://" + servername + "/ws/testfolder/");

    long t1 = System.currentTimeMillis();
    SmbFile[] filelist = share.listFiles();
    long t2 = System.currentTimeMillis() - t1;

    logger.debug("received " + filelist.length + "files on " + t2 + "ms");

    NodeIterator it = root.getNode("testfolder").getNodes();
    int nlsize = (int) it.getSize();

    Node[] nodelist = new Node[nlsize];

    for (int i = 0; i < nlsize; i++) {
      nodelist[i] = it.nextNode();
    }

    // check length
    assertEquals(filelist.length, nodelist.length);

    // check each node for the same in received
    for (int i = 0; i < nlsize; i++) {
      boolean isexist = false;
      for (int j = 0; j < filelist.length; j++) {
        String jcrname = NameCoder.EncodeName(nodelist[i].getName());
        String receivname = filelist[j].isDirectory() ? filelist[j].getName()
            .substring(0, filelist[j].getName().length() - 1) : filelist[j]
            .getName();

        if (jcrname.equals(receivname)) {
          isexist = true;

          // names are equals, so check the types;
          assertEquals(nodelist[i].isNodeType("nt:file"), filelist[j].isFile());
        }
      }
      assertTrue(isexist);
    }

    folder.remove();
    s.save();
  }

}

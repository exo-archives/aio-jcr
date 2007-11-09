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

public class testDeleteFileFolder extends BaseStandaloneTest {
  protected static Log logger = ExoLogger
      .getLogger("jcr.JCRTest.testDeleteFileFolder");

  protected String servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();
  }

  /**
   * Test delete file
   * 
   * @throws Exception
   */
  public void testDeleteExistFile() throws Exception {

    Session s = null;

    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
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

    SmbFile newfile = new SmbFile("smb://" + user + servername + "/ws/" +
        newname);

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

    SmbFile newfile = new SmbFile("smb://" + user + servername + "/ws/" +
        newname);

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

    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);

    String newname = "hhfolder";

    Node cr = s.getRootNode().addNode(newname);

    cr.addNode("subfolder1");
    cr.addNode("subfolder2");

    s.save();

    SmbFile newfile = new SmbFile("smb://" + user + servername + "/ws/" +
        newname + "/");

    assertTrue(newfile.exists());

    newfile.delete();

    assertFalse(newfile.exists());

    assertFalse(s.itemExists("/" + newname));

  }

}

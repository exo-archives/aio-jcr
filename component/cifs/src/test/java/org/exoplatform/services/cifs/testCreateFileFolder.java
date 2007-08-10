/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Session;

import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * This class includes open file an folder tests
 * 
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class testCreateFileFolder extends BaseStandaloneTest {
  protected static Log logger = ExoLogger
      .getLogger("jcr.JCRTest.testCreateFileFolder");

  protected String servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();
  }

  /**
   * Test of create file
   * 
   * @throws Exception
   */
  public void testCreateFile() throws Exception {

    String newname = "newfile.dat";
    SmbFile newfile = new SmbFile("smb://" + servername + "/ws/" + newname);

    // assertFalse(newfile.exists());

    newfile.createNewFile();

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);

    Node n = ((Node) s.getItem("/" + newname));
    assertTrue(n.isNodeType("nt:file")); // check is that realy nt:file

    n.remove();
    s.save();
  }

  /**
   * Test of create folder
   * 
   * @throws Exception
   */
  public void testCreateFolder() throws Exception {

    String newname = "newfolder";
    SmbFile newfile = new SmbFile("smb://" + servername + "/ws/" + newname);

    // assertFalse(newfile.exists());

    newfile.mkdir(); // coreprotocol; Deirectorycreate command not NT_CREATE

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");

    s.refresh(false);

    Node n = ((Node) s.getItem("/" + newname));
    assertFalse(n.isNodeType("nt:file")); // check is that not nt:file node

    n.remove();
    s.save();
  }

  /**
   * Test of create existed folder
   * 
   * @throws Exception
   */
  public void testCreateExistFolder() throws Exception {

    String newname = "newfolder";

    Session s = null;
    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);

    Node root = s.getRootNode();

    root.addNode(newname);
    root.save();

    SmbFile newfile = new SmbFile("smb://" + servername + "/ws/" + newname);

    // assertFalse(newfile.exists());

    try {
      newfile.mkdir(); // coreprotocol; Deirectorycreate command not NT_CREATE
    } catch (jcifs.smb.SmbException e) {
      // here must be exception
      Node n = ((Node) s.getItem("/" + newname));
      assertFalse(n.isNodeType("nt:file")); // check is that not nt:file node

      n.remove();
      s.save();
      return;
    }

    Node n = ((Node) s.getItem("/" + newname));
    assertFalse(n.isNodeType("nt:file")); // check is that not nt:file node

    n.remove();
    s.save();
    fail();

  }

  /**
   * Test of create existed file
   * 
   * @throws Exception
   */
  public void testCreateExistFile() throws Exception {

    Session s = null;

    Credentials credentials = new CredentialsImpl("admin", "admin"
        .toCharArray());
    s = (SessionImpl) (repositoryService.getDefaultRepository()).login(
        credentials, "ws");
    s.refresh(false);

    String newname = "newfile.dat";

    SmbFile newfile = new SmbFile("smb://" + servername + "/ws/" + newname);

    s.getRootNode().addNode(newname);

    s.save();

    // assertFalse(newfile.exists());

    try {
      newfile.createNewFile(); // NT_CREATE
    } catch (jcifs.smb.SmbException e) {
      // here must be excpetion becose file exist
      Node n = ((Node) s.getItem("/" + newname));
      n.remove();
      s.save();
      return;
    }

    Node n = ((Node) s.getItem("/" + newname));
    n.remove();
    s.save();
    fail();

  }

}

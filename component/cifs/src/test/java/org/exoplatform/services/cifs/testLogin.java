/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import java.util.Calendar;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.cifs.server.filesys.NameCoder;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

import jcifs.smb.SmbFile;

/**
 * 
 * There is a test of login to server
 * 
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class testLogin extends BaseStandaloneTest {
  protected static Log logger = ExoLogger.getLogger("jcr.JCRTest.testLogin");

  protected String servername;

  public void setUp() throws Exception {
    super.setUp();

    // get realy used server name, Win32ServerName may not be initialized
    servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName() : serv.getConfiguration()
        .getServerName();
  }

  /**
   * Test connection to server
   * 
   * @throws Exception
   */

  public void testCorrectLogin() throws Exception {
    String user = "exo:exo@";

    long t1 = System.currentTimeMillis();
    SmbFile host = new SmbFile("smb://" + user + servername + "/");
    host.connect();

    long t2 = System.currentTimeMillis() - t1;

    assertEquals(host.getType(), SmbFile.TYPE_SERVER);

    logger.debug(" server connecteed in " + t2 + " ms");
  }

  public void testIncorrectLogin() throws Exception {
    String user = "serg:blabla@";

    SmbFile host = new SmbFile("smb://" + user + servername + "/");
    try {
      host.connect();
    } catch (jcifs.smb.SmbAuthException e) {
      // It's OK here must be exception
      return;
    }
    fail();
  }

  /**
   * Guest login means that there is just login name and no password (or
   * password is useless)
   * 
   * @throws Exception
   */
   public void testGuestLogin() throws Exception {
    String user = "__anonim:blabla@";// login "__anonim" password anystring

    SmbFile host = new SmbFile("smb://" + user + servername + "/ws");

    host.connect();

  }

  public void testGuestLoginNoPass() throws Exception {
    String user = "__anonim:@";// login "__anonim" password anystring

    SmbFile host = new SmbFile("smb://" + user + servername + "/");

    host.connect();

  }
  
  
  public void testConnectDevice() throws Exception {
    String user = "__anonim:@";
    SmbFile share = new SmbFile("smb://" + user + servername + "/ws/");

    long t1 = System.currentTimeMillis();
    share.connect();

    SmbFile[] filelist = share.listFiles();
    long t2 = System.currentTimeMillis() - t1;

    logger.debug("received " + filelist.length + "files on " + t2 + "ms");

  }

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import javax.jcr.Repository;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;

import jcifs.smb.SmbFile;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * <p>
 * 
 * Here is the tests of connection with server.
 * <p> 
 * jCifs used as smb-client
 * 
 */

public class testConnectionToServer extends BaseStandaloneTest {
  protected static Log logger = ExoLogger
      .getLogger("jcr.JCRTest.testConnectionToServer");

  /**
   * Test if server run and available
   * 
   * @throws Exception
   */
  public void testServerRun() throws Exception {
    assertTrue(serv.isServerActive());
  }

  /**
   * Test connection to server
   * 
   * @throws Exception
   */
  public void testConnection() throws Exception {
    // get realy used server name, Win32ServerName may not be initialized
    String servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName()
        : serv.getConfiguration().getServerName();

    long t1 = System.currentTimeMillis();
    SmbFile host = new SmbFile("smb://" + servername + "/");
    long t2 = System.currentTimeMillis() - t1;

    assertEquals(host.getType(), SmbFile.TYPE_SERVER);

    logger.debug(" server connecteed in " + t2 + " ms");
  }

  /**
   * Getting device list (shares) test
   * 
   * @throws Exception
   */
  public void testDeviceList() throws Exception {
    // get realy used server name, Win32ServerName may not be initialized
    String servername = serv.getConfiguration().getWin32ServerName() != null ? serv
        .getConfiguration().getWin32ServerName()
        : serv.getConfiguration().getServerName();

    SmbFile host = new SmbFile("smb://" + servername + "/");

    // Get share list from server and measure spended time
    long t1 = System.currentTimeMillis();
    String[] receivedShares = host.list();
    long t2 = System.currentTimeMillis() - t1;

    // Get workspaces names from repository

    String[] wsList = serv.getConfiguration().getWorkspaceList();

    Repository repo;
    try {
      String repoName = serv.getConfiguration().getRepoName();
      boolean isJndi = serv.getConfiguration().isFromJndi();

      if (repoName == null) {
        repo = repositoryService.getDefaultRepository();
      } else {
        // obtain repository object from JNDI or from eXo Container
        repo = isJndi ? (Repository) new InitialContext().lookup(repoName)
            : repositoryService.getRepository(repoName);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (repo instanceof ManageableRepository) {
      wsList = ((ManageableRepository) repo).getWorkspaceNames();
    } else {
      if (wsList == null) {
        throw new RuntimeException(
            "Non-eXo JCR does not support dynamic workspace list. Please set 'workspaces' "
                + "parameter with comma delimited workspace names available to browsing.");
      }
    }

    assertEquals(receivedShares.length, wsList.length + 1);

    // add to list server's shares, like admine pipes and other.
    // "IPC$" for now
    String[] shares = new String[wsList.length + 1];

    for (int i = 0; i < wsList.length; i++) {
      shares[i] = wsList[i];
    }

    shares[wsList.length] = "IPC$";

    // Check the correlation between correct and received data

    java.util.Arrays.sort(receivedShares);
    java.util.Arrays.sort(shares);

    assertTrue(java.util.Arrays.equals(shares, receivedShares));

    logger.debug("received " + receivedShares.length + " shares in " + t2
        + "ms");
  }

}

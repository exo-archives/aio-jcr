package org.exoplatform.services.cifs;

import org.picocontainer.Startable;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cifs.server.NetworkServer;
import org.exoplatform.services.cifs.smb.server.SMBServer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.cifs.netbios.win32.Win32NetBIOS;

import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL Author : Karpenko Sergey
 * 
 */

public class CIFSServiceImpl implements CIFSService, Startable {

  private Log log = ExoLogger
      .getLogger("org.exoplatform.services.cifs.CIFSServiceImpl");

  // Server configuration
  private ServerConfiguration config = null;

  private RepositoryService repositoryService = null;

  private NetworkServer server;

  public CIFSServiceImpl(InitParams params, RepositoryService rep) {

    repositoryService = rep;

    if (params == null) {
      config = new ServerConfiguration();
    } else {
      config = new ServerConfiguration(params);
    }

  }

  public void start() {
    try {

      if (config.isSMBServerEnabled()) {
        log.info("Starting CIFS service");
        server = new SMBServer(config, repositoryService);
        server.startServer();
      } else {
        log.error("Starting CIFS service error: server not initalized");
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    log.info("Stoping...");
    try {
      if (server != null)
        server.shutdownServer(false);
      repositoryService = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public ServerConfiguration getConfiguration() {
    return config;
  }
}

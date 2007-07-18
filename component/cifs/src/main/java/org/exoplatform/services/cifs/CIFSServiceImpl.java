/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cifs.ServerConfiguration.PlatformType;
import org.exoplatform.services.cifs.server.NetworkServer;
import org.exoplatform.services.cifs.smb.server.SMBServer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Author : Karpenko Sergey
 * 
 */

public class CIFSServiceImpl implements CIFSService, Startable {

  private Log log = ExoLogger
      .getLogger("org.exoplatform.services.cifs.CIFSServiceImpl");

  // Server configuration
  private ServerConfiguration config = null;

  private RepositoryService repositoryService;

  private NetworkServer server;

  public CIFSServiceImpl(InitParams params, RepositoryService repositoryService)
      throws RepositoryException, RepositoryConfigurationException,
      NamingException {

    this.repositoryService = repositoryService;

    config = new ServerConfiguration(params);
  }

  public void start() {

      try {

        if (config.isSMBServerEnabled()) {
          log.info("Starting CIFS service");
          server = new SMBServer(config, repositoryService);
          server.startServer();
          log.info("CIFS service is started server name: "
              + config.getServerName()
              + " on repository: "
              + ((config.getRepoName() == null) ? "default" : config
                  .getRepoName()));
        } else {
          log.error("Starting CIFS service error: server not initalized");
          return;
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
  }

  public void stop() {
    log.info("Stoping...");
    try {
      if (server != null)
        server.shutdownServer(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public ServerConfiguration getConfiguration() {
    return config;
  }

}

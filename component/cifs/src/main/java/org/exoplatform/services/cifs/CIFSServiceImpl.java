/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cifs;

import javax.jcr.RepositoryException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cifs.server.NetworkServer;
import org.exoplatform.services.cifs.smb.server.SMBServer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

/**
 * CIFS service implementation.
 * <p>
 * Created by The eXo Platform SAS Author : Karpenko Sergey
 */
public class CIFSServiceImpl implements CIFSService, Startable {

  private Log                 log    = ExoLogger
                                         .getLogger("org.exoplatform.services.cifs.CIFSServiceImpl");

  /**
   * Server (service) configuration.
   */
  private ServerConfiguration config = null;

  /**
   * Repository service - jcr repository access.
   */
  private RepositoryService   repositoryService;

  /**
   * OrganizationService - security.
   */
  private OrganizationService organizationService;

  /**
   * CIFS server.
   */
  private NetworkServer       server;

  public CIFSServiceImpl(InitParams params, RepositoryService repositoryService,
      OrganizationService organizationService) throws RepositoryException,
      RepositoryConfigurationException, NamingException {

    this.repositoryService = repositoryService;
    this.organizationService = organizationService;
    config = new ServerConfiguration(params);
  }

  /**
   * Is server active.
   * 
   * @return boolean
   */
  public boolean isServerActive() {
    if (server == null)
      return false;

    if (!server.isActive())
      return false;

    return true;
  }

  /**
   * Start service.
   */
  public final void start() {

    try {
      if (config.isSMBServerEnabled()) {
        log.info("Starting CIFS service");
        server = new SMBServer(config, repositoryService, organizationService);
        server.startServer();
        log.info("CIFS service is started server name: " + config.getServerName()
            + " on repository: "
            + ((config.getRepoName() == null) ? "default" : config.getRepoName()));
      } else {
        log.error("Starting CIFS service error: server not initalized");
        return;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Stop service.
   */
  public final void stop() {
    log.info("Stoping...");
    try {
      if (server != null)
        server.shutdownServer(false);
    } catch (Exception e) {
      log.error("Error occured, when server stops :", e);
    }
  }

  /**
   * Get service configuration.
   * 
   * @return ServerConfiguration
   */
  public final ServerConfiguration getConfiguration() {
    return config;
  }
}
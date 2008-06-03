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
package org.exoplatform.services.ftp;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.ftp.config.FtpConfig;
import org.exoplatform.services.ftp.config.FtpConfigImpl;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpServiceImpl implements FtpService, Startable {

  private Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpServiceImpl");
  
  private CommandService commandService = null;
  private RepositoryService repositoryService = null;
  
  private FtpServer ftpServer = null;
  
  private FtpConfig config = null;
  
  public FtpServiceImpl(InitParams params,
      CommandService commandService,
      RepositoryService repositoryService) {
  
    this.commandService = commandService;
    this.repositoryService = repositoryService;
    config = new FtpConfigImpl(params);
  }
  
  public void start() {
    log.info("Start service.");
    try {      
      ftpServer = new FtpServerImpl(config, commandService, repositoryService.getRepository());
      ftpServer.start();
    } catch (Exception e) {
      log.info("Unhandled exception. could not get repository!!!! " + e.getMessage(), e);
    }    
  }

  public void stop() {
    log.info("Stopping...");
    if (ftpServer != null)
      ftpServer.stop();
    else
      log.warn("Service isn't started");
  }

}

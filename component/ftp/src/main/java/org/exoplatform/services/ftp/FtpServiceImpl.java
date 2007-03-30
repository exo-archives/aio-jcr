/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
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
    ftpServer.stop();
  }

}

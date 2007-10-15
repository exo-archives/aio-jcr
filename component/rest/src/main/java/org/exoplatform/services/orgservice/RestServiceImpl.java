/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice;

import java.io.InputStream;

import org.apache.commons.chain.Catalog;
import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.command.RestCommand;
import org.exoplatform.services.orgservice.common.resource.RootResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RestServiceImpl implements RestService {
  
  public static final String COMMANDS_NAME = "/conf/rest-commands.xml";
  public static final String CATALOG_NAME = "REST";
  
  private static Log log = ExoLogger.getLogger("jcr.RestServiceImpl");
  
  private OrganizationService organizationService;
  private Catalog commandCatalog;

  public RestServiceImpl(InitParams params,
      CommandService commandService,
      OrganizationService organizationService) {
    log.info("public RestServiceImpl(InitParams params)");
    
    log.info("Init params: " + params);
    
    this.organizationService = organizationService;
    log.info("Organization Service: " + organizationService);
    
    log.info("COMMAND SERVICE: " + commandService);
    
    commandService = new CommandService();
    
    try {
      InputStream commandStream = getClass().getResourceAsStream(COMMANDS_NAME);
      log.info("COMMAND STREAM: " + commandStream);
      
      commandService.putCatalog(commandStream);
      
      log.info("PUTTED!");
      
      commandCatalog = commandService.getCatalog(CATALOG_NAME);      
      log.info("COMMAND CATALOG: " + commandCatalog);
      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
  }
  
  public RestCommand getCommand(String commandName) {
    log.info("Command Name: " + commandName);
    
    RestCommand restCommand = (RestCommand)commandCatalog.getCommand(commandName);
    
    log.info("REST Command: " + restCommand);
    
    return restCommand;
  }

  public OrganizationService getOrganizationService() {
    return organizationService;
  }
  
}

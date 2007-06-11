/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.chain.Catalog;
import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.config.MappingLoader;
import org.exoplatform.services.webdav.config.PropertyConfigLoader;
import org.exoplatform.services.webdav.config.WebDavConfig;
import org.exoplatform.services.webdav.config.WebDavConfigImpl;
import org.exoplatform.services.webdav.lock.FakeLockTable;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavServiceImpl.java 12853 2007-02-16 16:24:30Z gavrikvetal $
 */

public class WebDavServiceImpl implements WebDavService {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavServiceImpl");
  
  public static final String COMMANDS_PATH = "/org/exoplatform/services/webdav/webdav-commands.xml";
  public static final String MAPPING_PATH = "/conf/webdav-mapping-table.xml";
  public static final String PROPERTYCONFIG_PATH = "/conf/webdav-propertyconfig.xml";
  
  protected RepositoryService  repositoryService;
  protected Catalog commandCatalog;
  protected WebDavConfig config;
  protected FakeLockTable lockTable;
  protected PropertyFactory propertyFactory;
  
  public WebDavServiceImpl (
      InitParams params,
      CommandService commandService, 
      RepositoryService repositoryService,
      OrganizationService organizationService
    ) throws Exception {

    this.repositoryService = repositoryService;
    
    InputStream commandsStream = getClass().getResourceAsStream(COMMANDS_PATH);
    
    commandService.putCatalog(commandsStream);    
    
    commandCatalog = commandService.getCatalog(DavConst.WDBDAV_COMMAND_CATALOG);
    if (commandCatalog == null) {
      CommandService curService = new CommandService();
      InputStream curCommandsStream = getClass().getResourceAsStream(COMMANDS_PATH);
      curService.putCatalog(curCommandsStream);
      
      commandCatalog = curService.getCatalog(DavConst.WDBDAV_COMMAND_CATALOG);
      if (commandCatalog == null) {
        log.info("CAN'T GET COMMAND CATALOG");
      }
    }
    
    config = new WebDavConfigImpl(params);

    lockTable = new FakeLockTable();
    
    initPropertySettings();
  }
  
  private void initPropertySettings() throws Exception {
    InputStream mappingStream = getClass().getResourceAsStream(MAPPING_PATH);
    MappingLoader mappingLoader = new MappingLoader(mappingStream);
    
    InputStream configStream = getClass().getResourceAsStream(PROPERTYCONFIG_PATH);
    PropertyConfigLoader configLoader = new PropertyConfigLoader(configStream);    
    
    propertyFactory = new PropertyFactory(mappingLoader.getMappingTable(), 
        configLoader.getConfigTable());
  }
  
  public WebDavConfig getConfig() {
    return config;
  }

  public ArrayList<String> getAvailableCommands() {
    ArrayList<String> commands = new ArrayList<String>();    
    Iterator<String> commandIter = commandCatalog.getNames();
    while (commandIter.hasNext()) {
      String curCommand = commandIter.next();
      commands.add(curCommand);
    }
    return commands;
  }

  public WebDavCommand getCommand(String commandName) {
    WebDavCommand command = (WebDavCommand)commandCatalog.getCommand(commandName);
    return command;
  }
  
  public ManageableRepository getRepository() throws RepositoryException, RepositoryConfigurationException {
    String repositoryName = config.getRepositoryName();
    if (repositoryName == null) {
      return repositoryService.getCurrentRepository();
    } else {
      return repositoryService.getRepository(repositoryName);
    }
  }
  
  public FakeLockTable getLockTable() {
    return lockTable;
  }
  
  public PropertyFactory getPropertyFactory() {
    return propertyFactory;
  }
  
}

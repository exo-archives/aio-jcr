/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
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
  
  public static final String WEBDAVCONFIG_PATH = "/conf/exo-webdav-config.xml";
  
  protected RepositoryService  repositoryService;
  protected WebDavConfig config;
  protected FakeLockTable lockTable;
  
  public WebDavServiceImpl (
      InitParams params, 
      RepositoryService repositoryService
    ) throws Exception {

    this.repositoryService = repositoryService;
    
    config = new WebDavConfigImpl(params, WEBDAVCONFIG_PATH);

    lockTable = new FakeLockTable();    
  }
  
  public WebDavConfig getConfig() {
    return config;
  }

  public ArrayList<String> getAvailableCommands() {
    ArrayList<String> commands = new ArrayList<String>();
    
    commands.add(WebDavMethod.COPY);
    commands.add(WebDavMethod.DELETE);
    commands.add(WebDavMethod.GET);
    commands.add(WebDavMethod.HEAD);
    commands.add(WebDavMethod.MKCOL);
    commands.add(WebDavMethod.MOVE);
    commands.add(WebDavMethod.OPTIONS);
    commands.add(WebDavMethod.PROPFIND);
    commands.add(WebDavMethod.PROPPATCH);
    commands.add(WebDavMethod.PUT);

    return commands;
  }

  public ManageableRepository getRepository() throws RepositoryException {
    
    try {
      String repositoryName = config.getRepositoryName();
      if (repositoryName == null) {
        return repositoryService.getCurrentRepository();
      } else {
        return repositoryService.getRepository(repositoryName);
      }      
    } catch (RepositoryConfigurationException rexc) {
      log.info("Unhandled exception. " + rexc.getMessage(), rexc);
      throw new RepositoryException(rexc.getMessage());
    }
    
  }
  
  public FakeLockTable getLockTable() {
    return lockTable;
  }
  
}

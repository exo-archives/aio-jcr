/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.config.WebDavConfig;
import org.exoplatform.services.webdav.lock.FakeLockTable;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavService.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public interface WebDavService {

  WebDavConfig getConfig();
  
  ArrayList<String> getAvailableCommands();
  
  WebDavCommand getCommand(String commandName);
  
  ManageableRepository getRepository() throws RepositoryException, RepositoryConfigurationException;
  
  //WebDavSessionProvider getSessionProvider();
  
  FakeLockTable getLockTable();
  
  PropertyFactory getPropertyFactory();
  
}

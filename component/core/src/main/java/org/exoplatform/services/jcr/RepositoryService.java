/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SARL        .<br/>
 * The repository service
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @author <a href="mailto:benjamin.mestrallet@exoplatform.com">Benjamin Mestrallet</a>
 * @version $Id: RepositoryService.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface RepositoryService {
  
  /**
   * @return default repository
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  ManageableRepository getRepository() throws RepositoryException, RepositoryConfigurationException;

  /**
   * @param name
   * @return repository by name
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  ManageableRepository getRepository(String name) throws RepositoryException, RepositoryConfigurationException;

  /**
   * @return RepositoryServiceConfiguration
   */
  RepositoryServiceConfiguration getConfig();
}

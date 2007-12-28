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
package org.exoplatform.services.jcr;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS.<br/> The repository service
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @author <a href="mailto:benjamin.mestrallet@exoplatform.com">Benjamin
 *         Mestrallet</a>
 * @version $Id: RepositoryService.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface RepositoryService {

  /**
   * @return default repository
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  ManageableRepository getDefaultRepository() throws RepositoryException,
      RepositoryConfigurationException;

  /**
   * @deprecated use getDefaultRepository() instead
   */
  ManageableRepository getRepository() throws RepositoryException, RepositoryConfigurationException;

  /**
   * @param name
   * @return repository by name
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  ManageableRepository getRepository(String name) throws RepositoryException,
      RepositoryConfigurationException;

  /**
   * @return
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  ManageableRepository getCurrentRepository() throws RepositoryException;

  /**
   * @param repositoryName
   * @throws RepositoryConfigurationException
   */
  void setCurrentRepositoryName(String repositoryName) throws RepositoryConfigurationException;

  /**
   * @return RepositoryServiceConfiguration
   */
  RepositoryServiceConfiguration getConfig();
  /**
   * Create new repository 
   * @param repositoryEntry
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  void createRepository(RepositoryEntry repositoryEntry) throws RepositoryConfigurationException,
      RepositoryException;
  /**
   * Remove repository with name repositoryName
   * @param repositoryName
   * @throws RepositoryException
   */
  void removeRepository(String repositoryName) throws RepositoryException;
  /**
   * Indicates if repository with name repositoryName can be removed
   * @param repositoryName
   * @return
   * @throws RepositoryException
   */
  boolean canRemoveRepository(String  repositoryName) throws RepositoryException ;
}

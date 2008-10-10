/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.organization;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Initialization will be performed via OrganizationServiceJCRInitializer. <br/>Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class JCROrganizationServiceImpl extends BaseOrganizationService implements Startable {

  public static final String           STORAGE_WORKSPACE    = "storage-workspace";

  public static final String           STORAGE_PATH         = "storage-path";

  public static final String           STORAGE_PATH_DEFAULT = "/exo:organization";

  protected final ManageableRepository repository;

  protected final String               storageWorkspace;

  protected final String               storagePath;

  public JCROrganizationServiceImpl(RepositoryService repositoryService, InitParams params) throws ConfigurationException,
      RepositoryException,
      RepositoryConfigurationException {
    // TODO Searching Repository Content should be enabled

    String storageWorkspace = params.getValueParam(STORAGE_WORKSPACE).getValue();
    String storagePath = params.getValueParam(STORAGE_PATH).getValue();

    repository = repositoryService.getDefaultRepository();

    if (storageWorkspace != null)
      this.storageWorkspace = storageWorkspace;
    else
      // use default
      this.storageWorkspace = repository.getConfiguration().getDefaultWorkspaceName();

    if (storagePath != null) {
      if (storagePath.equals("/"))
        throw new ConfigurationException(STORAGE_PATH + " can not be a root node");

      this.storagePath = storagePath;
    } else
      this.storagePath = STORAGE_PATH_DEFAULT;

    // create /exo:organization
    Session session = getStorageSession();
    try {
      session.getItem(this.storagePath);
      // if found do nothing, the storage was initialized before.
    } catch (PathNotFoundException e) {
      // will create new
      Node storage = session.getRootNode().addNode(this.storagePath.substring(1),
                                                   "exo:organizationStorage");

      storage.addNode(UserHandlerImpl.STORAGE_EXO_USERS, "exo:organizationUsers");
      storage.addNode(GroupHandlerImpl.STORAGE_EXO_GROUPS, "exo:organizationGroups");
      storage.addNode(MembershipTypeHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPES,
                      "exo:organizationMembershipTypes");

      session.save(); // storage done
    } finally {
      session.logout();
    }

    // create DAO object
    userDAO_ = new UserHandlerImpl(this);
    userProfileDAO_ = new UserProfileHandlerImpl(this);
    groupDAO_ = new GroupHandlerImpl(this);
    membershipDAO_ = new MembershipHandlerImpl(this);
    membershipTypeDAO_ = new MembershipTypeHandlerImpl(this);
  }

  /**
   * Return system Session to org-service storage workspace. For internal use only.
   * 
   * @return
   * @throws RepositoryException
   */
  Session getStorageSession() throws RepositoryException {
    return repository.getSystemSession(storageWorkspace);
  }

  /**
   * Return org-sergvice actual storage path.
   * 
   * @return
   */
  String getStoragePath() {
    return storagePath;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    // TODO Auto-generated method stub
    super.start();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // do nothing
    super.stop();
  }

  
}

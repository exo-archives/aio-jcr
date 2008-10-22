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

import org.picocontainer.Startable;

import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.organization.BaseOrganizationService;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Initialization will be performed via OrganizationServiceJCRInitializer. <br/>Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class JCROrganizationServiceImpl extends BaseOrganizationService implements Startable {

  public static final String     STORAGE_PATH         = "storage-path";

  public static final String     STORAGE_PATH_DEFAULT = "/exo:organization";

  public static final String     STORAGE_WORKSPACE    = "storage-workspace";

  protected ManageableRepository repository;

  protected RepositoryService    repositoryService;

  protected String               storagePath;

  protected String               storageWorkspace;

  public JCROrganizationServiceImpl(RepositoryService repositoryService, InitParams params) throws ConfigurationException {
    // TODO Searching Repository Content should be enabled
    String workspace = params.getValueParam(STORAGE_WORKSPACE).getValue();

    ValueParam paramStoragePath = params.getValueParam(STORAGE_PATH);
    String path = paramStoragePath != null ? paramStoragePath.getValue() : null;

    if (path != null) {
      if (path.equals("/")) {
        throw new ConfigurationException(STORAGE_PATH + " can not be a root node");
      }

      this.storagePath = path;
    } else {
      this.storagePath = STORAGE_PATH_DEFAULT;
    }

    this.storageWorkspace = workspace;
    this.repositoryService = repositoryService;

    // create DAO object
    userDAO_ = new UserHandlerImpl(this);
    userProfileDAO_ = new UserProfileHandlerImpl(this);
    groupDAO_ = new GroupHandlerImpl(this);
    membershipDAO_ = new MembershipHandlerImpl(this);
    membershipTypeDAO_ = new MembershipTypeHandlerImpl(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    try {
      repository = repositoryService.getDefaultRepository();
    } catch (Exception e) {
      throw new RuntimeException("Can not get default repository", e);
    }

    if (storageWorkspace == null) {
      storageWorkspace = repository.getConfiguration().getDefaultWorkspaceName();
    }

    // create /exo:organization
    try {
      Session session = getStorageSession();
      try {
        session.getItem(this.storagePath);
        // if found do nothing, the storage was initialized before.
      } catch (PathNotFoundException e) {
        // will create new
        Node storage = session.getRootNode().addNode(storagePath.substring(1),
                                                     "exo:organizationStorage");

        storage.addNode("exo:users", "exo:organizationUsers");
        storage.addNode("exo:groups", "exo:organizationGroups");
        storage.addNode("exo:membershipTypes", "exo:organizationMembershipTypes");

        session.save(); // storage done configure

      } finally {
        session.logout();
      }
    } catch (Exception e) {
      throw new RuntimeException("Can not configure storage", e);
    }

    super.start();

  }

  /**
   * Return org-sergvice actual storage path.
   * 
   * @return String with path
   */
  String getStoragePath() {
    return storagePath;
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

}

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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.apache.commons.logging.Log;

import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.log.ExoLogger;
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

  /**
   * The name of parameter that contain storage path.
   */
  public static final String     STORAGE_PATH         = "storage-path";

  /**
   * The name of parameter that contain workspace name.
   */
  public static final String     STORAGE_WORKSPACE    = "storage-workspace";

  /**
   * Default storage path.
   */
  public static final String     STORAGE_PATH_DEFAULT = "/exo:organization";

  /**
   * The service's name.
   */
  private static final String    SERVICE_NAME         = "JCROrganization";

  /**
   * Manageable repository.
   */
  protected ManageableRepository repository;

  /**
   * Repository service.
   */
  protected RepositoryService    repositoryService;

  /**
   * Registry service.
   */
  protected RegistryService      registryService;

  /**
   * Contain passed value of storage path in parameters.
   */
  protected String               storagePath;

  /**
   * Contain passed value of workspace name in parameters.
   */
  protected String               storageWorkspace;

  /**
   * Initialization parameters.
   */
  protected InitParams           initParams;

  /**
   * Logger.
   */
  private static Log             log                  = ExoLogger.getLogger("jcr.JCROrganizationService");

  /**
   * JCROrganizationServiceImpl constructor. Without registry service.
   * 
   * @param params
   *          The initialization parameters
   * @param repositoryService
   *          The repository service
   * @throws ConfigurationException
   *           The exception is thrown if can not initialize service
   */
  public JCROrganizationServiceImpl(InitParams params, RepositoryService repositoryService) throws ConfigurationException {
    this(params, repositoryService, null);
  }

  /**
   * JCROrganizationServiceImpl constructor.
   * 
   * @param params
   *          The initialization parameters
   * @param repositoryService
   *          The repository service
   * @param registryService
   *          The registry service
   * @throws ConfigurationException
   *           The exception is thrown if can not initialize service
   */
  public JCROrganizationServiceImpl(InitParams initParams,
                                    RepositoryService repositoryService,
                                    RegistryService registryService) throws ConfigurationException {
    // TODO Searching Repository Content should be enabled
    this.repositoryService = repositoryService;
    this.registryService = registryService;

    if (initParams == null)
      throw new ConfigurationException("Init parameters expected !!!");

    this.initParams = initParams;

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
    if (log.isDebugEnabled()) {
      log.debug("Starting JCROrganizationService");
    }

    try {
      repository = repositoryService.getDefaultRepository();
    } catch (Exception e) {
      throw new RuntimeException("Can not get default repository", e);
    }

    if (registryService != null) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();

      try {
        // reading parameters from registryService
        String entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME;
        RegistryEntry entry = registryService.getEntry(sessionProvider, entryPath);

        Document doc = entry.getDocument();

        storageWorkspace = doc.getElementsByTagName("organization.workspace")
                              .item(0)
                              .getTextContent();
        storagePath = doc.getElementsByTagName("organization.path").item(0).getTextContent();

        log.info("Workspace from RegistryService: " + storageWorkspace);
        log.info("Root node from RegistryService: " + storagePath);

      } catch (PathNotFoundException e) {
        // reading parameters from file
        getParamsFromConfigurationFile();

        try {
          // writing to RegistryService
          Document doc = createInitConf(storageWorkspace, storagePath);
          RegistryEntry serviceEntry = new RegistryEntry(doc);
          registryService.createEntry(sessionProvider, RegistryService.EXO_SERVICES, serviceEntry);

        } catch (RepositoryException exc) {
          log.error("Cannot write init configuration to RegistryService.", exc);
        } catch (ParserConfigurationException exc) {
          log.error("Cann't create XML document", exc);
        }

      } catch (RepositoryException e) {
      } finally {
        sessionProvider.close();
      }
    } else {
      getParamsFromConfigurationFile();
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
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    super.stop();
  }

  /**
   * Return org-sergvice actual storage path.
   * 
   * @return
   * @throws RepositoryException
   */
  String getStoragePath() throws RepositoryException {
    if (storagePath == null) {
      throw new RepositoryException("Can not get storage path because JCROrganizationService is not started");
    }

    return storagePath;
  }

  /**
   * Return system Session to org-service storage workspace. For internal use only.
   * 
   * @return
   * @throws RepositoryException
   */
  Session getStorageSession() throws RepositoryException {
    try {
      return repository.getSystemSession(storageWorkspace);
    } catch (NullPointerException e) {
      throw new RepositoryException("Can not get system session because JCROrganizationService is not started",
                                    e);
    }
  }

  /**
   * Store parameters into RegistryService.
   * 
   * @param workspace
   *          The name of storage workspace
   * @param path
   *          The name of storage path
   * @return
   * @throws ParserConfigurationException
   */
  private Document createInitConf(String workspace, String path) throws ParserConfigurationException {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = doc.createElement(SERVICE_NAME);
    doc.appendChild(root);

    // Name of the workspace
    Element nameElement = doc.createElement("organization.workspace");
    nameElement.setAttribute("id", "workspace");
    Text nameText = doc.createTextNode(workspace);
    nameElement.appendChild(nameText);
    root.appendChild(nameElement);

    // Set path to internal root node
    Element descriptionElement = doc.createElement("organization.path");
    descriptionElement.setAttribute("id", "path");
    Text descriptionText = doc.createTextNode(path);
    descriptionElement.appendChild(descriptionText);
    root.appendChild(descriptionElement);

    return doc;
  }

  /**
   * Get parameters which passed from the file.
   */
  private void getParamsFromConfigurationFile() {
    storageWorkspace = initParams.getValueParam(STORAGE_WORKSPACE).getValue();
    ValueParam paramStoragePath = initParams.getValueParam(STORAGE_PATH);
    storagePath = paramStoragePath != null ? paramStoragePath.getValue() : null;

    if (storagePath != null) {
      if (storagePath.equals("/")) {
        throw new RuntimeException("Storage path can not be a root node");
      }
    } else {
      this.storagePath = STORAGE_PATH_DEFAULT;
    }

    if (storageWorkspace == null) {
      storageWorkspace = repository.getConfiguration().getDefaultWorkspaceName();
    }

    log.info("Workspace from configuration file: " + storageWorkspace);
    log.info("Root node from configuration file: " + storagePath);
  }

}

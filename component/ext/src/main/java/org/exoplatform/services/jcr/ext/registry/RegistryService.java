/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.registry;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL . <br/> Centralized collector for JCR based
 * entities (services, apps, users) It contains info about the whole system,
 * i.e. for all repositories used by system. All operations performed in context
 * of "current" repository, i.e. RepositoryService.getCurrentRepository() Each
 * repository has own Registry storage which is placed in workspace configured
 * in "locations" entry like: <properties-param> <name>locations</name>
 * <description>registry locations</description> <property name="repository1"
 * value="workspace1"/> <property name="repository2" value="workspace2"/> The
 * implementation hides storage details from end user
 * 
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class RegistryService extends Registry implements Startable {

  private static Log log = ExoLogger.getLogger("jcr.RegistryService");

  protected final static String EXO_REGISTRY_NT = "exo:registry";

  protected final static String EXO_REGISTRYENTRY_NT = "exo:registryEntry";

  protected final static String EXO_REGISTRYGROUP_NT = "exo:registryGroup";

  protected final static String NT_FILE = "registry-nodetypes.xml";

  protected final static String EXO_REGISTRY = "exo:registry";

  protected final static String EXO_REGISTRYENTRY = "exo:registryEntry";

  public final static String EXO_SERVICES = "exo:services";

  public final static String EXO_APPLICATIONS = "exo:applications";

  public final static String EXO_USERS = "exo:users";

  protected final Map<String, String> regWorkspaces;

  protected final RepositoryService repositoryService;

  /**
   * @param params
   *          accepts "locations" properties param
   * @param repositoryService
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   * @throws FileNotFoundException
   */
  public RegistryService(InitParams params, RepositoryService repositoryService)
      throws RepositoryConfigurationException {

    this.repositoryService = repositoryService;
    this.regWorkspaces = new HashMap<String, String>();
    if (params == null)
      throw new RepositoryConfigurationException("Init parameters expected");
    PropertiesParam props = params.getPropertiesParam("locations");
    if (props == null)
      throw new RepositoryConfigurationException(
          "Property parameters 'locations' expected");
    for (RepositoryEntry repConfiguration : repConfigurations()) {
      String repName = repConfiguration.getName();
      String wsName = null;
      if (props != null) {
        wsName = props.getProperty(repName);
        if (wsName == null)
          wsName = repConfiguration.getDefaultWorkspaceName();
      } else {
        wsName = repConfiguration.getDefaultWorkspaceName();
      }
      addRegistryLocation(repName, wsName);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.registry.Registry#getRegistryEntry(
   *      org.exoplatform.services.jcr.ext.common.SessionProvider,
   *      java.lang.String, java.lang.String,
   *      org.exoplatform.services.jcr.core.ManageableRepository)
   */
  public RegistryEntry getEntry(SessionProvider sessionProvider, String groupName,
      String entryName) throws RepositoryConfigurationException,
      RepositoryException {

//    Node root = rootNode(sessionProvider, repositoryService
//        .getCurrentRepository());
//    String rootPath = (root.getPath().endsWith("/")) ? root.getPath() : root
//        .getPath()
//        + "/";
    String relPath = EXO_REGISTRY + "/" + groupName + "/" + entryName;
    Session session = session(sessionProvider, repositoryService.getCurrentRepository());
    if (session.getRootNode().hasNode(relPath)) {
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        session.exportDocumentView("/" + relPath, out, true,
            false);
        return RegistryEntry.parse(out.toByteArray());
      } catch (IOException e) {
        throw new RepositoryException(
            "Can't export node to XML representation " + e);
      } catch (ParserConfigurationException e) {
        throw new RepositoryException(
            "Can't export node to XML representation " + e);
      } catch (SAXException e) {
        throw new RepositoryException(
            "Can't export node to XML representation " + e);
      }
    }
    throw new ItemNotFoundException("Item not found " + relPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.registry.Registry#createEntry(
   *      org.exoplatform.services.jcr.ext.common.SessionProvider,
   *      java.lang.String, org.w3c.dom.Document)
   */
  public void createEntry(SessionProvider sessionProvider,
  		String groupName, RegistryEntry entry) throws RepositoryConfigurationException,
  		RepositoryException {
  	
// Node root = rootNode(sessionProvider,
// repositoryService.getCurrentRepository());
// String rootPath = (root.getPath().endsWith("/")) ? root.getPath() :
// root.getPath() + "/";
// String relPath = EXO_REGISTRY + "/" + groupName;
    String path = "/" + EXO_REGISTRY + "/" + groupName;
    try {
      session(sessionProvider, repositoryService.getCurrentRepository()).
      getWorkspace().importXML(path, entry.getAsInputStream(), 1);
// root.getSession().importXML(path,
// new ByteArrayInputStream(out.toByteArray()), 1);
// root.getNode(relPath).save();
    } catch(IOException ioe) {
      throw new RepositoryException("Item " + path + "can't be created");
    } catch(ItemExistsException iee) {
      throw new RepositoryException("Item " + path + "alredy exists");
    } catch(TransformerException te) {
      throw new RepositoryException("Can't get XML representation from stream");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.registry.Registry#removeEntry(
   *      org.exoplatform.services.jcr.ext.common.SessionProvider,
   *      java.lang.String, java.lang.String)
   */
  public void removeEntry(SessionProvider sessionProvider, String groupName,
      String entryName) throws RepositoryException,
      RepositoryConfigurationException {

    String relPath = EXO_REGISTRY + "/" + groupName + "/" + entryName;
    Node root = session(sessionProvider, repositoryService
        .getCurrentRepository()).getRootNode();
    if (!root.hasNode(relPath))
      throw new ItemNotFoundException("Item not found " + relPath);
    Node node = root.getNode(relPath);
    Node parent = node.getParent();
    node.remove();
    parent.save();
  }

  /**
   * @param sessionProvider
   * @param groupName
   * @param entry
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   * @throws TransformerException
   */
  public void recreateEntry(SessionProvider sessionProvider, String groupName,
      RegistryEntry entry) throws RepositoryConfigurationException,
      RepositoryException {

    //String exEntry = entry.getDocumentElement().getNodeName();
    removeEntry(sessionProvider, groupName, entry.getName());
    createEntry(sessionProvider, groupName, entry);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.registry.Registry#getRegistry(org.exoplatform.services.jcr.ext.common.SessionProvider,
   *      org.exoplatform.services.jcr.core.ManageableRepository)
   */
  public RegistryNode getRegistry(SessionProvider sessionProvider)
      throws RepositoryConfigurationException, RepositoryException {
    return new RegistryNode(session(sessionProvider,
        repositoryService.getCurrentRepository()).getRootNode()
        .getNode(EXO_REGISTRY));
  }

//  private Node rootNode(SessionProvider sessionProvider,
//      ManageableRepository repo) throws RepositoryException {
//
//    String repName = repo.getConfiguration().getName();
//    Session session = sessionProvider.getSession(regWorkspaces.get(repName),
//        repo);
//    return session.getRootNode();
//  }

  private Session session(SessionProvider sessionProvider,
      ManageableRepository repo) throws RepositoryException {

    String repName = repo.getConfiguration().getName();
    return sessionProvider.getSession(regWorkspaces.get(repName),
        repo);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    try {
      InputStream xml = getClass().getResourceAsStream(NT_FILE);
      for (RepositoryEntry repConfiguration : repConfigurations()) {
        String repName = repConfiguration.getName();
        repositoryService.getRepository(repName).getNodeTypeManager()
            .registerNodeTypes(xml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
      }
      initStorage(false);
    } catch (RepositoryConfigurationException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
  }

  /**
   * @param replace
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public void initStorage(boolean replace)
      throws RepositoryConfigurationException, RepositoryException {
    for (RepositoryEntry repConfiguration : repConfigurations()) {
      String repName = repConfiguration.getName();
      ManageableRepository rep = repositoryService.getRepository(repName);
      Session sysSession = rep.getSystemSession(regWorkspaces.get(repName));

      if (sysSession.getRootNode().hasNode(EXO_REGISTRY) && replace)
        sysSession.getRootNode().getNode(EXO_REGISTRY).remove();

      if (!sysSession.getRootNode().hasNode(EXO_REGISTRY)) {
        Node rootNode = sysSession.getRootNode().addNode(EXO_REGISTRY,
            EXO_REGISTRY_NT);
        rootNode.addNode(EXO_SERVICES, EXO_REGISTRYGROUP_NT);
        rootNode.addNode(EXO_APPLICATIONS, EXO_REGISTRYGROUP_NT);
        rootNode.addNode(EXO_USERS, EXO_REGISTRYGROUP_NT);
        sysSession.save();
      }
      sysSession.logout();
    }
  }

  /**
   * @param repositoryName
   * @param workspaceName
   */
  public void addRegistryLocation(String repositoryName, String workspaceName) {
    regWorkspaces.put(repositoryName, workspaceName);
  }

  /**
   * @param repositoryName
   */
  public void removeRegistryLocation(String repositoryName) {
    regWorkspaces.remove(repositoryName);
  }

  /**
   * @param sessionProvider
   * @param groupName
   * @param entryName
   * @param repositoryName
   * @return
   * @throws RepositoryException
   */
  public void initRegistryEntry(String groupName, String entryName)
      throws RepositoryException, RepositoryConfigurationException {

    String relPath = EXO_REGISTRY + "/" + groupName + "/" + entryName;
    for (RepositoryEntry repConfiguration : repConfigurations()) {
      String repName = repConfiguration.getName();
      SessionProvider sysProvider = SessionProvider.createSystemProvider();
      Node root = session(sysProvider, repositoryService
          .getRepository(repName)).getRootNode();
      if (!root.hasNode(relPath)) {
        root.addNode(relPath, EXO_REGISTRYENTRY_NT);
        root.save();
      } else {
        log.info("The RegistryEntry " + relPath
            + "is already initialized on repository " + repName);
      }
      sysProvider.close();
    }
  }

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  private List<RepositoryEntry> repConfigurations() {
    return (List<RepositoryEntry>) repositoryService.getConfig()
        .getRepositoryConfigurations();
  }

}

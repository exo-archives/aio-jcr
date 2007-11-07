/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.registry;

import static javax.jcr.ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SAS . <br/> 
 * Centralized collector for JCR based entities (services, apps, users) 
 * It contains info about the whole system, i.e. for all repositories used by system. 
 * All operations performed in context of "current" repository, i.e. 
 * RepositoryService.getCurrentRepository() Each repository has own Registry storage 
 * which is placed in workspace configured in "locations" entry like: 
 * <properties-param> <name>locations</name>
 * <description>registry locations</description> 
 * <property name="repository1" value="workspace1"/> 
 * <property name="repository2" value="workspace2"/> 
 * The implementation hides storage details from end user
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
  
  public final static String EXO_GROUPS = "exo:groups";

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
      throw new RepositoryConfigurationException("Property parameters 'locations' expected");
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
  public RegistryEntry getEntry(SessionProvider sessionProvider, 
      String entryPath) throws ItemNotFoundException, RepositoryException {

    String relPath = EXO_REGISTRY + "/" + entryPath;
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
    } else
      throw new ItemNotFoundException("Entry not found " + relPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.registry.Registry#createEntry(
   *      org.exoplatform.services.jcr.ext.common.SessionProvider,
   *      java.lang.String, org.w3c.dom.Document)
   */
  public void createEntry(SessionProvider sessionProvider,
  		String groupPath, RegistryEntry entry) throws RepositoryException {
  	
    String fullPath = "/" + EXO_REGISTRY + "/" + groupPath;
    try {
    	checkGroup(sessionProvider, groupPath);
      session(sessionProvider, repositoryService.getCurrentRepository()).
      getWorkspace().importXML(fullPath, entry.getAsInputStream(), IMPORT_UUID_CREATE_NEW);
    } catch(IOException ioe) {
      throw new RepositoryException("Item " + fullPath + "can't be created "+ioe);
    } catch(ItemExistsException iee) {
      throw new RepositoryException("Item " + fullPath + "alredy exists "+iee);
    } catch(TransformerException te) {
      throw new RepositoryException("Can't get XML representation from stream "+te);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.registry.Registry#removeEntry(
   *      org.exoplatform.services.jcr.ext.common.SessionProvider,
   *      java.lang.String, java.lang.String)
   */
  public void removeEntry(SessionProvider sessionProvider, String entryPath)
      throws RepositoryException {

    String relPath = EXO_REGISTRY + "/" + entryPath;
    Node root = session(sessionProvider, repositoryService
        .getCurrentRepository()).getRootNode();
    if (!root.hasNode(relPath))
      throw new ItemNotFoundException("Item not found " + relPath);
    Node node = root.getNode(relPath);
    Node parent = node.getParent();
    node.remove();
    parent.save();
  }

  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.registry.Registry#recreateEntry(org.exoplatform.services.jcr.ext.common.SessionProvider, java.lang.String, org.exoplatform.services.jcr.ext.registry.RegistryEntry)
   */
  public void recreateEntry(SessionProvider sessionProvider, String groupPath,
      RegistryEntry entry) throws RepositoryException {

    removeEntry(sessionProvider, groupPath + "/" + entry.getName());
    createEntry(sessionProvider, groupPath, entry);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.registry.Registry#getRegistry(org.exoplatform.services.jcr.ext.common.SessionProvider,
   *      org.exoplatform.services.jcr.core.ManageableRepository)
   */
  public RegistryNode getRegistry(SessionProvider sessionProvider)
      throws RepositoryException {
    
    return new RegistryNode(session(sessionProvider,
        repositoryService.getCurrentRepository()).getRootNode()
        .getNode(EXO_REGISTRY));
  }

  /**
   * @param sessionProvider
   * @param repo
   * @return session
   * @throws RepositoryException
   */
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
      
      for (RepositoryEntry repConfiguration : repConfigurations()) {
        InputStream xml = getClass().getResourceAsStream(NT_FILE);
        String repName = repConfiguration.getName();
        repositoryService.getRepository(repName).getNodeTypeManager()
            .registerNodeTypes(xml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
        xml.close();
      }
      initStorage(false);
    } catch (RepositoryConfigurationException e) {
      log.error(e.getLocalizedMessage());
      e.printStackTrace();
    } catch (RepositoryException e) {
      log.error(e.getLocalizedMessage());
      e.printStackTrace();
    } catch (IOException e) {
      log.error(e.getLocalizedMessage());
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
        rootNode.addNode(EXO_GROUPS, EXO_REGISTRYGROUP_NT);
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

  /**
   * @return repository service
   */
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  /**
   * @return repository entries 
   */
  private List<RepositoryEntry> repConfigurations() {
    return (List<RepositoryEntry>) repositoryService.getConfig()
        .getRepositoryConfigurations();
  }

  /**
   * check if group exists and creates one if necessary
   * @param sessionProvider
   * @param groupPath
   * @throws RepositoryException
   */
  private void checkGroup(SessionProvider sessionProvider, String groupPath) 
  throws RepositoryException {
  	String[] groupNames = groupPath.split("/");
  	String prefix = "/" + EXO_REGISTRY;
  	Session session = session(sessionProvider, repositoryService
		    .getCurrentRepository());
  	for(String name : groupNames) {
  		String path = prefix + "/" + name;  
  		try {
  			Node group = (Node)session.getItem(path);
  			if(!group.isNodeType(EXO_REGISTRYGROUP_NT))
  				throw new RepositoryException("Node at "+path+" should be  " + EXO_REGISTRYGROUP_NT + " type");
			} catch (PathNotFoundException e) {
				Node parent = (Node)session.getItem(prefix);
				parent.addNode(name, EXO_REGISTRYGROUP_NT);
				parent.save();
			}
			prefix = path;
  	}
  }
  
  
}

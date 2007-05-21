/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.registry;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Created by The eXo Platform SARL        . <br/>
 * Centralized collector for JCR based entities (services, apps, users)
 * It contains info about the whole system, i.e. for all repositories
 * used by system. 
 * 
 * Each repository has own Registry storage which is placed
 * in workspace configured in "locations" entry like:
 *   <properties-param>
 *      <name>locations</name>
 *      <description>registry locations</description>
 *      <property name="repository1" value="workspace1"/>
 *      <property name="repository2" value="workspace2"/>
 *      
 * The implementation hides storage details from end user       
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class RegistryService implements Startable {
  
  private static Log log = ExoLogger.getLogger("jcr.RegistryService");
  
  protected final static String EXO_REGISTRY_NT = "exo:registry";
  protected final static String EXO_REGISTRYENTRY_NT = "exo:registryEntry";
  protected final static String EXO_REGISTRYGROUP_NT = "exo:registryGroup";
  protected final static String NT_FILE = "registry-nodetypes.xml";

  protected final static String EXO_REGISTRY = "exo:registry";
  protected final static String EXO_REGISTRYENTRY = "exo:registryEntry";
  

  protected final static String EXO_SERVICES = "exo:services";
  protected final static String EXO_APPLICATIONS = "exo:applications";
  protected final static String EXO_USERS = "exo:users";
  
  protected final Map <String, String> regWorkspaces;
  protected final RepositoryService repositoryService;
  
  /**
   * @param params accepts "locations" properties param
   * @param repositoryService
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   * @throws FileNotFoundException
   */
  public RegistryService(InitParams params, RepositoryService repositoryService) 
  throws RepositoryConfigurationException, RepositoryException {
    this.repositoryService = repositoryService;
    this.regWorkspaces = new HashMap<String, String>();
    if(params == null)
      throw new RepositoryConfigurationException("Init parameters expected");
    PropertiesParam props = params.getPropertiesParam("locations");
    if(params == null)
      throw new RepositoryConfigurationException("Property parameters 'locations' expected");

    for(RepositoryEntry repConfiguration : repConfigurations()){
      String repName = repConfiguration.getName();
      String wsName = null;
      if(props != null) {
        wsName = props.getProperty(repName);
        if(wsName == null)
          wsName = repConfiguration.getDefaultWorkspaceName();
      } else {
        wsName = repConfiguration.getDefaultWorkspaceName();
      }
      addRegistryLocation(repName, wsName);
    }
  }
  
  
  /**
   * Returns existed or newly created RegistryEntry which wraps Node of "exo:registryEntry" type  
   * @param sessionProvider
   * @param entryType
   * @param entryName
   * @return
   * @throws RepositoryException
   */
  public RegistryEntryNode getRegistryEntry(SessionProvider sessionProvider, String entryType,
      String entryName, ManageableRepository repository) throws RepositoryException {
    String relPath = EXO_REGISTRY+"/"+entryType+"/"+entryName;
    Node root = rootNode(sessionProvider, repository);
    if(!root.hasNode(relPath)) {
      return new RegistryEntryNode(root.addNode(relPath, EXO_REGISTRYENTRY_NT));
    } else {
      return new RegistryEntryNode(root.getNode(relPath));
    }
  }
  
  /**
   * Returns Registry object which wraps Node of "exo:registry" type
   * (the whole registry tree)  
   * @param sessionProvider
   * @return
   * @throws RepositoryException
   */
  public RegistryNode getRegistry(SessionProvider sessionProvider, ManageableRepository repository) 
    throws RepositoryException{
    return new RegistryNode(rootNode(sessionProvider, repository).getNode(EXO_REGISTRY));
  } 
  

  /**
   * Registers entry (saves the node)  
   * @param entry
   * @throws RepositoryException
   */
  public void register(RegistryEntryNode entry) throws RepositoryException {
    Node node = entry.getNode();
    node.getParent().save();
  }
  
  /**
   * Unregisters entry
   * @param sessionProvider
   * @param entryType
   * @param entryName
   * @throws RepositoryException
   */
  public void unregister(SessionProvider sessionProvider, String entryType,
      String entryName, ManageableRepository repository) throws RepositoryException {
    String relPath = EXO_REGISTRY+"/"+entryType+"/"+entryName;
    Node root = rootNode(sessionProvider, repository);
    Node node = root.getNode(relPath);
    Node parent = node.getParent();
    node.remove();
    parent.save();
  }

  
  private Node rootNode(SessionProvider sessionProvider, ManageableRepository repository) throws RepositoryException {
    String repName = repository.getConfiguration().getName();
    Session session = sessionProvider.getSession(regWorkspaces.get(repName), repository);
    return session.getRootNode();
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    
    try {
      InputStream xml = getClass().getResourceAsStream(NT_FILE);

      for(RepositoryEntry repConfiguration : repConfigurations()){
        String repName = repConfiguration.getName();
        repositoryService.getRepository(repName).
        getNodeTypeManager().registerNodeTypes(
            xml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
      }

      initStorage(false);
    } catch (RepositoryConfigurationException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }  
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
  }

  /**
   * @param replace
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public void initStorage(boolean replace) throws RepositoryConfigurationException, RepositoryException {
    for(RepositoryEntry repConfiguration : repConfigurations()){
      String repName = repConfiguration.getName();
      ManageableRepository rep = repositoryService.getRepository(repName);
      Session sysSession = rep.getSystemSession(regWorkspaces.get(repName));
      
      if(sysSession.getRootNode().hasNode(EXO_REGISTRY) && replace) 
          sysSession.getRootNode().getNode(EXO_REGISTRY).remove();
      
      if(!sysSession.getRootNode().hasNode(EXO_REGISTRY)) {
        Node rootNode = sysSession.getRootNode().addNode(EXO_REGISTRY, EXO_REGISTRY_NT);
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

  private List <RepositoryEntry> repConfigurations() {
    return  (List <RepositoryEntry>)repositoryService.getConfig().getRepositoryConfigurations();
  }
  
  
  /**
   * Internal Node wrapper which ensures the node of "exo:registryEntry" type inside
   */
  public class RegistryEntryNode extends NodeWrapper {
    private RegistryEntryNode(final Node node) throws RepositoryException {
      super(node);
    }
  }

  /**
   * Internal Node wrapper which ensures the node of "exo:registry" type inside
   */
  public class RegistryNode extends NodeWrapper {
    private RegistryNode(final Node node) throws RepositoryException {
      super(node);
    }
  }

  private abstract class NodeWrapper {
    
    private final Node node;
    
    private NodeWrapper(final Node node) {
      this.node = node;
    }

    public final Node getNode() {
      return node;
    }
  }
}

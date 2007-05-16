/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: RepositoryServiceImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class RepositoryServiceImpl implements RepositoryService, Startable {

  protected static Log log = ExoLogger.getLogger("jcr.RepositoryService");

  private RepositoryServiceConfiguration config;

  private HashMap repositoryContainers = new HashMap();

  private List addNodeTypePlugins;
  
  private List addNamespacesPlugins;

  private ExoContainerContext containerContext;

  public RepositoryServiceImpl(RepositoryServiceConfiguration configuration)
      throws RepositoryConfigurationException, RepositoryException {
    this(configuration, null);
  }

  public RepositoryServiceImpl(RepositoryServiceConfiguration configuration, 
      ExoContainerContext context) throws RepositoryConfigurationException, RepositoryException {
    this.config = configuration;
    addNodeTypePlugins = new ArrayList();
    addNamespacesPlugins = new ArrayList();
    containerContext = context;
  }

  public ManageableRepository getRepository() throws RepositoryException {
    return getRepository(config.getDefaultRepositoryName());
  }

  public ManageableRepository getRepository(String name) throws RepositoryException {
    RepositoryContainer repositoryContainer = (RepositoryContainer) repositoryContainers.get(name);
    log.debug("RepositoryServiceimpl() getRepository " + name);
    if (repositoryContainer == null)
      throw new RepositoryException("Repository '" + name + "' not found.");
    else {
      return (ManageableRepository) repositoryContainer
          .getComponentInstanceOfType(Repository.class);
    }
  }

  public RepositoryServiceConfiguration getConfig() {
    return config;
  }

  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof AddNodeTypePlugin)
      addNodeTypePlugins.add(plugin);
    else if(plugin instanceof AddNamespacesPlugin)
      addNamespacesPlugins.add(plugin);
  }

  public ComponentPlugin removePlugin(String name) {
    return null;
  }

  public Collection getPlugins() {
    return null;
  }

  // ------------------- Startable ----------------------------
  
  public void start() {
    try {

      ExoContainer container = null;
      if (containerContext == null)
        container = PortalContainer.getInstance();
      else
        container = containerContext.getContainer();

      init(container);

      addNamespaces(container);
      addNodeTypes(container);

    } catch (Exception e) {
      log.error("Error start repository service", e);
      //e.printStackTrace();
    }
  }

  public void stop() {
  }
  
  private void init(ExoContainer parentContainer)
      throws RepositoryConfigurationException, RepositoryException {
    List rEntries = config.getRepositoryConfigurations();
    for (int i = 0; i < rEntries.size(); i++) {
      RepositoryEntry rEntry = (RepositoryEntry) rEntries.get(i);
      // Making new repository container as portal's subcontainer
      RepositoryContainer repositoryContainer = new RepositoryContainer(
          parentContainer, rEntry);
      // Storing and starting the repository container under
      // key=repository_name
      repositoryContainers.put(rEntry.getName(), repositoryContainer);
      repositoryContainer.start();
    }
  }
  
  private void addNodeTypes(ExoContainer container) throws Exception {
    
    ConfigurationManager configService = (ConfigurationManager) container
      .getComponentInstanceOfType(ConfigurationManager.class);
    
    ManageableRepository repository = getRepository();
    ExtendedNodeTypeManager ntManager = repository.getNodeTypeManager();
    for (int j = 0; j < addNodeTypePlugins.size(); j++) {
      AddNodeTypePlugin plugin = (AddNodeTypePlugin) addNodeTypePlugins.get(j);
      List nodeTypesFiles = plugin.getNodeTypesFiles();
      String nodeTypeFilesName = null;
      try {
        // Trying load node types from xml file
        if (nodeTypesFiles != null && nodeTypesFiles.size() > 0) {
          for (Iterator iter = nodeTypesFiles.iterator(); iter.hasNext();) {
            nodeTypeFilesName = (String) iter.next();
            InputStream inXml = configService.getInputStream(nodeTypeFilesName);
            //log.info("Trying register nodes from xml-file " + nodeTypeFilesName);
            ntManager.registerNodeTypes(inXml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
            //log.info("Nodes is registered from xml-file " + nodeTypeFilesName);
          }
          continue; // Loaded! Go to next element in addNodeTypePlugins
        } else {
          log
              .warn("Empty nodeTypesFiles param found, trying node types from list stored in plugin config");
        }
      } catch (Exception e) {
        log.error("Error load node types from resource: " + nodeTypeFilesName
            + ", trying node types from list stored in plugin config", e);
      }

      // Otherwise - loading node types from list stored in plugin config
      List nodeTypes = plugin.getNodeTypes();
      if (nodeTypes != null && nodeTypes.size() > 0) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (Iterator iter = nodeTypes.iterator(); iter.hasNext();) {
          String nodeTypeClassName = (String) iter.next();
          Class nodeTypeClass = Class.forName(nodeTypeClassName, true, cl);
          ntManager.registerNodeType(nodeTypeClass, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
          log.info("Node is registered from class " + nodeTypeClassName);
        }
      }
    }
  }
  
  private void addNamespaces(ExoContainer container) throws Exception {
    
    ManageableRepository repository = getRepository();
    NamespaceRegistry nsRegistry = repository.getNamespaceRegistry();
    
    for (int j = 0; j < addNamespacesPlugins.size(); j++) {
      AddNamespacesPlugin plugin = (AddNamespacesPlugin) addNamespacesPlugins
          .get(j);
      Map namespaces = plugin.getNamespaces();
      try {
        for (Iterator iter = namespaces.entrySet().iterator(); iter.hasNext();) {
          Map.Entry namespace = (Map.Entry) iter.next();
          String prefix = (String)namespace.getKey();
          String uri = (String)namespace.getValue();

          // register namespace if not found
          try {
            nsRegistry.getURI(prefix);
          } catch (NamespaceException e) {
            nsRegistry.registerNamespace(prefix, uri);
          }
          log.info("Namespace is registered " + prefix+" = "+uri);
        }
      } catch (Exception e) {
        log.error("Error load namespaces ", e);
      }
    }
  }
  
}
/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.ext.script.groovy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.jcr.ext.resource.UnifiedNodeReference;
import org.exoplatform.services.jcr.ext.resource.jcr.Handler;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.impl.ResourceBinder; //import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.script.groovy.GroovyScriptInstantiator;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScript2RestLoader implements Startable {

  /**
   * Logger.
   */
  private static final Log                 LOG                 = ExoLogger.getLogger(GroovyScript2RestLoader.class.getName());

  /**
   * Default node types for Groovy scripts.
   */
  public static final String               DEFAULT_NODETYPE    = "exo:groovyResourceContainer";

  /**
   * Service name.
   */
  private static final String              SERVICE_NAME        = "GroovyScript2RestLoader";

  /**
   * See {@link InitParams}.
   */
  private InitParams                       initParams;

  /**
   * See {@link ResourceBinder}.
   */
  private ResourceBinder                   binder;

  /**
   * See {@link GroovyScriptInstantiator}.
   */
  private GroovyScriptInstantiator         groovyScriptInstantiator;

  /**
   * See {@link Handler}. Not used in this class but should be in constructor
   * parameters for correct order of start components.
   */
  @SuppressWarnings("unused")
  private Handler                          handler;

  /**
   * See {@link RepositoryService}.
   */
  private RepositoryService                repositoryService;

  /**
   * See {@link ConfigurationManager}.
   */
  private ConfigurationManager             configurationManager;

  /**
   * See {@link RegistryService}.
   */
  private RegistryService                  registryService;

  /**
   * keeps configuration for observation listener.
   */
  private ObservationListenerConfiguration observationListenerConfiguration;

  /**
   * Node type for Groovy scripts.
   */
  private String                           nodeType;

  /**
   * Mapping scripts URL (or other key) to classes.
   */
  private Map<String, Class<?>>            scriptsURL2ClassMap = new HashMap<String, Class<?>>();

  public GroovyScript2RestLoader(ResourceBinder binder,
                                 GroovyScriptInstantiator groovyScriptInstantiator,
                                 Handler handler,
                                 RepositoryService repositoryService,
                                 ConfigurationManager configurationManager,
                                 InitParams params) {
    this(binder,
         groovyScriptInstantiator,
         handler,
         repositoryService,
         configurationManager,
         null,
         params);
  }

  public GroovyScript2RestLoader(ResourceBinder binder,
                                 GroovyScriptInstantiator groovyScriptInstantiator,
                                 Handler handler,
                                 RepositoryService repositoryService,
                                 ConfigurationManager configurationManager,
                                 RegistryService registryService,
                                 InitParams params) {

    this.binder = binder;
    this.groovyScriptInstantiator = groovyScriptInstantiator;
    this.repositoryService = repositoryService;
    this.handler = handler;
    this.configurationManager = configurationManager;

    this.registryService = registryService;
    this.initParams = params;
  }

  /**
   * Remove script with specified URL from ResourceBinder.
   * 
   * @param url the URL. The <code>url.toString()</code> must be corresponded to
   *          script class, otherwise IllegalArgumentException will be thrown.
   * @see GroovyScriptRestLoader#loadScript(URL).
   * @see GroovyScript2RestLoader#loadScript(String, InputStream)
   */
  public void unloadScript(URL url) {
    unloadScript(url.toString());
  }

  /**
   * Remove script by specified key from ResourceBinder.
   * 
   * @param key the key with which script was created.
   * @see GroovyScriptRestLoader#loadScript(URL).
   * @see GroovyScript2RestLoader#loadScript(String, InputStream)
   */
  public void unloadScript(String key) {
    if (scriptsURL2ClassMap.containsKey(key)) {
      if (binder.unbind(scriptsURL2ClassMap.get(key))) {
        scriptsURL2ClassMap.remove(key);
        LOG.info("Remove groovy script, key " + key);
      } else {
        LOG.warn("Can't remove groovy script, key " + key);
      }
    } else {
      LOG.warn("Specified key '" + key + "' does not corresponds to any class name.");
    }
  }

  /**
   * @param url script's key
   * @return true if script loaded false otherwise
   */
  public boolean isLoaded(String key) {
    return scriptsURL2ClassMap.containsKey(key);
  }

  /**
   * @param url script's URL
   * @return true if script loaded false otherwise
   */
  public boolean isLoaded(URL url) {
    return isLoaded(url.toString());
  }

  /**
   * @param url the RUL for loading script.
   * @throws InvalidResourceDescriptorException if loaded object is not valid
   *           ResourceContainer.
   * @throws IOException it script can't be loaded.
   */
  public void loadScript(URL url) throws IOException {
    Object resource = groovyScriptInstantiator.instantiateScript(url);
    if (binder.bind(resource)) {
      // add mapping script URL to name of class.
      scriptsURL2ClassMap.put(url.toString(), resource.getClass());
      LOG.info("Add new groovy scripts, URL: " + url);
    } else {
      LOG.warn("Groovy script was not binded, URL: " + url);
    }

  }

  /**
   * Load script from given stream.
   * 
   * @param key the key which must be corresponded to object class name.
   * @param stream the stream which represents grrovy script.
   * @throws InvalidResourceDescriptorException if loaded Object can't be added
   *           in ResourceBinder.
   * @throws IOException if script can't be loaded or parsed.
   * @see ResourceBinder#bind(ResourceContainer)
   */
  public void loadScript(String key, InputStream stream) throws IOException {
    Object resource = groovyScriptInstantiator.instantiateScript(stream);
    if (binder.bind(resource)) {
      // add mapping script URL to name of class.
      scriptsURL2ClassMap.put(key, resource.getClass());
      LOG.info("Add new groovy scripts, script key: " + key);
    } else {
      LOG.warn("Groovy script was not binded, key: " + key);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void start() {
    if (LOG.isDebugEnabled())
      LOG.debug(">>> begin start");

    if (registryService != null && !registryService.getForceXMLConfigurationValue(initParams)) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try {
        readParamsFromRegistryService(sessionProvider);
      } catch (Exception e) {
        readParamsFromFile();
        try {
          writeParamsToRegistryService(sessionProvider);
        } catch (Exception exc) {
          LOG.error("Cannot write init configuration to RegistryService.", exc);
        }
      } finally {
        sessionProvider.close();
      }
    } else {
      readParamsFromFile();
    }

    // add script from config files to jcr.
    addScripts();

    try {

      String repositoryName = observationListenerConfiguration.getRepository();
      List<String> workspaceNames = observationListenerConfiguration.getWorkspaces();

      ManageableRepository repository = repositoryService.getRepository(repositoryName);

      for (String workspaceName : workspaceNames) {
        Session session = repository.getSystemSession(workspaceName);

        String xpath = "//element(*, " + nodeType + ")";
        Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);

        QueryResult result = query.execute();
        NodeIterator nodeIterator = result.getNodes();
        while (nodeIterator.hasNext()) {
          Node node = nodeIterator.nextNode();

          if (node.getPath().startsWith("/jcr:system")) {
            continue;
          }

          if (node.getProperty("exo:autoload").getBoolean()) {

            UnifiedNodeReference unifiedNodeReference = new UnifiedNodeReference(repositoryName,
                                                                                 workspaceName,
                                                                                 node.getPath());
            loadScript(unifiedNodeReference.getURL().toString(), node.getProperty("jcr:data")
                                                                     .getStream());
            node.setProperty("exo:load", true);
          } else {
            // set it to false to be able to control script life cycle
            node.setProperty("exo:load", false);
          }
        }

        session.save();
        session.getWorkspace()
               .getObservationManager()
               .addEventListener(new GroovyScript2RestUpdateListener(repositoryName,
                                                                     workspaceName,
                                                                     this,
                                                                     session),
                                 Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED
                                     | Event.PROPERTY_REMOVED,
                                 "/",
                                 true,
                                 null,
                                 new String[] { nodeType },
                                 false);
      }
    } catch (Exception e) {
      LOG.error("Error occurs ", e);
    }
    if (LOG.isDebugEnabled())
      LOG.debug("<<< end start");
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    // nothing to do!
  }

  /**
   * See {@link GroovyScript2RestLoaderPlugin}.
   */
  private List<GroovyScript2RestLoaderPlugin> loadPlugins;

  /**
   * @param cp See {@link ComponentPlugin}
   */
  public void addPlugin(ComponentPlugin cp) {
    if (cp instanceof GroovyScript2RestLoaderPlugin) {
      if (loadPlugins == null)
        loadPlugins = new ArrayList<GroovyScript2RestLoaderPlugin>();
      loadPlugins.add((GroovyScript2RestLoaderPlugin) cp);
    }
  }

  /**
   * Add scripts that specified in configuration.
   */
  private void addScripts() {
    if (loadPlugins == null || loadPlugins.size() == 0)
      return;
    for (GroovyScript2RestLoaderPlugin loadPlugin : loadPlugins) {
      Session session = null;
      try {
        ManageableRepository repository = repositoryService.getRepository(loadPlugin.getRepository());
        String workspace = loadPlugin.getWorkspace();
        session = repository.getSystemSession(workspace);
        String nodeName = loadPlugin.getNode();
        Node node = null;
        try {
          node = (Node) session.getItem(nodeName);
        } catch (PathNotFoundException e) {
          StringTokenizer tokens = new StringTokenizer(nodeName, "/");
          node = session.getRootNode();
          while (tokens.hasMoreTokens()) {
            String t = tokens.nextToken();
            if (node.hasNode(t))
              node = node.getNode(t);
            else
              node = node.addNode(t);
          }
        }

        for (XMLGroovyScript2Rest xg : loadPlugin.getXMLConfigs()) {
          String scriptName = xg.getName();
          if (node.hasNode(scriptName)) {
            LOG.warn("Node '" + node.getPath() + "/" + scriptName + "' already exists. ");
            continue;
          }
          Node scriptFile = node.addNode(scriptName, "nt:file");
          // TODO use the same node-type here and in observation listener
          // configuration. Temporary use 'GroovyScript2RestLoader.DEFAULT_NODETYPE'
          Node script = scriptFile.addNode("jcr:content", GroovyScript2RestLoader.DEFAULT_NODETYPE);
          script.setProperty("exo:autoload", xg.isAutoload());
          script.setProperty("exo:load", false);
          script.setProperty("jcr:mimeType", "script/groovy");
          script.setProperty("jcr:lastModified", Calendar.getInstance());
          script.setProperty("jcr:data", configurationManager.getInputStream(xg.getPath()));
        }
        session.save();
      } catch (Exception e) {
        LOG.error("Failed add scripts. ", e);
      } finally {
        if (session != null)
          session.logout();
      }
    }
  }

  /**
   * Read parameters from RegistryService.
   * 
   * @param sessionProvider The SessionProvider
   * @throws RepositoryException
   * @throws PathNotFoundException
   */
  private void readParamsFromRegistryService(SessionProvider sessionProvider) throws PathNotFoundException,
                                                                             RepositoryException {

    if (LOG.isDebugEnabled())
      LOG.debug("<<< Read init parametrs from registry service.");

    observationListenerConfiguration = new ObservationListenerConfiguration();

    String entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/" + "nodeType";
    RegistryEntry registryEntry = registryService.getEntry(sessionProvider, entryPath);
    Document doc = registryEntry.getDocument();
    Element element = doc.getDocumentElement();
    nodeType = getAttributeSmart(element, "value");

    entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/" + "repository";
    registryEntry = registryService.getEntry(sessionProvider, entryPath);
    doc = registryEntry.getDocument();
    element = doc.getDocumentElement();
    observationListenerConfiguration.setRepository(getAttributeSmart(element, "value"));

    entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/" + "workspaces";
    registryEntry = registryService.getEntry(sessionProvider, entryPath);
    doc = registryEntry.getDocument();
    element = doc.getDocumentElement();
    String workspaces = getAttributeSmart(element, "value");

    String ws[] = workspaces.split(";");
    List<String> wsList = new ArrayList<String>();
    for (String w : ws) {
      wsList.add(w);
    }

    observationListenerConfiguration.setWorkspaces(wsList);

    LOG.info("NodeType from RegistryService: " + nodeType);
    LOG.info("Repository from RegistryService: " + observationListenerConfiguration.getRepository());
    LOG.info("Workspaces node from RegistryService: "
        + observationListenerConfiguration.getWorkspaces());
  }

  /**
   * Write parameters to RegistryService.
   * 
   * @param sessionProvider The SessionProvider
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws RepositoryException
   */
  private void writeParamsToRegistryService(SessionProvider sessionProvider) throws IOException,
                                                                            SAXException,
                                                                            ParserConfigurationException,
                                                                            RepositoryException {
    if (LOG.isDebugEnabled())
      LOG.debug(">>> Save init parametrs in registry service.");

    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = doc.createElement(SERVICE_NAME);
    doc.appendChild(root);

    Element element = doc.createElement("nodeType");
    setAttributeSmart(element, "value", nodeType);
    root.appendChild(element);

    StringBuffer sb = new StringBuffer();
    for (String workspace : observationListenerConfiguration.getWorkspaces()) {
      if (sb.length() > 0)
        sb.append(';');
      sb.append(workspace);
    }
    element = doc.createElement("workspaces");
    setAttributeSmart(element, "value", sb.toString());
    root.appendChild(element);

    element = doc.createElement("repository");
    setAttributeSmart(element, "value", observationListenerConfiguration.getRepository());
    root.appendChild(element);

    RegistryEntry serviceEntry = new RegistryEntry(doc);
    registryService.createEntry(sessionProvider, RegistryService.EXO_SERVICES, serviceEntry);
  }

  /**
   * Get attribute value.
   * 
   * @param element The element to get attribute value
   * @param attr The attribute name
   * @return Value of attribute if present and null in other case
   */
  private String getAttributeSmart(Element element, String attr) {
    return element.hasAttribute(attr) ? element.getAttribute(attr) : null;
  }

  /**
   * Set attribute value. If value is null the attribute will be removed.
   * 
   * @param element The element to set attribute value
   * @param attr The attribute name
   * @param value The value of attribute
   */
  private void setAttributeSmart(Element element, String attr, String value) {
    if (value == null) {
      element.removeAttribute(attr);
    } else {
      element.setAttribute(attr, value);
    }
  }

  /**
   * Read parameters from file.
   */
  private void readParamsFromFile() {
    if (initParams != null) {
      nodeType = initParams.getValuesParam("nodetype") != null ? initParams.getValueParam("nodetype")
                                                                           .getValue()
                                                              : DEFAULT_NODETYPE;

      ObjectParameter param = initParams.getObjectParam("observation.config");
      observationListenerConfiguration = (ObservationListenerConfiguration) param.getObject();
    }

    LOG.info("NodeType from configuration file: " + nodeType);
    LOG.info("Repository from configuration file: "
        + observationListenerConfiguration.getRepository());
    LOG.info("Workspaces node from configuration file: "
        + observationListenerConfiguration.getWorkspaces());
  }

  /**
   * Should be used in configuration.xml as object parameter.
   */
  public static class ObservationListenerConfiguration {

    /**
     * Repository name.
     */
    private String       repository;

    /**
     * Workspace name.
     */
    private List<String> workspaces;

    /**
     * @return get repository
     */
    public String getRepository() {
      return repository;
    }

    /**
     * @param repository repository name
     */
    public void setRepository(String repository) {
      this.repository = repository;
    }

    /**
     * @return get list of workspaces
     */
    public List<String> getWorkspaces() {
      return workspaces;
    }

    /**
     * @param workspaces list of workspaces
     */
    public void setWorkspaces(List<String> workspaces) {
      this.workspaces = workspaces;
    }

  }

}

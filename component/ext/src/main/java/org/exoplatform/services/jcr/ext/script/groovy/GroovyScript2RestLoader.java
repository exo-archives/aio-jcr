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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.script.groovy.GroovyScriptInstantiator;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScript2RestLoader implements Startable {

  public static final String                    DEFAULT_NODETYPE    = "exo:groovyResourceContainer";

  private static final String                   SERVICE_NAME        = "GroovyScript2RestLoader";

  private InitParams                            initParams;

  private ResourceBinder                        binder;

  private GroovyScriptInstantiator              groovyScriptInstantiator;

  private Handler                               handler;

  private RepositoryService                     repositoryService;

  private RegistryService                       registryService;

  private ObservationListenerConfiguration      observationListenerConfiguration;

  private String                                nodeType;

  private Map<String, Class<?>> scriptsURL2ClassMap = new HashMap<String, Class<?>>();

  /**
   * Logger.
   */
  private static final Log                      log                 = ExoLogger.getLogger(GroovyScript2RestLoader.class.getName());

  public GroovyScript2RestLoader(ResourceBinder binder,
                                 GroovyScriptInstantiator groovyScriptInstantiator,
                                 Handler handler,
                                 RepositoryService repositoryService,
                                 InitParams params) {
    this(binder, groovyScriptInstantiator, handler, repositoryService, null, params);
  }

  public GroovyScript2RestLoader(ResourceBinder binder,
                                 GroovyScriptInstantiator groovyScriptInstantiator,
                                 Handler handler,
                                 RepositoryService repositoryService,
                                 RegistryService registryService,
                                 InitParams params) {

    this.binder = binder;
    this.groovyScriptInstantiator = groovyScriptInstantiator;
    this.repositoryService = repositoryService;
    this.handler = handler;

    this.registryService = registryService;
    this.initParams = params;
  }

  /**
   * Remove script with specified URL from ResourceBinder.
   * 
   * @param url the URL. The <code>url.toString()</code> must be corresponded to
   *          script class, otherwise IllegalArgumentException will be
   *          thrown.
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
       binder.unbind(scriptsURL2ClassMap.get(key));
      scriptsURL2ClassMap.remove(key);
    } else {
      throw new IllegalArgumentException("Specified key '" + key
          + "' does not corresponds to any class name.");
    }
  }

  /**
   * @param url the RUL for loading script.
   * @throws InvalidResourceDescriptorException if loaded object is not valid
   *           ResourceContainer.
   * @throws IOException it script can't be loaded.
   */
  public void loadScript(URL url) throws IOException {

    ResourceContainer resourceContainer = (ResourceContainer) groovyScriptInstantiator.instantiateScript(url);
    binder.bind(resourceContainer);

    // add mapping script URL to name of class.
    scriptsURL2ClassMap.put(url.toString(), resourceContainer.getClass());
    log.info("Add new groovy scripts, URL: " + url);

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
    ResourceContainer resourceContainer = (ResourceContainer) groovyScriptInstantiator.instantiateScript(stream);
    binder.bind(resourceContainer);
    // add mapping script URL to name of class.
    scriptsURL2ClassMap.put(key, resourceContainer.getClass());
    log.info("Add new groovy scripts, script key: " + key);
  }

  /*
   * (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    if (registryService != null && !registryService.getForceXMLConfigurationValue(initParams)) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try {
        readParamsFromRegistryService(sessionProvider);
      } catch (Exception e) {
        readParamsFromFile();
        try {
          writeParamsToRegistryService(sessionProvider);
        } catch (Exception exc) {
          log.error("Cannot write init configuration to RegistryService.", exc);
        }
      } finally {
        sessionProvider.close();
      }
    } else {
      readParamsFromFile();
    }

    // observation not configured
    if (observationListenerConfiguration == null)
      return;

    try {

      String repositoryName = observationListenerConfiguration.getRepository();
      List<String> workspaceNames = observationListenerConfiguration.getWorkspaces();

      ManageableRepository repository = repositoryService.getRepository(repositoryName);

      for (String workspaceName : workspaceNames) {
        Session session = repository.getSystemSession(workspaceName);

        session.getWorkspace()
               .getObservationManager()
               .addEventListener(new GroovyScript2RestUpdateListener(repositoryName,
                                                                     workspaceName,
                                                                     this,
                                                                     session),
                                 Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED,
                                 "/",
                                 true,
                                 null,
                                 new String[] { nodeType },
                                 false);

        String xpath = "//element(*, " + nodeType + ")[@exo:autoload='true']";
        Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);

        QueryResult result = query.execute();
        NodeIterator nodeIterator = result.getNodes();
        while (nodeIterator.hasNext()) {
          Node node = nodeIterator.nextNode();

          if (node.getPath().startsWith("/jcr:system")){
            continue;
          }

          UnifiedNodeReference unifiedNodeReference = new UnifiedNodeReference(repositoryName,
                                                                               workspaceName,
                                                                               node.getPath());

          loadScript(unifiedNodeReference.getURL().toString(), node.getProperty("jcr:data")
                                                                   .getStream());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    // nothing to do!
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
    for (int i = 0; i < ws.length; i++) {
      if (!ws[i].equals("")) {
        wsList.add(ws[i]);
      }
    }

    observationListenerConfiguration.setWorkspaces(wsList);

    log.info("NodeType from RegistryService: " + nodeType);
    log.info("Repository from RegistryService: " + observationListenerConfiguration.getRepository());
    log.info("Workspaces node from RegistryService: "
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

    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = doc.createElement(SERVICE_NAME);
    doc.appendChild(root);

    Element element = doc.createElement("nodeType");
    setAttributeSmart(element, "value", nodeType);
    root.appendChild(element);

    String workspaces = "";
    for (String workspace : observationListenerConfiguration.getWorkspaces()) {
      workspaces += workspace + ";";
    }
    element = doc.createElement("workspaces");
    setAttributeSmart(element, "value", workspaces);
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

    log.info("NodeType from configuration file: " + nodeType);
    log.info("Repository from configuration file: "
        + observationListenerConfiguration.getRepository());
    log.info("Workspaces node from configuration file: "
        + observationListenerConfiguration.getWorkspaces());
  }

  /*
   * Should be used in configuration.xml as object parameter.
   */
  public static class ObservationListenerConfiguration {

    private String       repository;

    private List<String> workspaces;

    public String getRepository() {
      return repository;
    }

    public void setRepository(String repository) {
      this.repository = repository;
    }

    public List<String> getWorkspaces() {
      return workspaces;
    }

    public void setWorkspaces(List<String> workspaces) {
      this.workspaces = workspaces;
    }

  }

}

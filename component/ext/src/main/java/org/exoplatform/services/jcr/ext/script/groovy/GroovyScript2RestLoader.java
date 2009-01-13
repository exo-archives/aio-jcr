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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.jcr.ext.resource.UnifiedNodeReference;
import org.exoplatform.services.jcr.ext.resource.jcr.Handler;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.script.groovy.GroovyScriptInstantiator;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("script/groovy/{repository}/{workspace}")
public class GroovyScript2RestLoader implements Startable {

  /**
   * Logger.
   */
  private static final Log                  LOG                 = ExoLogger.getLogger(GroovyScript2RestLoader.class.getName());

  /**
   * Default node types for Groovy scripts.
   */
  private static final String               DEFAULT_NODETYPE    = "exo:groovyResourceContainer";

  /**
   * Service name.
   */
  private static final String               SERVICE_NAME        = "GroovyScript2RestLoader";

  /**
   * See {@link InitParams}.
   */
  private InitParams                        initParams;

  /**
   * See {@link ResourceBinder}.
   */
  private ResourceBinder                    binder;

  /**
   * See {@link GroovyScriptInstantiator}.
   */
  private GroovyScriptInstantiator          groovyScriptInstantiator;

  /**
   * See {@link RepositoryService}.
   */
  private RepositoryService                 repositoryService;

  /**
   * See {@link ConfigurationManager}.
   */
  private ConfigurationManager              configurationManager;

  /**
   * See {@link RegistryService}.
   */
  private RegistryService                   registryService;

  /**
   * See {@link SessionProviderService},
   * {@link ThreadLocalSessionProviderService}.
   */
  private ThreadLocalSessionProviderService sessionProviderService;

  /**
   * keeps configuration for observation listener.
   */
  private ObservationListenerConfiguration  observationListenerConfiguration;

  /**
   * Node type for Groovy scripts.
   */
  private String                            nodeType;

  /**
   * Mapping scripts URL (or other key) to classes.
   */
  private Map<String, Class<?>>             scriptsURL2ClassMap = new HashMap<String, Class<?>>();

  /**
   * @param binder binder for RESTful services
   * @param groovyScriptInstantiator instantiate groovy scripts
   * @param repositoryService See {@link RepositoryService}
   * @param sessionProviderService See {@link SessionProviderService}
   * @param configurationManager for solve resource loading issue in common way
   * @param handler
   * @param params initialized parameters
   */
  public GroovyScript2RestLoader(ResourceBinder binder,
                                 GroovyScriptInstantiator groovyScriptInstantiator,
                                 RepositoryService repositoryService,
                                 ThreadLocalSessionProviderService sessionProviderService,
                                 ConfigurationManager configurationManager,
                                 Handler handler,
                                 InitParams params) {
    this(binder,
         groovyScriptInstantiator,
         repositoryService,
         sessionProviderService,
         configurationManager,
         null,
         handler,
         params);
  }

  /**
   * @param binder binder for RESTful services
   * @param groovyScriptInstantiator instantiates Groovy scripts
   * @param repositoryService See {@link RepositoryService}
   * @param sessionProviderService See {@link SessionProviderService}
   * @param configurationManager for solve resource loading issue in common way
   * @param registryService See {@link RegistryService}
   * @param handler
   * @param params initialized parameters
   */
  public GroovyScript2RestLoader(ResourceBinder binder,
                                 GroovyScriptInstantiator groovyScriptInstantiator,
                                 RepositoryService repositoryService,
                                 ThreadLocalSessionProviderService sessionProviderService,
                                 ConfigurationManager configurationManager,
                                 RegistryService registryService,
                                 Handler handler,
                                 InitParams params) {

    this.binder = binder;
    this.groovyScriptInstantiator = groovyScriptInstantiator;
    this.repositoryService = repositoryService;
    this.handler = handler;
    this.configurationManager = configurationManager;
    this.registryService = registryService;
    this.sessionProviderService = sessionProviderService;
    this.initParams = params;
  }

  /**
   * Remove script with specified URL from ResourceBinder.
   * 
   * @param url the URL. The <code>url.toString()</code> must be corresponded to
   *          script class.
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
   * Get node type for store scripts, may throw {@link IllegalStateException} if
   * <tt>nodeType</tt> not initialized yet.
   * 
   * @return return node type
   */
  public String getNodeType() {
    if (nodeType == null)
      throw new IllegalStateException("Node type not initialized, yet. ");
    return nodeType;
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
   * @param stream the stream which represents groovy script.
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

    // Add script from configuration files to JCR.
    addScripts();

    try {

      // Deploy auto-load scripts and start Observation Listeners.
      String repositoryName = observationListenerConfiguration.getRepository();
      List<String> workspaceNames = observationListenerConfiguration.getWorkspaces();

      ManageableRepository repository = repositoryService.getRepository(repositoryName);

      for (String workspaceName : workspaceNames) {
        Session session = repository.getSystemSession(workspaceName);

        String xpath = "//element(*, " + getNodeType() + ")[@exo:autoload='true']";
        Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);

        QueryResult result = query.execute();
        NodeIterator nodeIterator = result.getNodes();
        while (nodeIterator.hasNext()) {
          Node node = nodeIterator.nextNode();

          if (node.getPath().startsWith("/jcr:system"))
            continue;

          UnifiedNodeReference unifiedNodeReference = new UnifiedNodeReference(repositoryName,
                                                                               workspaceName,
                                                                               node.getPath());
          loadScript(unifiedNodeReference.getURL().toString(), node.getProperty("jcr:data")
                                                                   .getStream());
        }

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
                                 new String[] { getNodeType() },
                                 false);
      }
    } catch (Exception e) {
      LOG.error("Error occurs ", e);
    }
    // Finally bind this object as RESTful service.
    // NOTE this service does not implement ResourceContainer, as usually
    // done for this type of services. It can't be binded in common way cause
    // to dependencies problem. And in other side not possible to use third
    // part which can be injected by GroovyScript2RestLoader.
    binder.bind(this);
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
   * See {@link Handler}. Not used in this class but should be in constructor
   * parameters for correct order of start components.
   */
  @SuppressWarnings("unused")
  private Handler handler;

  /**
   * Add scripts that specified in configuration.
   */
  private void addScripts() {
    if (loadPlugins == null || loadPlugins.size() == 0)
      return;
    for (GroovyScript2RestLoaderPlugin loadPlugin : loadPlugins) {
      // If no one script configured then skip this item,
      // there is no reason to do anything.
      if (loadPlugin.getXMLConfigs().size() == 0)
        continue;

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
              node = node.addNode(t, "nt:folder");
          }
        }

        for (XMLGroovyScript2Rest xg : loadPlugin.getXMLConfigs()) {
          String scriptName = xg.getName();
          if (node.hasNode(scriptName)) {
            LOG.warn("Node '" + node.getPath() + "/" + scriptName + "' already exists. ");
            continue;
          }

          createScript(node,
                       scriptName,
                       xg.isAutoload(),
                       configurationManager.getInputStream(xg.getPath()));
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
   * Create JCR node.
   * 
   * @param parent parent node
   * @param name name of node to be created
   * @param stream data stream for property jcr:data
   * @return newly created node
   * @throws Exception if any errors occurs
   */
  private Node createScript(Node parent, String name, boolean autoload, InputStream stream) throws Exception {
    Node scriptFile = parent.addNode(name, "nt:file");
    Node script = scriptFile.addNode("jcr:content", getNodeType());
    script.setProperty("exo:autoload", autoload);
    script.setProperty("jcr:mimeType", "script/groovy");
    script.setProperty("jcr:lastModified", Calendar.getInstance());
    script.setProperty("jcr:data", stream);
    return scriptFile;
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

    LOG.info("NodeType from RegistryService: " + getNodeType());
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
    setAttributeSmart(element, "value", getNodeType());
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

    LOG.info("NodeType from configuration file: " + getNodeType());
    LOG.info("Repository from configuration file: "
        + observationListenerConfiguration.getRepository());
    LOG.info("Workspaces node from configuration file: "
        + observationListenerConfiguration.getWorkspaces());
  }

  /**
   * This method is useful for clients that can send script in request body
   * without form-data. At required to set specific Content-type header
   * 'script/groovy'.
   * 
   * @param stream the stream that contains groovy source code
   * @param uriInfo see {@link UriInfo}
   * @param repository repository name
   * @param workspace workspace name
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "script/groovy" })
  @Path("{path:.*}/add")
  public Response addScript(InputStream stream,
                            @Context UriInfo uriInfo,
                            @PathParam("repository") String repository,
                            @PathParam("workspace") String workspace,
                            @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem(getPath(path));
      createScript(node, getName(path), false, stream);
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * This method is useful for clients that can send script in request body
   * without form-data. At required to set specific Content-type header
   * 'script/groovy'.
   * 
   * @param stream the stream that contains groovy source code
   * @param uriInfo see {@link UriInfo}
   * @param repository repository name
   * @param workspace workspace name
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "script/groovy" })
  @Path("{path:.*}/update")
  public Response updateScript(InputStream stream,
                               @Context UriInfo uriInfo,
                               @PathParam("repository") String repository,
                               @PathParam("workspace") String workspace,
                               @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem("/" + path);
      node.getNode("jcr:content").setProperty("jcr:data", stream);
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * This method is useful for clients that send scripts as file in
   * 'multipart/*' request body. <br/>
   * NOTE even we use iterator item should be only one, rule one address - one
   * script. This method is created just for comfort loading script from HTML
   * form. NOT use this script for uploading few files in body of
   * 'multipart/form-data' or other type of multipart.
   * 
   * @param items iterator {@link FileItem}
   * @param uriInfo see {@link UriInfo}
   * @param repository repository name
   * @param workspace workspace name
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "multipart/*" })
  @Path("{path:.*}/add")
  public Response addScript(Iterator<FileItem> items,
                            @Context UriInfo uriInfo,
                            @PathParam("repository") String repository,
                            @PathParam("workspace") String workspace,
                            @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem(getPath(path));
      InputStream stream = null;
      boolean autoload = false;
      while (items.hasNext()) {
        FileItem fitem = items.next();
        if (fitem.isFormField() && fitem.getFieldName() != null
            && fitem.getFieldName().equalsIgnoreCase("autoload"))
          autoload = Boolean.valueOf(fitem.getString());
        else if (!fitem.isFormField()) // accept files
          stream = fitem.getInputStream();
      }

      createScript(node, getName(path), autoload, stream);
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * This method is useful for clients that send scripts as file in
   * 'multipart/*' request body. <br/>
   * NOTE even we use iterator item should be only one, rule one address - one
   * script. This method is created just for comfort loading script from HTML
   * form. NOT use this script for uploading few files in body of
   * 'multipart/form-data' or other type of multipart.
   * 
   * @param items iterator {@link FileItem}
   * @param uriInfo see {@link UriInfo}
   * @param repository repository name
   * @param workspace workspace name
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "multipart/*" })
  @Path("{path:.*}/update")
  public Response updateScripts(Iterator<FileItem> items,
                                @Context UriInfo uriInfo,
                                @PathParam("repository") String repository,
                                @PathParam("workspace") String workspace,
                                @PathParam("path") String path) {
    Session ses = null;
    try {
      FileItem fitem = items.next();
      InputStream stream = null;
      if (!fitem.isFormField()) // if file
        stream = fitem.getInputStream();
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem("/" + path);
      node.getNode("jcr:content").setProperty("jcr:data", stream);
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Get source code of groovy script.
   * 
   * @param repository repository name
   * @param workspace workspace name
   * @param path JCR path to node that contains script
   * @return groovy script as stream
   */
  @GET
  @Produces( { "script/groovy" })
  @Path("{path:.*}/src")
  public InputStream getScript(@PathParam("repository") String repository,
                               @PathParam("workspace") String workspace,
                               @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node scriptFile = (Node) ses.getItem("/" + path);
      return scriptFile.getNode("jcr:content").getProperty("jcr:data").getStream();

    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Get groovy script's meta-information.
   * 
   * @param repository repository name
   * @param workspace workspace name
   * @param path JCR path to node that contains script
   * @return groovy script's meta-information
   */
  @GET
  @Produces( { MediaType.APPLICATION_JSON })
  @Path("{path:.*}/meta")
  public ScriptMetadata getScriptMetadata(@PathParam("repository") String repository,
                                          @PathParam("workspace") String workspace,
                                          @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      ScriptMetadata meta = new ScriptMetadata(script.getProperty("exo:autoload").getBoolean(),
                                               script.getProperty("jcr:mimeType").getString(),
                                               script.getProperty("jcr:lastModified")
                                                     .getDate()
                                                     .getTimeInMillis());
      return meta;
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Remove node that contains groovy script.
   * 
   * @param repository repository name
   * @param workspace workspace name
   * @param path JCR path to node that contains script
   */
  @GET
  @Path("{path:.*}/delete")
  public void deleteScript(@PathParam("repository") String repository,
                           @PathParam("workspace") String workspace,
                           @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      ses.getItem("/" + path).remove();
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Change exo:autoload property. If this property is 'true' script will be
   * deployed automatically when JCR repository startup and automatically
   * re-deployed when script source code changed.
   * 
   * @param repository repository name
   * @param workspace workspace name
   * @param path JCR path to node that contains script
   * @param state value for property exo:autoload, if it is not specified then
   *          'true' will be used as default. <br />
   *          Example: .../scripts/groovy/test1.groovy/load is the same to
   *          .../scripts/groovy/test1.groovy/load?state=true
   */
  @GET
  @Path("{path:.*}/autoload")
  public void autoload(@PathParam("repository") String repository,
                       @PathParam("workspace") String workspace,
                       @PathParam("path") String path,
                       @DefaultValue("true") @QueryParam("state") boolean state) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      script.setProperty("exo:autoload", state);
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Deploy groovy script as REST service. If this property set to 'true' then
   * script will be deployed as REST service if 'false' the script will be
   * undeployed. NOTE is script already deployed and <tt>state</tt> is
   * <tt>true</tt> script will be re-deployed.
   * 
   * @param repository repository name
   * @param workspace workspace name
   * @param path the path to JCR node that contains groovy script to be deployed
   */
  @GET
  @Path("{path:.*}/load")
  public void load(@PathParam("repository") String repository,
                   @PathParam("workspace") String workspace,
                   @PathParam("path") String path,
                   @DefaultValue("true") @QueryParam("state") boolean state) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      String unifiedNodePath = new UnifiedNodeReference(repository, workspace, script.getPath()).getURL()
                                                                                                .toString();
      if (isLoaded(unifiedNodePath))
        unloadScript(unifiedNodePath);

      if (state)
        loadScript(unifiedNodePath, script.getProperty("jcr:data").getStream());
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Returns the list of all groovy-scripts found in workspace.
   * 
   * @param repository Repository name.
   * @param workspace Workspace name.
   * @param name Additional search parameter. If not emtpy method returns the
   *          list of script names matching wildcard else returns all the
   *          scripts found in workspace.
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/list")
  public ScriptList list(@PathParam("repository") String repository,
                         @PathParam("workspace") String workspace,
                         @QueryParam("name") String name) {

    Session ses = null;
    try {

      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));

      String xpath = "//element(*, exo:groovyResourceContainer)";

      Query query = ses.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();

      ArrayList<String> scriptList = new ArrayList<String>();

      if (name == null) {
        while (nodeIterator.hasNext()) {
          Node node = nodeIterator.nextNode();
          scriptList.add(node.getParent().getPath());
        }
      } else {
        while (nodeIterator.hasNext()) {          
          Node node = nodeIterator.nextNode();
          String scriptName = getName(node.getParent().getPath());
          Pattern pattern = Pattern.compile(name.replace("*", ".*"));

          if (pattern.matcher(scriptName).matches()) {
            scriptList.add(node.getParent().getPath());
          }
        }
      }
      return new ScriptList(scriptList);

    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Extract path to node's parent from full path.
   * 
   * @param fullPath full path to node
   * @return node's parent path
   */
  private static String getPath(String fullPath) {
    int sl = fullPath.lastIndexOf('/');
    return sl > 0 ? "/" + fullPath.substring(0, sl) : "/";
  }

  /**
   * Extract node's name from full node path.
   * 
   * @param fullPath full path to node
   * @return node's name
   */
  private static String getName(String fullPath) {
    int sl = fullPath.lastIndexOf('/');
    return sl > 0 ? fullPath.substring(sl + 1) : fullPath;
  }

  /**
   * Script metadata, used for pass script metada as JSON.
   */
  public static class ScriptMetadata {

    /**
     * Is script autoload.
     */
    private final boolean autoload;

    /**
     * Script media type (script/groovy).
     */
    private final String  mediaType;

    /**
     * Last modified date.
     */
    private final long    lastModified;

    public ScriptMetadata(boolean autoload, String mediaType, long lastModified) {
      this.autoload = autoload;
      this.mediaType = mediaType;
      this.lastModified = lastModified;
    }

    /**
     * @return {@link #autoload}
     */
    public boolean getAutoload() {
      return autoload;
    }

    /**
     * @return {@link #mediaType}
     */
    public String getMediaType() {
      return mediaType;
    }

    /**
     * @return {@link #lastModified}
     */
    public long getLastModified() {
      return lastModified;
    }
  }

  /**
   * Script list, used for pass script list as JSON.
   */
  public static class ScriptList {

    /**
     * The list of scripts.
     */
    private List<String> list;

    /**
     * 
     * Returns the list of scripts.
     *
     * @return the list of scripts.
     */
    public List<String> getList() {
      return list;
    }

    /**
     * 
     * ScriptList  constructor.
     *
     * @param the list of scripts
     */
    public ScriptList(List<String> scriptList) {
      this.list = scriptList;
    }

  }
}

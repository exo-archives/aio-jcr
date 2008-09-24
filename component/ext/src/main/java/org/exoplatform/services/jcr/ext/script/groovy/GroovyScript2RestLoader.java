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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.resource.UnifiedNodeReference;
import org.exoplatform.services.jcr.ext.resource.jcr.Handler;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.container.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.script.groovy.GroovyScriptInstantiator;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScript2RestLoader implements Startable {

  public static final String               DEFAULT_NODETYPE     = "exo:groovyResourceContainer";

  private static final Log                 Log                  = ExoLogger.getLogger("jcr.script.GroovyScript2RestLoader");

  private ResourceBinder                   binder;

  private GroovyScriptInstantiator         groovyScriptInstantiator;

  // used for processing URL : jcr://repository/workspace#/root/node1/....
  private Handler                          handler;

  private RepositoryService                repositoryService;

  private ObservationListenerConfiguration observationListenerConfiguration;

  private String                           nodeType;

  private Map<String, String>              scriptsURL2ClassName = new HashMap<String, String>();

  public GroovyScript2RestLoader(ResourceBinder binder,
                                 GroovyScriptInstantiator groovyScriptInstantiator,
                                 Handler handler,
                                 RepositoryService repositoryService,
                                 InitParams params) {

    this.binder = binder;
    this.groovyScriptInstantiator = groovyScriptInstantiator;
    this.repositoryService = repositoryService;
    this.handler = handler;

    if (params != null) {
      nodeType = params.getValuesParam("nodetype") != null ? params.getValueParam("nodetype")
                                                                   .getValue() : DEFAULT_NODETYPE;

      ObjectParameter param = params.getObjectParam("observation.config");
      if (param != null)
        observationListenerConfiguration = (ObservationListenerConfiguration) param.getObject();
    }
  }

  /**
   * Remove script with specified URL from ResourceBinder.
   * 
   * @param url
   *          the URL. The <code>url.toString()</code> must be corresponded to script class name,
   *          otherwise IllegalArgumentException will be thrown.
   * @see GroovyScriptRestLoader#loadScript(URL).
   * @see GroovyScript2RestLoader#loadScript(String, InputStream)
   */
  public void unloadScript(URL url) {
    unloadScript(url.toString());
  }

  /**
   * Remove script by specified key from ResourceBinder.
   * 
   * @param key
   *          the key with which script was created.
   * 
   * @see GroovyScriptRestLoader#loadScript(URL).
   * @see GroovyScript2RestLoader#loadScript(String, InputStream)
   */
  public void unloadScript(String key) {
    if (scriptsURL2ClassName.containsKey(key)) {
      binder.unbind(scriptsURL2ClassName.get(key));
      scriptsURL2ClassName.remove(key);
    } else {
      throw new IllegalArgumentException("Specified key '" + key
          + "' does not corresponds to any class name.");
    }
  }

  /**
   * @param url
   *          the RUL for loading script.
   * @throws InvalidResourceDescriptorException
   *           if loaded object is not valid ResourceContainer.
   * @throws IOException
   *           it script can't be loaded.
   */
  public void loadScript(URL url) throws InvalidResourceDescriptorException, IOException {

    ResourceContainer resourceContainer = (ResourceContainer) groovyScriptInstantiator.instantiateScript(url);
    binder.bind(resourceContainer);

    // add mapping script URL to name of class.
    scriptsURL2ClassName.put(url.toString(), resourceContainer.getClass().getName());
    Log.info("Add new groovy scripts, URL: " + url);

  }

  /**
   * Load script from given stream.
   * 
   * @param key
   *          the key which must be corresponded to object class name.
   * @param stream
   *          the stream which represents grrovy script.
   * @throws InvalidResourceDescriptorException
   *           if loaded Object can't be added in ResourceBinder.
   * @throws IOException
   *           if script can't be loaded or parsed.
   * @see ResourceBinder#bind(ResourceContainer)
   */
  public void loadScript(String key, InputStream stream) throws InvalidResourceDescriptorException,
                                                        IOException {
    ResourceContainer resourceContainer = (ResourceContainer) groovyScriptInstantiator.instantiateScript(stream);
    binder.bind(resourceContainer);

    // add mapping script URL to name of class.
    scriptsURL2ClassName.put(key, resourceContainer.getClass().getName());
    Log.info("Add new groovy scripts, script key: " + key);
  }

  /*
   * (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    // observation not configured
    if (observationListenerConfiguration == null)
      return;

    try {

      String repositoryName = observationListenerConfiguration.getRepository();
      List<String> workspaceNames = observationListenerConfiguration.getWorkspaces();

      ManageableRepository repository = repositoryService.getRepository(repositoryName);

      // SessionProvider sessionProvider = SessionProvider.createSystemProvider();

      for (String workspaceName : workspaceNames) {
        // Session session = sessionProvider.getSession(workspaceName, repository);
        Session session = repository.getSystemSession(workspaceName);

        // add observation listeners
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

        // load all nodes exo:groovy2rest with exo:autoload=true
        String xpath = "//element(*, " + nodeType + ")[@exo:autoload='true']";
        Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);

        QueryResult result = query.execute();
        NodeIterator nodeIterator = result.getNodes();
        while (nodeIterator.hasNext()) {
          Node node = nodeIterator.nextNode();

          // skip nodes from /jcr:system
          if (node.getPath().startsWith("/jcr:system"))
            continue;

          UnifiedNodeReference unifiedNodeReference = new UnifiedNodeReference(repositoryName,
                                                                               workspaceName,
                                                                               node.getPath());

          // use URL as key
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

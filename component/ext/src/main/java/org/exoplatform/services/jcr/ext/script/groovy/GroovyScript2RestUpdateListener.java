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

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.services.log.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyScript2RestUpdateListener implements EventListener {

  /**
   * Logger.
   */
  private static final Log              LOG = ExoLogger.getLogger(GroovyScript2RestUpdateListener.class.getName());

  /**
   * Repository name.
   */
  private final String                  repository;

  /**
   * Workspace name.
   */
  private final String                  workspace;

  /**
   * See {@link GroovyScript2RestLoader}.
   */
  private final GroovyScript2RestLoader groovyScript2RestLoader;

  /**
   * See {@link Session}.
   */
  private final Session                 session;

  /**
   * @param repository repository name
   * @param workspace workspace name
   * @param groovyScript2RestLoader See {@link GroovyScript2RestLoader}
   * @param session JCR session
   */
  public GroovyScript2RestUpdateListener(String repository,
                                         String workspace,
                                         GroovyScript2RestLoader groovyScript2RestLoader,
                                         Session session) {
    this.repository = repository;
    this.workspace = workspace;
    this.groovyScript2RestLoader = groovyScript2RestLoader;
    this.session = session;
  }

  /**
   * {@inheritDoc}
   */
  public void onEvent(EventIterator eventIterator) {
    // waiting for Event.PROPERTY_ADDED, Event.PROPERTY_REMOVED,
    // Event.PROPERTY_CHANGED
    try {
      while (eventIterator.hasNext()) {
        Event event = eventIterator.nextEvent();
        String path = event.getPath();

        if (path.endsWith("/jcr:data")) {
          // jcr:data removed 'exo:groovyResourceContainer' then unbind resource
          if (event.getType() == Event.PROPERTY_REMOVED) {
            unloadScript(path.substring(0, path.lastIndexOf('/')));
          } else if (event.getType() == Event.PROPERTY_ADDED
              || event.getType() == Event.PROPERTY_CHANGED) {
            Node node = session.getItem(path).getParent();
            if (node.getProperty("exo:autoload").getBoolean())
              loadScript(node);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Process event failed. ", e);
    }
  }

  /**
   * Load script form supplied node.
   * 
   * @param node JCR node
   * @throws Exception if any error occurs
   */
  private void loadScript(Node node) throws Exception {
    ScriptKey key = new NodeScriptKey(repository, workspace, node);
    if (groovyScript2RestLoader.isLoaded(key))
      groovyScript2RestLoader.unloadScript(key);
    groovyScript2RestLoader.loadScript(key, node.getPath(), node.getProperty("jcr:data")
                                                                .getStream());
  }

  /**
   * Unload script.
   * 
   * @param path unified JCR node path
   * @throws Exception if any error occurs
   */
  private void unloadScript(String path) throws Exception {
    ScriptKey key = new NodeScriptKey(repository, workspace, path);
    if (groovyScript2RestLoader.isLoaded(key))
      groovyScript2RestLoader.unloadScript(key);
  }

}

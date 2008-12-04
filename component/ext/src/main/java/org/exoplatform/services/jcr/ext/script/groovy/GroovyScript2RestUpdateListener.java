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

import org.exoplatform.services.jcr.ext.resource.UnifiedNodeReference;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScript2RestUpdateListener implements EventListener {

  private final String                  repository;

  private final String                  workspace;

  private final GroovyScript2RestLoader groovyScript2RestLoader;

  private final Session                 session;

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
    // waiting for Event.PROPERTY_ADDED, Event.PROPERTY_CHANGED, Event.PROPERTY_CHANGED
    try {
      while (eventIterator.hasNext()) {
        Event event = eventIterator.nextEvent();
        String path = event.getPath();

        // jcr:data is mandatory for node-type 'exo:groovyResourceContainer'
        // remove with node only, so unbind resource
        if (event.getType() == Event.PROPERTY_REMOVED && path.endsWith("/jcr:data")) {
          path = path.substring(0, path.lastIndexOf('/'));
          unloadScript(path);
          // nothing to do, node (script) removed
          break;
        }

        // interesting about change script source code
        if (path.endsWith("/jcr:data")) {
          Node node = session.getItem(path).getParent();
          // check is script should be automatically loaded
          if (node.getProperty("exo:autoload").getBoolean()) {
            if (event.getType() == Event.PROPERTY_CHANGED) {
              unloadScript(node);
              loadScript(node);
            } else {
              // if Event.PROPERTY_ADDED
              loadScript(node);
            }
          }
        }
        // property 'exo:load' changed, if it false script should be removed
        // from ResourceBinder. Not care about Event.ADDED_NODE it will be catched
        // by path.endsWith("/jcr:data").
        if (path.endsWith("/exo:load") && event.getType() == Event.PROPERTY_CHANGED) {
          Node node = session.getItem(path).getParent();
          if (node.getProperty("exo:load").getBoolean())
            loadScript(node);
          else
            unloadScript(node);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void loadScript(Node node) throws Exception {
    String unifiedNodePath = new UnifiedNodeReference(repository, workspace, node.getPath()).getURL()
                                                                                            .toString();
    groovyScript2RestLoader.loadScript(unifiedNodePath, node.getProperty("jcr:data").getStream());
    node.setProperty("exo:load", true);
  }

  private void unloadScript(Node node) throws Exception {
    node.setProperty("exo:load", false);
    unloadScript(node.getPath());
  }

  private void unloadScript(String path) throws Exception {
    String unifiedNodePath = new UnifiedNodeReference(repository, workspace, path).getURL()
                                                                                  .toString();
    groovyScript2RestLoader.unloadScript(unifiedNodePath);
  }

}

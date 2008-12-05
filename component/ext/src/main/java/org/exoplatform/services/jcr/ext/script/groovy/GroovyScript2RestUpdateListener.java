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
    // waiting for Event.PROPERTY_ADDED, Event.PROPERTY_REMOVED, Event.PROPERTY_CHANGED
    try {
      while (eventIterator.hasNext()) {
        Event event = eventIterator.nextEvent();
        String path = event.getPath();

        Node node = null;
        if (event.getType() != Event.PROPERTY_REMOVED)
          node = session.getItem(path).getParent();

        if (path.endsWith("/jcr:data")) {
          switch (event.getType()) {
          case Event.PROPERTY_REMOVED:
            // jcr:data is mandatory for node-type 'exo:groovyResourceContainer'
            // remove with node only, so unbind resource
            unloadScript(path.substring(0, path.lastIndexOf('/')));
            break;
          case Event.PROPERTY_CHANGED:
            // interesting about change script source code
            if (node.getProperty("exo:autoload").getBoolean()) {
              // check is script should be automatically loaded
              unloadScript(node);
//              loadScript(node);
              node.setProperty("exo:load", true);
              session.save();
            }
            break;
          case Event.PROPERTY_ADDED:
            if (node.getProperty("exo:autoload").getBoolean()) {
//            loadScript(node);
              node.setProperty("exo:load", true);
              session.save();
            }
            break;
          }
        } else if (path.endsWith("/exo:load") && event.getType() == Event.PROPERTY_CHANGED) {
          // property 'exo:load' changed, if it false script should be removed from ResourceBinder.
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
  }

  private void unloadScript(Node node) throws Exception {
    unloadScript(node.getPath());
  }

  private void unloadScript(String path) throws Exception {
    String unifiedNodePath = new UnifiedNodeReference(repository, workspace, path).getURL()
                                                                                  .toString();
    groovyScript2RestLoader.unloadScript(unifiedNodePath);
  }

}

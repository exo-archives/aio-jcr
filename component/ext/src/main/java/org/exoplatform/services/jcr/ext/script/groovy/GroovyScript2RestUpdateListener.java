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

  private String repository;
  private String workspace;
  private GroovyScript2RestLoader groovyScript2RestLoader;
  private Session session;

  public GroovyScript2RestUpdateListener(String repository, String workspace,
      GroovyScript2RestLoader groovyScript2RestLoader,
      Session session) {
    this.repository = repository;
    this.workspace = workspace;
    this.groovyScript2RestLoader = groovyScript2RestLoader;
    this.session = session;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.observation.EventListener#onEvent(javax.jcr.observation.EventIterator)
   */
  public void onEvent(EventIterator eventIterator) {
    // waiting for Event.PROPERTY_ADDED and Event.PROPERTY_CHANGED
    try {
      while (eventIterator.hasNext()) {
        Event event = eventIterator.nextEvent();
        String path = event.getPath();

        // interesting about change script source code
        if (path.endsWith("/jcr:data")) {
          String nodePath = path.substring(0, path.lastIndexOf("/jcr:data"));

          // check is script should be automatically loaded
          if (session.getRootNode().getNode(nodePath.substring(1)).getProperty(
              "exo:autoload").getBoolean()) {
            UnifiedNodeReference unifiedNodeReference = new UnifiedNodeReference(
                repository, workspace, nodePath);
            if (event.getType() == Event.PROPERTY_CHANGED) {
              groovyScript2RestLoader.unloadScript(unifiedNodeReference.getURL());
              groovyScript2RestLoader.loadScript(unifiedNodeReference.getURL());
            } else {
              // if Event.PROPERTY_ADDED 
              groovyScript2RestLoader.loadScript(unifiedNodeReference.getURL());
            }
          }
        }
        // property 'exo:autoload' changed, if it false script should be removed
        // from ResourceBinder. Not care about Event.ADDED_NODE it will be catched 
        // by path.endsWith("/jcr:data").
        if (path.endsWith("/exo:autoload") && event.getType() == Event.PROPERTY_CHANGED) {
          String nodePath = path.substring(0, path.lastIndexOf("/exo:autoload"));
          UnifiedNodeReference unifiedNodeReference = new UnifiedNodeReference(
              repository, workspace, nodePath);
          if (session.getRootNode().getNode(nodePath.substring(1)).getProperty(
          "exo:autoload").getBoolean()) {
            groovyScript2RestLoader.loadScript(unifiedNodeReference.getURL());
          } else {
            groovyScript2RestLoader.unloadScript(unifiedNodeReference.getURL());
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

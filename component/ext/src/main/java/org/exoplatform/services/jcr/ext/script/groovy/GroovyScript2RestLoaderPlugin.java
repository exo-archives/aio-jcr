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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class GroovyScript2RestLoaderPlugin extends BaseComponentPlugin {

  /** Logger. */
  private static final Log           LOG = ExoLogger.getLogger(GroovyScript2RestLoaderPlugin.class.getName());

  /** Configurations for scripts what were got from XML. */
  private List<XMLGroovyScript2Rest> l   = new ArrayList<XMLGroovyScript2Rest>();

  /** Repository. */
  private String                     repository;

  /** Workspace. */
  private String                     workspace;

  /** Root node for scripts. If it does not exist new one will be created. */
  private String                     node;

  @SuppressWarnings("unchecked")
  public GroovyScript2RestLoaderPlugin(InitParams params) {
    repository = params.getValueParam("repository").getValue();
    workspace = params.getValueParam("workspace").getValue();
    node = params.getValueParam("node").getValue();
    Iterator<PropertiesParam> iterator = params.getPropertiesParamIterator();
    while (iterator.hasNext()) {
      PropertiesParam p = iterator.next();
      String name = p.getName();
      boolean autoload = Boolean.valueOf(p.getProperty("autoload"));
      String path = p.getProperty("path");
      if (LOG.isDebugEnabled())
        LOG.debug("Read new script configuration " + name);
      l.add(new XMLGroovyScript2Rest(name, path, autoload));
    }
  }

  public List<XMLGroovyScript2Rest> getXMLConfigs() {
    return l;
  }

  /**
   * @return the repository
   */
  public String getRepository() {
    return repository;
  }

  /**
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * @return the node
   */
  public String getNode() {
    return node;
  }

}

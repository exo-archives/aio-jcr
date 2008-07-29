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

package org.exoplatform.services.jcr.ext.resource;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * 
 * @since 1.9
 */
public class NodeRepresentationFactoryLoaderPlugin extends BaseComponentPlugin {

  private Map<String, String> factories_ = new HashMap<String, String>();
  
  public NodeRepresentationFactoryLoaderPlugin(InitParams params) {
    if (params != null) {
      PropertiesParam pparams = params.getPropertiesParam("node-representation-factories");
      if (pparams != null)
        factories_ = pparams.getProperties();
    }
  }
  
  /**
   * @return map where key node type and value names of classes corresponding factories.
   */
  public Map<String, String> getFactories() {
    return factories_;
  }
  
}

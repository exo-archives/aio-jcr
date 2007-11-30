/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

package org.exoplatform.services.jcr.impl.storage.value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.ValueStorageEntry;
import org.exoplatform.services.jcr.config.ValueStorageFilterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.jcr.storage.value.ValuePluginFilter;
import org.exoplatform.services.jcr.storage.value.ValueStoragePlugin;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. Per-workspace factory object for
 * ValueStoragePlugin
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: StandaloneStoragePluginProvider.java 13463 2007-03-16 09:17:29Z
 *          geaz $
 */

public class StandaloneStoragePluginProvider extends ArrayList<ValueStoragePlugin> implements ValueStoragePluginProvider {

  private static final long serialVersionUID = 4537116106932443262L;

  private static Log        log              = ExoLogger.getLogger("jcr.ValueStoragePluginFactory");

  public StandaloneStoragePluginProvider(WorkspaceEntry wsConfig) throws RepositoryConfigurationException,
      IOException {

    List<ValueStorageEntry> storages = wsConfig.getContainer().getValueStorages();

    if (storages != null)
      for (ValueStorageEntry storageEntry : storages) {
        
        // can be only one storage with given id
        for (ValueStoragePlugin vsp : this) {
          if (vsp.getId().equals(storageEntry.getId()))
            throw new RepositoryConfigurationException("Value storage with ID '" + storageEntry.getId() + "' already exists");
        }
        
        Object o = null;
        try {
          o = Class.forName(storageEntry.getType()).newInstance();
        } catch (Exception e) {
          log.error("Value Storage Plugin instantiation FAILED. ", e);
          continue;
        }
        if (!(o instanceof ValueStoragePlugin)) {
          log.error("Not a ValueStoragePlugin object IGNORED: " + o);
          continue;
        }

        ValueStoragePlugin plugin = (ValueStoragePlugin) o;
        // init filters
        ArrayList<ValuePluginFilter> filters = new ArrayList<ValuePluginFilter>();
        List<ValueStorageFilterEntry> filterEntries = storageEntry.getFilters();
        for (ValueStorageFilterEntry filterEntry : filterEntries) {
          ValuePluginFilter filter = new ValuePluginFilter(
              PropertyType.valueFromName(filterEntry.getPropertyType()), 
              null, null, filterEntry.getMinValueSize());
          filters.add(filter);
        }

        // init properties
        Properties props = new Properties();
        List<SimpleParameterEntry> paramEntries = storageEntry.getParameters();
        for (SimpleParameterEntry paramEntry : paramEntries) {
          props.setProperty(paramEntry.getName(), paramEntry.getValue());
        }

        plugin.init(props);
        plugin.setId(storageEntry.getId());
        plugin.setFilters(filters);

        add(plugin);
        log.info("Value Storage Plugin initialized " + plugin);
      }
  }

  /**
   * @param property
   * @return ValueIOChannel appropriate for this property (by path, id etc) or
   *         null if no such channel found
   * @throws IOException
   */
  public ValueIOChannel getApplicableChannel(PropertyData property, int valueOrderNumer) throws IOException {
    Iterator<ValueStoragePlugin> plugins = iterator();
    while (plugins.hasNext()) {
      ValueStoragePlugin plugin = plugins.next();
      List<ValuePluginFilter> filters = plugin.getFilters();
      for (ValuePluginFilter filter : filters) {
        if (filter.match(property, valueOrderNumer))
          return plugin.openIOChannel();
      }
    }
    return null;
  }

  public Iterator<ValueStoragePlugin> plugins() {
    return iterator();
  }

  public void checkConsistency(WorkspaceStorageConnection dataConnection) {
    Iterator<ValueStoragePlugin> plugins = plugins();
    while (plugins.hasNext()) {
      ValueStoragePlugin plugin = plugins.next();
      plugin.checkConsistency(dataConnection);
    }
  }

  public ValueIOChannel getChannel(String storageId,PropertyData prop) throws IOException, ValueDataNotFoundException {
    Iterator<ValueStoragePlugin> plugins = iterator();
    while (plugins.hasNext()) {
      ValueStoragePlugin plugin = plugins.next();
      if (plugin.match(storageId, prop)) {
        return plugin.openIOChannel();
      }
    }
    throw new ValueDataNotFoundException("No value storage found for value "
        + storageId + " from property " + prop.getQPath());
  }
}

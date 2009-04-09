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
package org.exoplatform.services.jcr.storage.value;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.impl.storage.value.ValueDataResourceHolder;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueStoragePlugin.java 11907 2008-03-13 15:36:21Z ksm $
 */

public abstract class ValueStoragePlugin {

  protected List<ValuePluginFilter> filters;

  protected String                  id;

  /**
   * Initialize this plugin.
   * 
   * @param props
   *          configuration Properties
   * @param resources
   *          ValueDataResourceHolder
   * @throws RepositoryConfigurationException
   *           if config error
   * @throws IOException
   *           if IO error
   */
  public abstract void init(Properties props, ValueDataResourceHolder resources) throws RepositoryConfigurationException,
                                                                                IOException;

  /**
   * Open ValueIOChannel.
   * 
   * @return ValueIOChannel
   * @throws IOException
   *           if error occurs
   */
  public abstract ValueIOChannel openIOChannel() throws IOException;

  /**
   * Return filters.
   * 
   * @return List of ValuePluginFilter
   */
  public final List<ValuePluginFilter> getFilters() {
    return filters;
  }

  /**
   * Set filters.
   * 
   * @param filters
   *          List of ValuePluginFilter
   */
  public final void setFilters(List<ValuePluginFilter> filters) {
    this.filters = filters;
  }

  /**
   * Get Stirage Id.
   * 
   * @return String
   */
  public final String getId() {
    return id;
  }

  /**
   * Set Storage Id.
   * 
   * @param id
   *          String
   */
  public final void setId(String id) {
    this.id = id;
  }

  /**
   * Run consistency check operation.
   * 
   * @param dataConnection
   *          - connection to metadata storage
   */
  public abstract void checkConsistency(WorkspaceStorageConnection dataConnection);

  /**
   * Check criteria match.
   * 
   * @param valueDataDescriptor
   *          String
   * @return boolean, tru iof match
   */
  public abstract boolean match(String valueDataDescriptor);

}

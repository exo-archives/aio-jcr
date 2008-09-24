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
package org.exoplatform.services.jcr.config;

import java.io.InputStream;
import java.util.List;

import javax.jcr.RepositoryException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov </a>
 * @version $Id: RepositoryServiceConfiguration.java 2038 2005-10-05 16:50:11Z geaz $
 */

public class RepositoryServiceConfiguration {

  private List<RepositoryEntry> repositoryConfigurations;

  private String                defaultRepositoryName;

  public final String getDefaultRepositoryName() {
    return defaultRepositoryName;
  }

  public final List<RepositoryEntry> getRepositoryConfigurations() {
    return repositoryConfigurations;
  }

  public final RepositoryEntry getRepositoryConfiguration(String name) throws RepositoryConfigurationException {

    for (int i = 0; i < repositoryConfigurations.size(); i++) {
      RepositoryEntry conf = repositoryConfigurations.get(i);
      if (conf.getName().equals(name))
        return conf;
    }
    throw new RepositoryConfigurationException("Repository not configured " + name);
  }

  protected final void init(InputStream is) throws RepositoryConfigurationException {
    try {
      IBindingFactory factory = BindingDirectory.getFactory(RepositoryServiceConfiguration.class);
      IUnmarshallingContext uctx = factory.createUnmarshallingContext();
      RepositoryServiceConfiguration conf = (RepositoryServiceConfiguration) uctx.unmarshalDocument(is,
                                                                                                    null);

      this.defaultRepositoryName = conf.getDefaultRepositoryName();
      this.repositoryConfigurations = conf.getRepositoryConfigurations();
    } catch (JiBXException e) {
      e.printStackTrace();
      throw new RepositoryConfigurationException("Error in config initialization " + e);
    }
  }

  /**
   * Checks if current configuration can be saved.
   * 
   * @return
   */
  public boolean isRetainable() {
    return false;
  }

  /**
   * Saves current configuration to persistent.
   * 
   * @throws RepositoryException
   */
  public void retain() throws RepositoryException {
  }

}

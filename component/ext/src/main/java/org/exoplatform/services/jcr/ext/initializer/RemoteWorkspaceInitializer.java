/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.initializer;

import java.io.File;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NamespaceRegistryImpl;
import org.exoplatform.services.jcr.impl.core.SysViewWorkspaceInitializer;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 16.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: RemoteWorkspaceInitialization.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RemoteWorkspaceInitializer extends SysViewWorkspaceInitializer {

  public RemoteWorkspaceInitializer(WorkspaceEntry config,
                                    RepositoryEntry repConfig,
                                    CacheableWorkspaceDataManager dataManager,
                                    NamespaceRegistryImpl namespaceRegistry,
                                    LocationFactory locationFactory,
                                    NodeTypeManagerImpl nodeTypeManager,
                                    ValueFactoryImpl valueFactory,
                                    AccessManager accessManager) throws RepositoryConfigurationException,
      PathNotFoundException,
      RepositoryException {
    super(config,
          repConfig,
          dataManager,
          namespaceRegistry,
          locationFactory,
          nodeTypeManager,
          valueFactory,
          accessManager,
          null);

    if (!isWorkspaceInitialized()) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      RemoteWorkspaceInitializationService workspaceInitializationService = (RemoteWorkspaceInitializationService) container.getComponentInstanceOfType(RemoteWorkspaceInitializationService.class);

      File f;
      try {
        f = workspaceInitializationService.getWorkspaceData(repConfig.getName(), config.getName());
      } catch (RemoteWorkspaceInitializationException e) {
        throw new RepositoryException("Can not get remote workspace data :" + e.getMessage(), e);
      }

      this.restorePath = f.getAbsolutePath();
    }
  }

}

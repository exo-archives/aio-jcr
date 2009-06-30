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
package org.exoplatform.services.jcr.impl;

import javax.jcr.RepositoryException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.exoplatform.services.log.Log;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterFactory;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.core.SessionFactory;
import org.exoplatform.services.jcr.impl.core.WorkspaceInitializer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.jmx.annotations.NamingContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: WorkspaceContainer.java 11907 2008-03-13 15:36:21Z ksm $
 */

@Managed
@NameTemplate({
  @Property(key="container", value="workspace"),
  @Property(key="name", value="{Name}")})
@NamingContext(@Property(key="workspace", value="{Name}"))
public class WorkspaceContainer extends ExoContainer {

  protected static Log              log = ExoLogger.getLogger("jcr.WorkspaceContainer");

  private final String              name;

  private final RepositoryContainer repositoryContainer;

  public WorkspaceContainer(RepositoryContainer parent, WorkspaceEntry config) throws RepositoryException,
      RepositoryConfigurationException {

    // Before repository instantiation
    super(new MX4JComponentAdapterFactory(), parent);

    repositoryContainer = parent;
    this.name = config.getName();
  }

  // Components access methods -------

  @Managed
  @ManagedDescription("The workspace container name")
  public String getName() {
    return name;
  }

  public SessionFactory getSessionFactory() {
    return (SessionFactory) getComponentInstanceOfType(SessionFactory.class);
  }

  public WorkspaceInitializer getWorkspaceInitializer() {
    return (WorkspaceInitializer) getComponentInstanceOfType(WorkspaceInitializer.class);
  }

  /*
   * (non-Javadoc)
   * @see org.picocontainer.defaults.DefaultPicoContainer#stop()
   */
  @Override
  public void stop() {
    try {
      stopContainer();
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }
    super.stop();
  }

}

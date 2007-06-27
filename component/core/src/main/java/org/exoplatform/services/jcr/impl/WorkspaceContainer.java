/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl;

import javax.jcr.RepositoryException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterFactory;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.core.SessionFactory;
import org.exoplatform.services.jcr.impl.core.WorkspaceInitializer;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: WorkspaceContainer.java 13463 2007-03-16 09:17:29Z geaz $
 */

public class WorkspaceContainer extends ExoContainer {

  protected static Log log = ExoLogger.getLogger("jcr.WorkspaceContainer");
  
  private String name;

  private RepositoryContainer repositoryContainer;

  private MBeanServer mbeanServer;

  public WorkspaceContainer(RepositoryContainer parent, WorkspaceEntry config)
      throws RepositoryException, RepositoryConfigurationException {

    // Before repository instantiation
    super(new MX4JComponentAdapterFactory(), parent);

    repositoryContainer = parent;
    this.name = config.getName();

    this.mbeanServer = MBeanServerFactory.createMBeanServer("jcrws" + name
        + "at" + repositoryContainer.getName() + "mx");
  }

  public MBeanServer getMBeanServer() {
    return mbeanServer;
  }

  // Components access methods -------

  public SessionFactory getSessionFactory() {
    return (SessionFactory)getComponentInstanceOfType(SessionFactory.class);
  }
  
  public WorkspaceInitializer getWorkspaceInitializer() {
    return (WorkspaceInitializer)getComponentInstanceOfType(WorkspaceInitializer.class);
  }

}
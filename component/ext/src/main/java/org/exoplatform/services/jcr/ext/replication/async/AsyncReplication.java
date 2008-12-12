/**
 * 
 */
/*
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
package org.exoplatform.services.jcr.ext.replication.async;

import javax.jcr.RepositoryException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class AsyncReplication implements Startable {

  protected final ManageableRepository repository;
  
  public AsyncReplication(RepositoryService repoService, InitParams params) throws RepositoryException, RepositoryConfigurationException {
    this.repository = repoService.getDefaultRepository();
    
    // TODO params to a local var(s)
    
    // TODO restore previous state if it's restart
    // handle local restoration or cleanups of unfinished or breaked work
    
    // Ready to begin...
    // TODO Init 
    init();
  }
  
  /**
   * Initializer.
   *
   */
  private void init() {
    // TODO add listeners to a Repository Workspaces
    
    // TODO Initialize schedulling for AsyncInitializer on high priority node.
    
  }

  /**
   * {@inheritDoc}
   */
  public void start() {
    // TODO start after the JCR Repo started 
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    // TODO stop after the JCR Repo stopped
  }
}

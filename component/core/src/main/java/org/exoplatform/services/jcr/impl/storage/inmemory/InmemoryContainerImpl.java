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
package org.exoplatform.services.jcr.impl.storage.inmemory;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: InmemoryContainerImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class InmemoryContainerImpl extends WorkspaceDataContainerBase {

  private static Log log = ExoLogger.getLogger("jcr.InmemoryContainerImpl");

  private String     name;

  public InmemoryContainerImpl(WorkspaceEntry wsEntry) throws RepositoryException {

    this.name = wsEntry.getUniqueName();
    log.debug("ContainerImpl() name: " + name);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#getName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#getInfo()
   */
  public String getInfo() {
    String str = "Info: Inmemory (for testing only) based container \n";
    str += "Name: " + name + "\n";
    return str;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#openConnection()
   */
  public WorkspaceStorageConnection openConnection() {
    return new InmemoryStorageConnection(name);
  }

  public WorkspaceStorageConnection reuseConnection(WorkspaceStorageConnection original) throws RepositoryException {
    return openConnection();
  }

  public String getStorageVersion() {
    return "1.0";
  }

}

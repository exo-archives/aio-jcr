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
package org.exoplatform.services.jcr.storage;

import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCStorageConnection;

/**
 * Created by The eXo Platform SAS.<br>
 * 
 * Serves repository workspace persistent storage.
 * Acts as factory for WorkspaceStorageConnection objects, the implementation should support thread safety for openConnection() method;  
 * 
 * @author Gennady Azarenkov
 * @version $Id: WorkspaceDataContainer.java 11907 2008-03-13 15:36:21Z ksm $
 */

public interface WorkspaceDataContainer extends DataContainer {
 
  /**
   * [G.A] do we need it here or in WorkspaceDataManager better??
   * @return current time as for this container env
   */
  Calendar getCurrentTime();
  
  /**
   * @return the new connection to workspace storage
   * normally implementation of this method should be synchronized
   */
  WorkspaceStorageConnection openConnection() throws RepositoryException ;
  
  /**
   * @return the connection to workspace storage, 
   *         if it possible the connection will use same physical resource (already obtained)
   *         as original connection, otherwise same behaviour will be used as for openConnection(). 
   *           
   * normally implementation of this method should be synchronized
   */
  WorkspaceStorageConnection reuseConnection(WorkspaceStorageConnection original) throws RepositoryException;

}
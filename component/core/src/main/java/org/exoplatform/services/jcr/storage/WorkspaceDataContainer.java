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

/**
 * Created by The eXo Platform SAS.<br>
 * 
 * Serves repository workspace persistent storage. Acts as factory for WorkspaceStorageConnection
 * objects, the implementation should support thread safety for openConnection() method;
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public interface WorkspaceDataContainer extends DataContainer {

  // configuration params

  public final static String CONTAINER_NAME    = "containerName";

  public final static String SOURCE_NAME       = "source-name";

  public final static String MULTIDB           = "multi-db";

  public final static String SINGLEDB          = "single-db";

  public final static String MAXBUFFERSIZE     = "max-buffer-size";

  public final static String SWAPDIR           = "swap-directory";

  public final static int    DEF_MAXBUFFERSIZE = 1024 * 200;                          // 200k

  public final static String DEF_SWAPDIR       = System.getProperty("java.io.tmpdir");

  /**
   * [G.A] do we need it here or in WorkspaceDataManager better??
   * 
   * @return current time as for this container env
   */
  Calendar getCurrentTime();

  /**
   * Status of write-operations restrictions.
   * 
   * Read-only status is descriptive within the container, i.e. will not prevent any write
   * operation.
   * 
   * Used in DataManager implementations.
   * 
   * @return true - if write-operations allowed, false - otherwise.
   */
  boolean isReadOnly();

  /**
   * Set status of write-operations restrictions.
   * 
   * Read-only status is descriptive within the container, i.e. will not prevent any write
   * operation.
   * 
   * Used in DataManager implementations.
   * 
   * @param status
   *          , true - if write-operations allowed, false - otherwise.
   */
  void setReadOnly(boolean status);

  /**
   * @return the new connection to workspace storage normally implementation of this method should
   *         be synchronized
   */
  WorkspaceStorageConnection openConnection() throws RepositoryException;

  /**
   * @return the connection to workspace storage, if it possible the connection will use same
   *         physical resource (already obtained) as original connection, otherwise same behaviour
   *         will be used as for openConnection().
   * 
   *         normally implementation of this method should be synchronized
   */
  WorkspaceStorageConnection reuseConnection(WorkspaceStorageConnection original) throws RepositoryException;

}

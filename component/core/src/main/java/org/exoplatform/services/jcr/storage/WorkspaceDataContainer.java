/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.storage;

import java.util.Calendar;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL .<br>
 * 
 * Serves repository workspace persistent storage.
 * Acts as factory for WorkspaceStorageConnection objects, the implementation should support thread safety for openConnection() method;  
 * 
 * @author Gennady Azarenkov
 * @version $Id: WorkspaceDataContainer.java 12843 2007-02-16 09:11:18Z peterit $
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

}
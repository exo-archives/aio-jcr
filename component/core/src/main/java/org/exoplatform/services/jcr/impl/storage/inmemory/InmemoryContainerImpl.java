/*
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.inmemory;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: InmemoryContainerImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class InmemoryContainerImpl extends WorkspaceDataContainerBase {

  private static Log log = ExoLogger.getLogger("jcr.InmemoryContainerImpl");

  private String name;

  public InmemoryContainerImpl(WorkspaceEntry wsEntry)
      throws RepositoryException {

    this.name = wsEntry.getUniqueName();
    log.debug("ContainerImpl() name: " + name);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceDataContainer#getInfo()
   */
  public String getInfo() {
    String str = "Info: Inmemory (for testing only) based container \n";
    str += "Name: " + name + "\n";
    return str;
  }

  /* (non-Javadoc)
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
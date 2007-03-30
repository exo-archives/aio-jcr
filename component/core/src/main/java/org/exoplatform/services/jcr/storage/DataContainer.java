/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.storage;


/**
 * Created by The eXo Platform SARL. <br/>
 * 
 * Abstract jcr data storage
 *
 * @author Gennady Azarenkov
 * @version $Id: DataContainer.java 12843 2007-02-16 09:11:18Z peterit $
 */

public abstract interface DataContainer {
  
  /**
   * @return some info about this container
   */
  String getInfo();
  
  /**
   * @return name of this container 
   */
  String getName();
  
  /**
   * @return current storage version 
   */
  String getStorageVersion();
  
  
  /**
   * check compatibility of underlying storage with current repository version and try to apply patch if there is suitable patch and the flag==true
   * @param repositoryVersion
   * @param apply
   * @throws RepositoryException if current repository version is not compatible with underlying strorage and patches could not be applied(no such patch or applyPatch==false)
   */
  //void manageCompatibility(String repositoryVersion, boolean applyPatch) throws RepositoryException;

}

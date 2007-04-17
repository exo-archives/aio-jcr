/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
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
  
}

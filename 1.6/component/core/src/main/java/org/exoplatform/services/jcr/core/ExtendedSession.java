/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core;

import javax.jcr.Session;

import org.exoplatform.services.jcr.impl.core.LocationFactory;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface ExtendedSession extends Session {
  
  String getId();
  
  /**
   * @return Returns the locationFactory.
   */
  LocationFactory getLocationFactory();
  

}

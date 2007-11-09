/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

public interface CIFSService {

  public void start();

  public void stop();

  public ServerConfiguration getConfiguration();

}

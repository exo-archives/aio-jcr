/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow.persistent;


/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: WorkspaceStorageCacheConfiguration.java 12843 2007-02-16 09:11:18Z peterit $
 */
public interface WorkspaceStorageCacheConfiguration {
  
  boolean isEnabled();
  void setEnabled(boolean enabled);
  
  int getMaxSize();
  void setMaxSize(int maxSize);
  
  long getLiveTime();
  void setLiveTime(long liveTime);
  
  int getOnChangePolicy();
  void setOnChangePolicy(int policy);
  
  //String getImplementation();
  //void setImplementation(String implementation);
}

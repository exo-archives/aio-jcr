/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class LocalWorkspaceDataManagerStub extends VersionableWorkspaceDataManager {
  
  private static Log log = ExoLogger.getLogger("jcr.LocalWorkspaceDataManagerStub");
  
  public LocalWorkspaceDataManagerStub(CacheableWorkspaceDataManager persistentManager) {
    super(persistentManager);
  }
}

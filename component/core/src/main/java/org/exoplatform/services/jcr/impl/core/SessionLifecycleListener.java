/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SessionLifecycleListener.java 12841 2007-02-16 08:58:38Z peterit $
 */

public interface SessionLifecycleListener {
  
  void onCloseSession(SessionImpl session);
}

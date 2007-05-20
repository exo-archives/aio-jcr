/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.app;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface SessionProviderService {
  void setSessionProvider(Object key, SessionProvider sessionProvider);
  SessionProvider getSessionProvider(Object key);
  void removeSessionProvider(Object key);
}

/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.app;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL        .
 * Session providers holder component 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface SessionProviderService {
  
  /**
   * @param key
   * @param sessionProvider
   */
  void setSessionProvider(Object key, SessionProvider sessionProvider);
  
  /**
   * @param key
   * @return session provider
   */
  SessionProvider getSessionProvider(Object key);
  
  /**
   * Removes the session provider
   * @param key
   */
  void removeSessionProvider(Object key);
}

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

public class ThreadLocalSessionProviderService implements SessionProviderService {


  private static ThreadLocal <SessionProvider> sessionProviderKeeper;
  
  public ThreadLocalSessionProviderService() {
    sessionProviderKeeper = new ThreadLocal <SessionProvider>();
  }
  
  public SessionProvider getSessionProvider(Object key) {
    if(sessionProviderKeeper.get() != null)
      return sessionProviderKeeper.get();
    else
      throw new NullPointerException("SessionProvider is not initialized");
  }

  public void setSessionProvider(Object key, SessionProvider sessionProvider) {
    sessionProviderKeeper.set(sessionProvider);
  }

  public void removeSessionProvider(Object key) {
    getSessionProvider(key).close();
    sessionProviderKeeper.set(null);
  }

}

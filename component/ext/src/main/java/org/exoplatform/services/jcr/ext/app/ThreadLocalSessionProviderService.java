/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.app;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL        .<br/>
 * SessionProviderService implementation where SessionProviders are stored in Thread Local.
 * In this implementation the KEY make no sense, null value can be passed as a key.   
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ThreadLocalSessionProviderService implements SessionProviderService {


  private static ThreadLocal <SessionProvider> sessionProviderKeeper;
  
  public ThreadLocalSessionProviderService() {
    sessionProviderKeeper = new ThreadLocal <SessionProvider>();
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#getSessionProvider(java.lang.Object)
   */
  public SessionProvider getSessionProvider(Object key) {
    if(sessionProviderKeeper.get() != null)
      return sessionProviderKeeper.get();
    else
      throw new NullPointerException("SessionProvider is not initialized");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#setSessionProvider(java.lang.Object, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public void setSessionProvider(Object key, SessionProvider sessionProvider) {
    sessionProviderKeeper.set(sessionProvider);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#removeSessionProvider(java.lang.Object)
   */
  public void removeSessionProvider(Object key) {
    getSessionProvider(key).close();
    sessionProviderKeeper.set(null);
  }

}

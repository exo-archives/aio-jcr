/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.app;

import java.util.HashMap;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL        .<br/>
 * SessionProviderService implementation where SessionProviders are stored as key-value pairs.
 * where key is any object, for instance user's credentials   
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class MapStoredSessionProviderService implements SessionProviderService{
  
  private HashMap <Object, SessionProvider> providers;

  public MapStoredSessionProviderService() {
    providers = new HashMap<Object, SessionProvider>();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#getSessionProvider(java.lang.Object)
   */
  public SessionProvider getSessionProvider(Object key) {
    if(providers.containsKey(key))
      return providers.get(key);
    else
      throw new NullPointerException("SessionProvider is not initialized");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#setSessionProvider(java.lang.Object, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public void setSessionProvider(Object key, SessionProvider sessionProvider) {
    providers.put(key, sessionProvider);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#removeSessionProvider(java.lang.Object)
   */
  public void removeSessionProvider(Object key) {
    getSessionProvider(key).close();
    providers.remove(key);
  }

}

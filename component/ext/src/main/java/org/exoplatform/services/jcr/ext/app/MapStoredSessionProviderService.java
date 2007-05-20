/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.app;

import java.util.HashMap;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class MapStoredSessionProviderService implements SessionProviderService{
  
  private HashMap <Object, SessionProvider> providers;

  public MapStoredSessionProviderService() {
    providers = new HashMap<Object, SessionProvider>();
  }

  public SessionProvider getSessionProvider(Object key) {
    if(providers.containsKey(key))
      return providers.get(key);
    else
      throw new NullPointerException("SessionProvider is not initialized");
  }

  public void setSessionProvider(Object key, SessionProvider sessionProvider) {
    providers.put(key, sessionProvider);
  }

  public void removeSessionProvider(Object key) {
    getSessionProvider(key).close();
    providers.remove(key);
  }

}

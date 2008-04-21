/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.ext.app;

import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS        .<br/>
 * SessionProviderService implementation where SessionProviders are stored in Thread Local.
 * In this implementation the KEY make no sense, null value can be passed as a key.   
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class ThreadLocalSessionProviderService implements SessionProviderService {


  private static ThreadLocal <SessionProvider> sessionProviderKeeper;
  private static ThreadLocal <SessionProvider> systemSessionProviderKeeper;
  
  public ThreadLocalSessionProviderService() {
    sessionProviderKeeper = new ThreadLocal <SessionProvider>();
    systemSessionProviderKeeper = new ThreadLocal <SessionProvider>();
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#getSessionProvider(java.lang.Object)
   */
  public SessionProvider getSessionProvider(Object key) {
    if(sessionProviderKeeper.get() != null)
      return sessionProviderKeeper.get();
    return null;
    //  throw new NullPointerException("SessionProvider is not initialized");
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#getSystemSessionProvider(java.lang.Object)
   */
  public SessionProvider getSystemSessionProvider(Object key) {
    if(systemSessionProviderKeeper.get() != null) {
      return systemSessionProviderKeeper.get();
    } 
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#setSessionProvider(java.lang.Object, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public void setSessionProvider(Object key, SessionProvider sessionProvider) {
    sessionProviderKeeper.set(sessionProvider);
    systemSessionProviderKeeper.set(SessionProvider.createSystemProvider());
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.app.SessionProviderService#removeSessionProvider(java.lang.Object)
   */
  public void removeSessionProvider(Object key) {
    getSessionProvider(key).close();
    sessionProviderKeeper.set(null);
    
    getSystemSessionProvider(key).close();
    systemSessionProviderKeeper.set(null);
  }

}

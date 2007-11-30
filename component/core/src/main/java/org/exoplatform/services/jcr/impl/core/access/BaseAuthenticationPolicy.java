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
package org.exoplatform.services.jcr.impl.core.access;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.SimpleCredentials;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AuthenticationPolicy;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.SecurityService;
import org.exoplatform.services.security.impl.CredentialsImpl;
import org.exoplatform.services.security.jaas.BasicCallbackHandler;

/**
 * Created by The eXo Platform SAS.<br/>
 * Abstract implementation of AuthenticationPolicy interface
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov
 *         </a>
 * @version $Id: BaseAuthenticationPolicy.java 12830 2007-02-15 11:12:26Z geaz $
 */
abstract public class BaseAuthenticationPolicy implements AuthenticationPolicy {
  
  protected static Log log = ExoLogger.getLogger("jcr.AuthenticationPolicy");
  protected RepositoryEntry config;
  
  protected SecurityService securityService;

  public BaseAuthenticationPolicy(RepositoryEntry config,
      SecurityService securityService) {
    this.config = config;
    this.securityService = securityService;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.access.AuthenticationPolicy#authenticate(javax.jcr.Credentials)
   */
  public final Credentials authenticate(Credentials credentials) throws LoginException {
    
    CredentialsImpl thisCredentials;
    if (credentials instanceof CredentialsImpl) {
      thisCredentials = (CredentialsImpl) credentials;
    } else if (credentials instanceof SimpleCredentials) {
      String name = ((SimpleCredentials) credentials).getUserID();
      char[] pswd = ((SimpleCredentials) credentials).getPassword();
      thisCredentials = new CredentialsImpl(name, pswd);
    } else
      throw new LoginException(
          "Credentials for the authentication should be CredentialsImpl or SimpleCredentials type");
    
    doAuthentication(thisCredentials); // throws LoginException
    
    log.debug("Repository.login() authenticated "
        + thisCredentials.getUserID());

    return thisCredentials;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.access.AuthenticationPolicy#authenticate()
   */
  abstract public Credentials authenticate() throws LoginException;
  
  private void doAuthentication(CredentialsImpl credentials) throws LoginException {

    // SYSTEM
    if (credentials.getUserID().equals(SystemIdentity.SYSTEM))
      return;

    // prepare to new login
    // uses BasicCallbackHandler
    CallbackHandler handler = new BasicCallbackHandler(credentials
        .getUserID(), credentials.getPassword());

    // and try to login
    try {

      LoginContext loginContext = new LoginContext(config.getSecurityDomain(),
          handler);
      loginContext.login();
    } catch (javax.security.auth.login.LoginException e) {
      throw new LoginException("Login failed for " + credentials.getUserID()
          + " " + e);
    }
    log.debug("Logged "+credentials.getUserID());
  }
  
}

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

import java.util.Iterator;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.security.auth.Subject;

import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS.<br/>
 * 
 * This authenticator uses identity stored in Security Service
 * 
 * @author Gennady Azarenkov
 * @version $Id: PortalAuthenticationPolicy.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class PortalAuthenticationPolicy extends BaseAuthenticationPolicy {  
  private static final Credentials SYSTEM_CRED = new CredentialsImpl(SystemIdentity.ANONIM, new char[0]);
  private AuthenticationService authService_ ;
  
  public PortalAuthenticationPolicy(RepositoryEntry config, AuthenticationService service) {
    super(config, null);
    authService_ =  service ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.access.BaseAuthenticationPolicy#authenticate()
   */
  public Credentials authenticate() throws LoginException {
    Credentials cred = null;
    Subject subj = authService_.getCurrentIdentity().getSubject();
    
    if (subj == null)
      throw new LoginException("PortalAuthenticationPolicy: current subject is not found in security service");
    
    Iterator credentials = subj.getPublicCredentials().iterator();
    while(credentials.hasNext()) {
      Object tmp = credentials.next(); 
      if(tmp instanceof Credentials) {
        cred = (Credentials)tmp;
        if(log.isDebugEnabled()) 
          log.debug("PortalAuthenticationPolicy.authenticate() found credentials " + cred
             + " in SecurityService");
        return cred;
      }
    } 
    return SYSTEM_CRED;  
  }
}

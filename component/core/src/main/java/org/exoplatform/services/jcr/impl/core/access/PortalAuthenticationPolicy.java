/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.access;

import javax.jcr.Credentials;
import javax.jcr.LoginException;

import org.exoplatform.container.SessionContainer;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.impl.core.CredentialsImpl;
import org.exoplatform.services.security.SecurityService;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * This authenticator uses identity stored in current SessionContainer
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov
 *         </a>
 * @version $Id: PortalAuthenticationPolicy.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class PortalAuthenticationPolicy extends BaseAuthenticationPolicy {
  
  public PortalAuthenticationPolicy(RepositoryEntry config,
      SecurityService securityService) {
    super(config, securityService);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.access.BaseAuthenticationPolicy#authenticate()
   */
  public Credentials authenticate() throws LoginException {
    
    CredentialsImpl thisCredentials;

    // Try to reuse Host's identity
    SessionContainer sessionContainer = SessionContainer.getInstance();
    if (sessionContainer != null) {
      String userId = SessionContainer.getInstance().getRemoteUser();
      
      // Anonimous user
      if (userId == null)
        userId = SystemIdentity.ANONIM;
      
      thisCredentials = new CredentialsImpl(userId, new char[0]);

      log.debug("Repository.login() gets user  " + userId
          + " from SessionContainer");
    } else {
      throw new LoginException(
          "Current User should be authenticated externally but was not.");
    }
    
    return thisCredentials;
  }

}

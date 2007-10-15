/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.access;

import javax.jcr.Credentials;
import javax.jcr.LoginException;

import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.security.SecurityService;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: RemoteAuthenticationPolicy.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class RemoteAuthenticationPolicy extends BaseAuthenticationPolicy {
  
  public RemoteAuthenticationPolicy(RepositoryEntry config,
      SecurityService securityService) {
    super(config, securityService);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.access.BaseAuthenticationPolicy#authenticate()
   */
  public Credentials authenticate() throws LoginException {
    return new CredentialsImpl("admin", "admin".toCharArray());
  }

}

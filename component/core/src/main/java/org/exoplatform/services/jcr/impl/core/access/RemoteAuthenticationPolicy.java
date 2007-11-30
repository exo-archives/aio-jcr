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

import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.security.SecurityService;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS.
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

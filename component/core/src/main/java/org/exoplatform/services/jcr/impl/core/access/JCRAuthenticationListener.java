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

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * May 17, 2007  
 * 
 * @version $Id$
 */
public class JCRAuthenticationListener extends Listener<AuthenticationService, Identity> {
  
  private Log      log = ExoLogger.getLogger("kernel.JCRAuthenticationListener");
  
  public void onEvent(Event<AuthenticationService, Identity> event)  {
    Identity identity = event.getData() ;
    String username = identity.getUsername() ;
    Subject subject = identity.getSubject() ;
    subject.getPublicCredentials().add(new CredentialsImpl(username, "".toCharArray()));
    
    if (log.isDebugEnabled())
      log.debug("Call JCRAuthenticationListener");
  }
}

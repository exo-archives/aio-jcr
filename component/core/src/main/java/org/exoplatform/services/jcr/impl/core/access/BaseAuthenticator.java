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

import java.util.HashSet;

import javax.jcr.LoginException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.access.AuthenticationPolicy;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS.<br/>
 * Abstract implementation of AuthenticationPolicy interface
 * 
 * @author eXo Platform
 * @version $Id$
 */
abstract public class BaseAuthenticator implements AuthenticationPolicy {

  protected static Log       log = ExoLogger.getLogger("jcr.BaseAuthenticator");

  protected RepositoryEntry  config;

  protected IdentityRegistry identityRegistry;

  public BaseAuthenticator(RepositoryEntry config, IdentityRegistry identityRegistry) {
    this.config = config;
    this.identityRegistry = identityRegistry;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.access.AuthenticationPolicy#authenticate()
   */
  public ConversationState authenticate() throws LoginException {

    ConversationState state = ConversationState.getCurrent();

    if (state == null) {
      log.debug("No current identity found, ANONYMOUS one will be used");
      return new ConversationState(new Identity(SystemIdentity.ANONIM,
                                                new HashSet<MembershipEntry>()));
    }

    ConversationState.setCurrent(state);
    return state;

  }

}

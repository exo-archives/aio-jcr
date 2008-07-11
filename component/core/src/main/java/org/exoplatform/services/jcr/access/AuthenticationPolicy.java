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
package org.exoplatform.services.jcr.access;

import javax.jcr.Credentials;
import javax.jcr.LoginException;

import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS.<br/>
 * Authentication policy for 
 * 
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: AuthenticationPolicy.java 14100 2008-05-12 10:53:47Z gazarenkov $
 */

public interface AuthenticationPolicy {
  
  /**
   * Authenticates getting credentials. 
   * @param credentials credentials
   * @return credentials took part in authentication (could be not the same a incoming one)
   * @throws LoginException
   */
  ConversationState authenticate(Credentials credentials) throws LoginException;
  
  /**
   * Authenticates using some external mechanism. 
   * @return credentials took part in authentication
   * @throws LoginException
   */
  ConversationState authenticate() throws LoginException;
  
}
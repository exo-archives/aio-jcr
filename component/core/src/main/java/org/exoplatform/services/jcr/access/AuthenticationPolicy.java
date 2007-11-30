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

/**
 * Created by The eXo Platform SAS.<br/>
 * Authentication policy for 
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: AuthenticationPolicy.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface AuthenticationPolicy {
  
  /**
   * authenticates getting credentials 
   * @param credentials
   * @return credentials took part in authentication (could be not the same a incoming one)
   * @throws LoginException
   */
  public Credentials authenticate(Credentials credentials) throws LoginException;
  
  /**
   * authenticates using some external mechanisp 
   * @return credentials took part in authentication
   * @throws LoginException
   */
  public Credentials authenticate() throws LoginException;
  
}
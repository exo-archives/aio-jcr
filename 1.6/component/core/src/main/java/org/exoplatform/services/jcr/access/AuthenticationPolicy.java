/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.access;

import javax.jcr.Credentials;
import javax.jcr.LoginException;

/**
 * Created by The eXo Platform SARL .<br/>
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
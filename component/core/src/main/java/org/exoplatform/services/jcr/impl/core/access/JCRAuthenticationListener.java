/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.access;

import javax.security.auth.Subject;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * May 17, 2007  
 */
public class JCRAuthenticationListener extends Listener<AuthenticationService, Identity> {
  public void onEvent(Event<AuthenticationService, Identity> event)  {
    Identity identity = event.getData() ;
    String username = identity.getUsername() ;
    Subject subject = identity.getSubject() ;
    subject.getPublicCredentials().add(new CredentialsImpl(username, "".toCharArray()));
    
    System.out.println("\n\n == > Call JCRAuthenticationListener \n\n");
  }
}

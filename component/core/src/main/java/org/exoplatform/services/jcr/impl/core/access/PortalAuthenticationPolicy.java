/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.access;

import java.util.Iterator;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.security.auth.Subject;

import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.security.SecurityService;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * This authenticator uses identity stored in Security Service
 * 
 * @author Gennady Azarenkov
 * @version $Id: PortalAuthenticationPolicy.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class PortalAuthenticationPolicy extends BaseAuthenticationPolicy {
  
  private static final Credentials SYSTEM_CRED = new CredentialsImpl(SystemIdentity.ANONIM, new char[0]);
  
  public PortalAuthenticationPolicy(RepositoryEntry config,
      SecurityService securityService) {
    super(config, securityService);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.access.BaseAuthenticationPolicy#authenticate()
   */
  public Credentials authenticate() throws LoginException {
    
    Credentials cred = null;
    Subject subj = securityService.getCurrentSubject();
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
    
    //throw new LoginException(
    //  "Current User should be authenticated externally but was not.");
  }
    
//    ThreadLocalCredentialsProvider credentialsProvider = new ThreadLocalCredentialsProvider();
    
//    CredentialsImpl thisCredentials;

    // Try to reuse Host's identity
//    SessionContainer sessionContainer = SessionContainer.getInstance();
//    if (sessionContainer != null) {
//      String userId = SessionContainer.getInstance().getRemoteUser();
    
      
//      // Anonimous user
//    if (userId == null)
//      userId = SystemIdentity.ANONIM;
      
    //thisCredentials = new CredentialsImpl(userId, new char[0]);
    
    //Credentials thisCredentials = credentialsHolder.get();
    // Anonimous user
//    if (thisCredentials == null) {
//      thisCredentials = new CredentialsImpl(SystemIdentity.ANONIM, new char[0]);
//      if(log.isDebugEnabled()) {
//        log.debug("Repository.login() gets user  " + thisCredentials
//            + " from ThreadLocal holder");
//    } else 
//    if(log.isDebugEnabled()) {
//      log.debug("Repository.login() gets user  " + thisCredentials
//          + " from ThreadLocal holder");
//      
////    } else {
////      throw new LoginException(
////          "Current User should be authenticated externally but was not.");
////    }
//    
////    credentialsHolder.set(thisCredentials);
//
//    return thisCredentials;
//  }

}

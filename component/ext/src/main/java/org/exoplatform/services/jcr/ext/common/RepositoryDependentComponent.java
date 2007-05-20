/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.common;

import java.util.Iterator;

import javax.jcr.Credentials;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.security.SecurityService;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class RepositoryDependentComponent {
  
  protected final ManageableRepository repository;
  protected Credentials credentials;
 
  public RepositoryDependentComponent(RepositoryService repositoryService,
      SecurityService securityService) { 
    this.repository = repositoryService.getCurrentRepository();
    Iterator pubCreds = securityService.getCurrentSubject().getPublicCredentials().iterator();
    while(pubCreds.hasNext()) {
      Object o = pubCreds.next();
      if(o instanceof Credentials) {
        this.credentials = (Credentials)o;
        break;
      }
    }
    if(this.credentials == null)
      this.credentials = new CredentialsImpl(SystemIdentity.ANONIM, null); 
  }
  
}

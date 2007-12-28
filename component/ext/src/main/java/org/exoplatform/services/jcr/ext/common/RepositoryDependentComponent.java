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
package org.exoplatform.services.jcr.ext.common;

import java.util.Iterator;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.security.SecurityService;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class RepositoryDependentComponent {
  
  protected final ManageableRepository repository;
  protected Credentials credentials;
 
  public RepositoryDependentComponent(RepositoryService repositoryService,
      SecurityService securityService) throws RepositoryException, RepositoryConfigurationException  { 
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

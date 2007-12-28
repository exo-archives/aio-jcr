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

package org.exoplatform.services.webdav.common.resource;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class JCRResourceDispatcher {
  
  private SessionProvider sessionProvider;
  
  private ManageableRepository repository;
  
  public JCRResourceDispatcher(SessionProvider sessionProvider, ManageableRepository repository) {
    this.sessionProvider = sessionProvider;
    this.repository = repository;
  }
  
  public Item getItem(String jcrPath) throws RepositoryException {
    String []pathes = jcrPath.split("/");
    
    String workspaceName = pathes[0];
    
    Session session = sessionProvider.getSession(workspaceName, repository); 
    
    if (pathes.length == 1) {
      return session.getRootNode();
    }
    
    String path = jcrPath.substring(("/" + pathes[0]).length());
    
    if (!path.startsWith("/")) {
      path = "/" + path; 
    }
    
    return session.getItem(path);
  }
  
}

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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.exoplatform.services.database.XResources;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;


/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: DeleteUserListener.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class DeleteUserListener extends UserEventListener {

  protected static Log log = ExoLogger.getLogger("jcr.RepositoryService");

  private RepositoryImpl repository;

  public DeleteUserListener(RepositoryService jcrService) throws Exception {
    this.repository = (RepositoryImpl)jcrService.getCurrentRepository();
  }

  public void preSave(User user, boolean isNew, XResources xresources) {
  }

  public void postSave(User user, boolean isNew, XResources xresources) {
  }

  public void preDelete(User user, XResources xresources) {
    try {
      String[] wsNames = repository.getWorkspaceNames();
      for(int i=0; i<wsNames.length; i++) {
         // System session
         Session session = repository.getSystemSession(wsNames[i]);
         QueryManager qManager = session.getWorkspace().getQueryManager();
         Query q = qManager.createQuery("//*[@exo:owner='"+user.getUserName()+"']", Query.XPATH);
         q.execute();
      }
    } catch (RepositoryException e) {
      log.error("RepositoryException while trying to delete a user home dir", e);
    }
  }

  public void postDelete(User user, XResources xresources) {
  }
}
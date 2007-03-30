/**************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: DeleteUserListener.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class DeleteUserListener extends UserEventListener {

  protected static Log log = ExoLogger.getLogger("jcr.RepositoryService");

  private RepositoryImpl repository;

  public DeleteUserListener(RepositoryService jcrService) throws Exception {
    this.repository = (RepositoryImpl)jcrService.getRepository();
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
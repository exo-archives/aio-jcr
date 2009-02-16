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

package org.exoplatform.services.jcr.webdav.command;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */
public class MoveCommand {

  private static CacheControl cacheControl = new CacheControl();

  private static Log          log          = ExoLogger.getLogger(MoveCommand.class);

  // Fix problem with moving under Windows Explorer.
  static {
    cacheControl.setNoCache(true);
  }

  public Response move(Session session, String srcPath, String destPath) {
    try {

      boolean itemExisted = session.itemExists(destPath);
      if (itemExisted) {
        session.getItem(destPath).remove();
      }

      session.move(srcPath, destPath);
      session.save();

      if (itemExisted) {
        return Response.status(HTTPStatus.NO_CONTENT).cacheControl(cacheControl).build();
      } else {
        return Response.status(HTTPStatus.CREATED).cacheControl(cacheControl).build();
      }

    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();

    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.CONFLICT).build();

    } catch (RepositoryException exc) {
      log.error(exc.getMessage(), exc);
      return Response.serverError().build();
    }

  }

  public Response move(Session sourceSession, Session destSession, String srcPath, String destPath) {
    try {

      destSession.getWorkspace().copy(sourceSession.getWorkspace().getName(), srcPath, destPath);
      sourceSession.getItem(srcPath).remove();
      sourceSession.save();

      return Response.status(HTTPStatus.NO_CONTENT).cacheControl(cacheControl).build();

    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();

    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.CONFLICT).build();

    } catch (RepositoryException exc) {
      log.error(exc.getMessage(), exc);
      return Response.serverError().build();
    }

  }

}

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

import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.Response;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */
public class MoveCommand {

  private static CacheControl cacheControl = new CacheControl();

  // Fix problem with moving under Windows Explorer.
  static {
    cacheControl.setNoCache(true);
  }

  /**
   * To trace if an item on destination path existed.
   */

  final private boolean       itemExisted;

  public MoveCommand() {
    this.itemExisted = false;
  }

  /**
   * Here we pass info about pre-existence of item on the copy destination path
   * If an item existed, we must respond with NO_CONTENT (204) HTTP status. If
   * an item did not exist, we must respond with CREATED (201) HTTP status More
   * info can be found <a
   * href=http://www.webdav.org/specs/rfc2518.html#METHOD_COPY>here</a>.
   */
  public MoveCommand(boolean itemExisted) {
    this.itemExisted = itemExisted;
  }

  public Response move(Session session, String srcPath, String destPath) {
    try {
      session.move(srcPath, destPath);
      session.save();
      // If the source resource was successfully moved
      // to a pre-existing destination resource.
      if (itemExisted) {
        return Response.Builder.withStatus(WebDavStatus.NO_CONTENT)
                               .cacheControl(cacheControl)
                               .build();
      }
      // If the source resource was successfully moved,
      // and a new resource was created at the destination.
      else {
        return Response.Builder.withStatus(WebDavStatus.CREATED).cacheControl(cacheControl).build();
      }
    } catch (LockException exc) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).build();

    } catch (PathNotFoundException exc) {
      return Response.Builder.withStatus(WebDavStatus.CONFLICT).build();

    } catch (RepositoryException exc) {
      exc.printStackTrace();
      return Response.Builder.serverError().build();
    }

  }

  public Response move(Session sourceSession, Session destSession, String srcPath, String destPath) {
    try {

      destSession.getWorkspace().copy(sourceSession.getWorkspace().getName(), srcPath, destPath);
      sourceSession.getItem(srcPath).remove();
      sourceSession.save();

      // If the source resource was successfully moved
      // to a pre-existing destination resource.
      if (itemExisted) {
        return Response.Builder.withStatus(WebDavStatus.NO_CONTENT)
                               .cacheControl(cacheControl)
                               .build();
      }
      // If the source resource was successfully moved,
      // and a new resource was created at the destination.
      else {
        return Response.Builder.withStatus(WebDavStatus.CREATED).cacheControl(cacheControl).build();
      }

    } catch (LockException e) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).errorMessage(e.getMessage()).build();

    } catch (PathNotFoundException e) {
      return Response.Builder.withStatus(WebDavStatus.CONFLICT).errorMessage(e.getMessage()).build();

    } catch (RepositoryException e) {
      e.printStackTrace();
      return Response.Builder.serverError().errorMessage(e.getMessage()).build();
    }

  }

}

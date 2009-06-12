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

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : <a
 * href="gavrikvetal@gmail.com">Vitaly Guly</a>.
 * 
 * @version $Id: $
 */

public class CopyCommand {

  /**
   * Logger.
   */
  private static Log log = ExoLogger.getLogger(CopyCommand.class);

  /**
   * Webdav COPY method implementation for the same workspace.
   * 
   * @param destSession destination session
   * @param sourcePath source file path
   * @param destPath destination file path
   * @return the instance of javax.ws.rs.core.Response
   */
  public Response copy(Session destSession, String sourcePath, String destPath) {
    try {
      destSession.getWorkspace().copy(sourcePath, destPath);
      return Response.status(HTTPStatus.CREATED).build();
    } catch (ItemExistsException e) {
      return Response.status(HTTPStatus.METHOD_NOT_ALLOWED).build();
    } catch (PathNotFoundException e) {
      return Response.status(HTTPStatus.CONFLICT).build();
    } catch (AccessDeniedException e) {
      return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (LockException e) {
      return Response.status(HTTPStatus.LOCKED).build();
    } catch (RepositoryException e) {
      log.error(e.getMessage(), e);
      return Response.serverError().build();
    }
  }

  /**
   * Webdav COPY method implementation for the different workspaces.
   * 
   * @param destSession destination session
   * @param sourceWorkspace source workspace name
   * @param sourcePath source file path
   * @param destPath destination file path
   * @return the instance of javax.ws.rs.core.Response
   */
  public Response copy(Session destSession,
                       String sourceWorkspace,
                       String sourcePath,
                       String destPath) {
    try {
      destSession.getWorkspace().copy(sourceWorkspace, sourcePath, destPath);
      return Response.status(HTTPStatus.CREATED).build();
    } catch (ItemExistsException e) {
      return Response.status(HTTPStatus.METHOD_NOT_ALLOWED).build();
    } catch (PathNotFoundException e) {
      return Response.status(HTTPStatus.CONFLICT).build();
    } catch (AccessDeniedException e) {
      return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (LockException e) {
      return Response.status(HTTPStatus.LOCKED).build();
    } catch (RepositoryException e) {
      return Response.serverError().build();
    }
  }
}

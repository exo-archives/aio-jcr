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

import org.exoplatform.common.http.HTTPStatus;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class CopyCommand {

  public Response copy(Session destSession, String sourcePath, String destPath) {
    try {
      destSession.getWorkspace().copy(sourcePath, destPath);
      return Response.status(HTTPStatus.CREATED).build();
    } catch (ItemExistsException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.METHOD_NOT_ALLOWED).build();
    } catch (PathNotFoundException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.CONFLICT).build();
    } catch (AccessDeniedException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (LockException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.LOCKED).build();
    } catch (RepositoryException e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }

  public Response copy(Session destSession,
                       String sourceWorkspace,
                       String sourcePath,
                       String destPath) {
    try {
      destSession.getWorkspace().copy(sourceWorkspace, sourcePath, destPath);
      return Response.status(HTTPStatus.CREATED).build();
    } catch (ItemExistsException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.METHOD_NOT_ALLOWED).build();
    } catch (PathNotFoundException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.CONFLICT).build();
    } catch (AccessDeniedException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (LockException e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.LOCKED).build();
    } catch (RepositoryException e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }
}

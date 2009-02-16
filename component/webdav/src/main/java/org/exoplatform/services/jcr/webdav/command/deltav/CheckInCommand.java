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

package org.exoplatform.services.jcr.webdav.command.deltav;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class CheckInCommand {
  
  private static Log log = ExoLogger.getLogger(CheckInCommand.class);

  public Response checkIn(Session session, String path) {
    try {
      Node node = session.getRootNode().getNode(TextUtil.relativizePath(path));
      node.checkin();
      return Response.ok().header(HttpHeaders.CACHE_CONTROL, "no-cache").build();

    } catch (UnsupportedRepositoryOperationException e) {
      return Response.status(HTTPStatus.CONFLICT).build();

    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();

    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();

    } catch (RepositoryException exc) {
      log.error(exc.getMessage(), exc);
      return Response.serverError().build();
    }

  }

}

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
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.ws.rs.core.Response;

import org.exoplatform.services.log.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : <a
 * href="gavrikvetal@gmail.com">Vitaly Guly</a> .
 * 
 * @version $Id: $
 */

public class VersionControlCommand {

  /**
   * logger.
   */
  private static Log log = ExoLogger.getLogger(VersionControlCommand.class);

 /**
  * Webdav Version-Control method implementation.
  * 
  * @param session current session
  * @param path resource path 
  * @return the instance of javax.ws.rs.core.Response
  */
  public Response versionControl(Session session, String path) {
    try {
      Node node = (Node) session.getItem(path);

      if (!node.isNodeType("mix:versionable")) {
        node.addMixin("mix:versionable");
        session.save();
      }
      return Response.ok().build();

    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();

    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();

    } catch (Exception exc) {
      log.error(exc.getMessage(), exc);
      return Response.serverError().build();
    }
  }

}

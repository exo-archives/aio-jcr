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
import javax.jcr.version.Version;

import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class UnCheckOutCommand {

  public Response uncheckout(Session session, String path) {

    try {
      Node node = session.getRootNode().getNode(TextUtil.relativizePath(path));

      Version restoreVersion = node.getBaseVersion();
      node.restore(restoreVersion, true);

      return Response.Builder.ok().build();

    } catch (UnsupportedRepositoryOperationException e) {
      return Response.Builder.withStatus(WebDavStatus.CONFLICT).build();

    } catch (LockException exc) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).build();

    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();

    } catch (RepositoryException exc) {
      return Response.Builder.serverError().build();
    }

  }

}

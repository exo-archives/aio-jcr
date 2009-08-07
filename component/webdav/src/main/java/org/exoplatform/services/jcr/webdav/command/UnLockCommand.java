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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class UnLockCommand {

  protected final NullResourceLocksHolder nullResourceLocks;

  public UnLockCommand(final NullResourceLocksHolder nullResourceLocks) {
    this.nullResourceLocks = nullResourceLocks;
  }

  public Response unLock(Session session, String path, List<String> tokens) {
    try {
      try {
        Node node = (Node) session.getItem(path);

        if (node.isLocked()) {
          node.unlock();
          session.save();
        }

        return Response.Builder.noContent().build();
      } catch (PathNotFoundException exc) {
        if (nullResourceLocks.isLocked(session, path)) {
          nullResourceLocks.checkLock(session, path, tokens);
          nullResourceLocks.removeLock(session, path);
          return Response.Builder.noContent().build();
        }

        return Response.Builder.notFound().build();
      }

    } catch (LockException exc) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).build();
    } catch (Exception exc) {
      return Response.Builder.serverError().build();
    }

  }

}

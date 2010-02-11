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

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class MkColCommand {

  private static Log                    log = ExoLogger.getLogger("jcr.MkColCommand");

  private final NullResourceLocksHolder nullResourceLocks;

  public MkColCommand(final NullResourceLocksHolder nullResourceLocks) {
    this.nullResourceLocks = nullResourceLocks;
  }

  public Response mkCol(Session session,
                        String path,
                        String nodeType,
                        List<String> mixinTypes,
                        List<String> tokens) {
    Node node;
    try {
      nullResourceLocks.checkLock(session, path, tokens);

      node = session.getRootNode().addNode(TextUtil.relativizePath(path), nodeType);
      if (mixinTypes != null) {
        addMixins(node, mixinTypes);
      }
      session.save();

    } catch (ItemExistsException e) {
      return Response.Builder.withStatus(WebDavStatus.METHOD_NOT_ALLOWED)
                             .errorMessage(e.getMessage())
                             .build();

    } catch (PathNotFoundException e) {
      return Response.Builder.withStatus(WebDavStatus.CONFLICT)
                             .errorMessage(e.getMessage())
                             .build();

    } catch (AccessDeniedException e) {
      return Response.Builder.withStatus(WebDavStatus.FORBIDDEN)
                             .errorMessage(e.getMessage())
                             .build();

    } catch (LockException e) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).errorMessage(e.getMessage()).build();

    } catch (RepositoryException e) {
      return Response.Builder.serverError()
                             .errorMessage(e.getMessage())
                             .errorMessage(e.getMessage())
                             .build();
    }

    return Response.Builder.withStatus(WebDavStatus.CREATED).build();
  }

  private void addMixins(Node node, List<String> mixinTypes) {
    for (int i = 0; i < mixinTypes.size(); i++) {
      String curMixinType = mixinTypes.get(i);
      try {
        node.addMixin(curMixinType);
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Can't add mixin [" + curMixinType + "]");
      }
    }
  }
}

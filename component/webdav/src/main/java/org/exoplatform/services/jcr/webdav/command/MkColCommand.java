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
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : <a
 * href="gavrikvetal@gmail.com">Vitaly Guly</a>.
 * 
 * @version $Id: $
 */

public class MkColCommand {

  /**
   * Logger.
   */
  private static Log                    log = ExoLogger.getLogger(MkColCommand.class);

  /**
   * resource locks.
   */
  private final NullResourceLocksHolder nullResourceLocks;

  /**
   * Constructor. 
   * 
   * @param nullResourceLocks resource locks. 
   */
  public MkColCommand(final NullResourceLocksHolder nullResourceLocks) {
    this.nullResourceLocks = nullResourceLocks;
  }

  /**
   * Webdav Mkcol method implementation.
   * 
   * @param session current session
   * @param path resource path
   * @param nodeType folder node type
   * @param mixinTypes mixin types 
   * @param tokens tokens
   * @return the instance of javax.ws.rs.core.Response
   */
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

    } catch (ItemExistsException exc) {
      return Response.status(HTTPStatus.METHOD_NOT_ALLOWED).build();

    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.CONFLICT).build();

    } catch (AccessDeniedException exc) {
      return Response.status(HTTPStatus.FORBIDDEN).build();

    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();

    } catch (RepositoryException exc) {
      return Response.serverError().build();
    }

    return Response.status(HTTPStatus.CREATED).build();
  }

  /**
   * Adds mixins to node.
   * 
   * @param node node.
   * @param mixinTypes mixin types.
   */
  private void addMixins(Node node, List<String> mixinTypes) {
    for (int i = 0; i < mixinTypes.size(); i++) {
      String curMixinType = mixinTypes.get(i);
      try {
        node.addMixin(curMixinType);
      } catch (Exception exc) {
        log.error("Can't add mixin [" + curMixinType + "]", exc);
      }
    }
  }
}

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

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.command.proppatch.PropPatchResponseEntity;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class PropPatchCommand {

  protected final NullResourceLocksHolder lockHolder;

  public PropPatchCommand(NullResourceLocksHolder lockHolder) {
    this.lockHolder = lockHolder;
  }

  public Response propPatch(Session session,
                            String path,
                            HierarchicalProperty body,
                            List<String> tokens,
                            String baseURI) {
    try {

      lockHolder.checkLock(session, path, tokens);

      Node node = (Node) session.getItem(path);

      WebDavNamespaceContext nsContext = new WebDavNamespaceContext(session);
      URI uri = new URI(TextUtil.escape(baseURI + node.getPath(), '%', true));

      List<HierarchicalProperty> setList = Collections.emptyList();
      if (body.getChild(new QName("DAV:", "set")) != null) {
        setList = setList(body);
      }

      List<HierarchicalProperty> removeList = Collections.emptyList();
      if (body.getChild(new QName("DAV:", "remove")) != null) {
        removeList = removeList(body);
      }

      PropPatchResponseEntity entity = new PropPatchResponseEntity(nsContext,
                                                                   node,
                                                                   uri,
                                                                   setList,
                                                                   removeList);
      return Response.status(HTTPStatus.MULTISTATUS).entity(entity).build();

    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }

  }

  public List<HierarchicalProperty> setList(HierarchicalProperty request) {
    HierarchicalProperty set = request.getChild(new QName("DAV:", "set"));
    HierarchicalProperty prop = set.getChild(new QName("DAV:", "prop"));
    List<HierarchicalProperty> setList = prop.getChildren();
    return setList;
  }

  public List<HierarchicalProperty> removeList(HierarchicalProperty request) {
    HierarchicalProperty remove = request.getChild(new QName("DAV:", "remove"));
    HierarchicalProperty prop = remove.getChild(new QName("DAV:", "prop"));
    List<HierarchicalProperty> removeList = prop.getChildren();
    return removeList;
  }

}

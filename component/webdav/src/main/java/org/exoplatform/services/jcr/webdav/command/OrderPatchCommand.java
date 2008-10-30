/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.command.order.OrderMember;
import org.exoplatform.services.jcr.webdav.command.order.OrderPatchResponseEntity;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class OrderPatchCommand {

  public OrderPatchCommand() {
  }

  public Response orderPatch(Session session, String path, HierarchicalProperty body, String baseURI) {
    try {
      Node node = (Node) session.getItem(path);

      List<OrderMember> members = getMembers(body);

      WebDavNamespaceContext nsContext = new WebDavNamespaceContext(session);
      URI uri = new URI(TextUtil.escape(baseURI + node.getPath(), '%', true));

      if (doOrder(node, members)) {
        return Response.ok().build();
      }

      OrderPatchResponseEntity orderPatchEntity = new OrderPatchResponseEntity(nsContext,
                                                                               uri,
                                                                               node,
                                                                               members);
      return Response.status(HTTPStatus.MULTISTATUS).entity(orderPatchEntity).build();
    } catch (PathNotFoundException exc) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.serverError().build();
    }

  }

  protected List<OrderMember> getMembers(HierarchicalProperty body) {
    ArrayList<OrderMember> members = new ArrayList<OrderMember>();
    List<HierarchicalProperty> childs = body.getChildren();
    for (int i = 0; i < childs.size(); i++) {
      OrderMember member = new OrderMember(childs.get(i));
      members.add(member);
    }
    return members;
  }

  protected boolean doOrder(Node parentNode, List<OrderMember> members) {
    boolean success = true;
    for (int i = 0; i < members.size(); i++) {
      OrderMember member = members.get(i);

      int status = HTTPStatus.OK;

      try {
        parentNode.getSession().refresh(false);
        String positionedNodeName = null;

        if (!parentNode.hasNode(member.getSegment())) {
          throw new PathNotFoundException();
        }

        if (!new QName("DAV:", "last").equals(member.getPosition())) {
          NodeIterator nodeIter = parentNode.getNodes();
          boolean finded = false;

          while (nodeIter.hasNext()) {
            Node curNode = nodeIter.nextNode();

            if (new QName("DAV:", "first").equals(member.getPosition())) {
              positionedNodeName = curNode.getName();
              finded = true;
              break;
            }

            if (new QName("DAV:", "before").equals(member.getPosition())
                && curNode.getName().equals(member.getPositionSegment())) {
              positionedNodeName = curNode.getName();
              finded = true;
              break;
            }

            if (new QName("DAV:", "after").equals(member.getPosition())
                && curNode.getName().equals(member.getPositionSegment())) {
              if (nodeIter.hasNext()) {
                positionedNodeName = nodeIter.nextNode().getName();
              }
              finded = true;
              break;
            }
          }

          if (!finded) {
            throw new AccessDeniedException();
          }
        }

        parentNode.getSession().refresh(false);
        parentNode.orderBefore(member.getSegment(), positionedNodeName);
        parentNode.getSession().save();

      } catch (LockException exc) {
        status = HTTPStatus.LOCKED;

      } catch (PathNotFoundException exc) {
        status = HTTPStatus.FORBIDDEN;

      } catch (AccessDeniedException exc) {
        status = HTTPStatus.FORBIDDEN;

      } catch (RepositoryException exc) {
        status = HTTPStatus.INTERNAL_ERROR;

      }

      member.setStatus(status);
      if (status != HTTPStatus.OK) {
        success = false;
      }
    }

    return success;
  }

}

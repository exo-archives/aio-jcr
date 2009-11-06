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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.Depth;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.deltav.report.VersionTreeResponseEntity;
import org.exoplatform.services.jcr.webdav.resource.ResourceUtil;
import org.exoplatform.services.jcr.webdav.resource.VersionedCollectionResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedFileResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedResource;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class ReportCommand {

  public Response report(Session session,
                         String path,
                         HierarchicalProperty body,
                         Depth depth,
                         String baseURI) {
    try {
      Node node = (Node) session.getItem(path);
      WebDavNamespaceContext nsContext = new WebDavNamespaceContext(session);
      String strUri = baseURI + node.getPath();
      URI uri = new URI(TextUtil.escape(strUri, '%', true));

      if (!ResourceUtil.isVersioned(node)) {
        return Response.Builder.serverError().build();
      }

      VersionedResource resource;
      if (ResourceUtil.isFile(node)) {
        resource = new VersionedFileResource(uri, node, nsContext);
      } else {
        resource = new VersionedCollectionResource(uri, node, nsContext);
      }

      Set<QName> properties = getProperties(body);

      VersionTreeResponseEntity response = new VersionTreeResponseEntity(nsContext,
                                                                         resource,
                                                                         properties);

      return Response.Builder.withStatus(WebDavStatus.MULTISTATUS)
                             .entity(response, "text/xml")
                             .build();

    } catch (PathNotFoundException e) {
      return Response.Builder.notFound().errorMessage(e.getMessage()).build();
    } catch (RepositoryException e) {
      return Response.Builder.serverError().errorMessage(e.getMessage()).build();
    } catch (Exception e) {
      return Response.Builder.serverError().errorMessage(e.getMessage()).build();
    }
  }

  protected Set<QName> getProperties(HierarchicalProperty body) {
    HashSet<QName> properties = new HashSet<QName>();

    HierarchicalProperty prop = body.getChild(new QName("DAV:", "prop"));
    if (prop == null) {
      return properties;
    }

    for (int i = 0; i < prop.getChildren().size(); i++) {
      HierarchicalProperty property = prop.getChild(i);
      properties.add(property.getName());
    }

    return properties;
  }

}

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

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.exoplatform.services.jcr.util.Text;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class PutCommand {

  private final NullResourceLocksHolder nullResourceLocks;

  public PutCommand(final NullResourceLocksHolder nullResourceLocks) {
    this.nullResourceLocks = nullResourceLocks;
  }

  public Response put(Session session,
                      String path,
                      InputStream inputStream,
                      String nodeType,
                      String mimeType,
                      String updatePolicyType,
                      List<String> tokens) {

    try {

      Node node = null;
      try {
        node = (Node) session.getItem(path);
      } catch (PathNotFoundException pexc) {
        nullResourceLocks.checkLock(session, path, tokens);
      }

      if (node == null || "add".equals(updatePolicyType)) {

        node = session.getRootNode().addNode(TextUtil.relativizePath(path), nodeType);
        node.addNode("jcr:content", "nt:resource");
        updateContent(node, inputStream, mimeType);
      } else {
        if ("add".equals(updatePolicyType)) {
          node = session.getRootNode().addNode(TextUtil.relativizePath(path), nodeType);
          node.addNode("jcr:content", "nt:resource");
          updateContent(node, inputStream, mimeType);
        } else if ("create-version".equals(updatePolicyType)) {
          createVersion(node, inputStream, mimeType);
        } else {
          updateContent(node, inputStream, mimeType);
        }
      }

      session.save();

    } catch (LockException exc) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).build();

    } catch (AccessDeniedException e) {
      return Response.Builder.withStatus(WebDavStatus.FORBIDDEN).build();

    } catch (RepositoryException exc) {
      exc.printStackTrace();
      return Response.Builder.withStatus(WebDavStatus.CONFLICT).build();
    }

    return Response.Builder.withStatus(WebDavStatus.CREATED).build();
  }

  private final void createVersion(Node fileNode, InputStream inputStream, String mimeType) throws RepositoryException {
    if (!fileNode.isNodeType("mix:versionable")) {
      if (fileNode.canAddMixin("mix:versionable")) {
        fileNode.addMixin("mix:versionable");
        fileNode.getSession().save();
      }
      fileNode.checkin();
      fileNode.getSession().save();
    }

    if (!fileNode.isCheckedOut()) {
      fileNode.checkout();
      fileNode.getSession().save();
    }

    updateContent(fileNode, inputStream, mimeType);
    fileNode.getSession().save();
    fileNode.checkin();
    fileNode.getSession().save();
  }

  private final void updateContent(Node node, InputStream inputStream, String mimeType) throws RepositoryException {
    Node content = node.getNode("jcr:content");
    content.setProperty("jcr:mimeType", mimeType);
    content.setProperty("jcr:lastModified", Calendar.getInstance());
    content.setProperty("jcr:data", inputStream);
  }

}

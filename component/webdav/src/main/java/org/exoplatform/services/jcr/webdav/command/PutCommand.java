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
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.TextUtil;

/**
 * Created by The eXo Platform SAS Author : <a
 * href="gavrikvetal@gmail.com">Vitaly Guly</a>.
 * 
 * @version $Id: $
 */

public class PutCommand {

  /**
   * resource locks.
   */
  private final NullResourceLocksHolder nullResourceLocks;

  /**
   * Constructor.
   * 
   * @param nullResourceLocks resource locks.
   */
  public PutCommand(final NullResourceLocksHolder nullResourceLocks) {
    this.nullResourceLocks = nullResourceLocks;
  }

  /**
   * Webdav Put method implementation.
   * 
   * @param session current session
   * @param path resource path
   * @param inputStream stream that contains resource content
   * @param fileNodeType the node type of file node
   * @param contentNodeType the node type of content
   * @param mixins the list of mixins
   * @param mimeType content type
   * @param updatePolicyType update policy
   * @param tokens tokens
   * @return the instance of javax.ws.rs.core.Response
   */
  public Response put(Session session,
                      String path,
                      InputStream inputStream,
                      String fileNodeType,
                      String contentNodeType,
                      List<String> mixins,
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

        node = session.getRootNode().addNode(TextUtil.relativizePath(path), fileNodeType);

        node.addNode("jcr:content", contentNodeType);
        updateContent(node, inputStream, mimeType, mixins);
      } else {
        if ("add".equals(updatePolicyType)) {
          node = session.getRootNode().addNode(TextUtil.relativizePath(path), fileNodeType);
          node.addNode("jcr:content", contentNodeType);
          updateContent(node, inputStream, mimeType, mixins);
        } else if ("create-version".equals(updatePolicyType)) {
          createVersion(node, inputStream, mimeType, mixins);
        } else {
          updateContent(node, inputStream, mimeType, mixins);
        }
      }

      session.save();

    } catch (LockException exc) {
      return Response.status(HTTPStatus.LOCKED).build();

    } catch (AccessDeniedException e) {
      return Response.status(HTTPStatus.FORBIDDEN).build();

    } catch (RepositoryException exc) {
      return Response.status(HTTPStatus.CONFLICT).build();
    }

    return Response.status(HTTPStatus.CREATED).build();
  }

  /**
   * Creates the new version of file.
   * 
   * @param fileNode file node
   * @param inputStream input stream that contains the content of file
   * @param mimeType content type
   * @param mixins list of mixins
   * @throws RepositoryException {@link RepositoryException}
   */
  private void createVersion(Node fileNode,
                             InputStream inputStream,
                             String mimeType,
                             List<String> mixins) throws RepositoryException {
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

    updateContent(fileNode, inputStream, mimeType, mixins);
    fileNode.getSession().save();
    fileNode.checkin();
    fileNode.getSession().save();
  }

  /**
   * Updates jcr:content node.
   * 
   * @param node parent node
   * @param inputStream inputStream input stream that contains the content of
   *          file
   * @param mimeType content type
   * @param mixins list of mixins
   * @throws RepositoryException  {@link RepositoryException}
   */
  private void updateContent(Node node,
                             InputStream inputStream,
                             String mimeType,
                             List<String> mixins) throws RepositoryException {

    Node content = node.getNode("jcr:content");
    content.setProperty("jcr:mimeType", mimeType);
    content.setProperty("jcr:lastModified", Calendar.getInstance());
    content.setProperty("jcr:data", inputStream);

    for (String mixinName : mixins) {
      if (content.canAddMixin(mixinName)) {
        content.addMixin(mixinName);
      }

    }

  }

}

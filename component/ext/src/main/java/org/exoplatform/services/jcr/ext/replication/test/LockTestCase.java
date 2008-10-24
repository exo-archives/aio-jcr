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
package org.exoplatform.services.jcr.ext.replication.test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class LockTestCase extends BaseReplicationTestCase {

  /**
   * LockTestCase  constructor.
   *
   * @param repositoryService
   *          the RepositoryService.
   * @param reposytoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   */
  public LockTestCase(RepositoryService repositoryService,
                      String reposytoryName,
                      String workspaceName,
                      String userName,
                      String password) {
    super(repositoryService, reposytoryName, workspaceName, userName, password);
    log.info("LockTestCase inited");
  }

  /**
   * lock.
   *
   * @param repoPath
   *          repository path
   * @return StringBuffer
   *           return the responds {'ok', 'fail'}
   */
  public StringBuffer lock(String repoPath) {
    StringBuffer sb = new StringBuffer();

    try {
      Node lockNode = addNodePath(repoPath);
      lockNode.setProperty("jcr:data", "node data");
      lockNode.addMixin("mix:lockable");
      session.save();

      Lock lock = lockNode.lock(false, false);
      session.save();

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't locked: ", e);
      sb.append("fail");
    }

    return sb;
  }

  /**
   * isLocked.
   *
   * @param repoPath
   *          repository path
   * @return StringBuffer
   *           return the responds {'ok', 'fail'}
   */
  public StringBuffer isLocked(String repoPath) {
    StringBuffer sb = new StringBuffer();

    String normalizePath = getNormalizePath(repoPath);

    try {
      Node destNodeLocked = (Node) session.getItem(normalizePath);
      destNodeLocked.setProperty("jcr:data", "dd");
      session.save();

      sb.append("fail");
      log.error("Errore: Node is not locked");
    } catch (LockException e) {
      sb.append("ok");
    } catch (RepositoryException e) {
      sb.append("fail");
      log.error("The error checking lock :", e);
    }

    return sb;
  }
}

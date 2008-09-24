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
import javax.jcr.version.Version;

import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak alex.reshetnyak@exoplatform.com.ua
 * 30.06.2008
 */
public class VersionTestCase extends BaseReplicationTestCase {

  public VersionTestCase(RepositoryService repositoryService,
                         String reposytoryName,
                         String workspaceName,
                         String userName,
                         String password) {
    super(repositoryService, reposytoryName, workspaceName, userName, password);
    log.info("NtFileTestCase inited");
  }

  public StringBuffer addVersionNode(String repoPath, String value) {
    StringBuffer sb = new StringBuffer();

    try {
      Node srcVersionNode = addNodePath(repoPath);
      srcVersionNode.setProperty("jcr:data", value);
      srcVersionNode.addMixin("mix:versionable");
      session.save();

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't create versioning node: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer checkVersionNode(String repoPath, String checkedValue) {
    StringBuffer sb = new StringBuffer();

    String normalizePath = getNormalizePath(repoPath);
    try {
      Node destVersionNode = (Node) session.getItem(normalizePath);
      if (checkedValue.equals(destVersionNode.getProperty("jcr:data").getString()))
        sb.append("ok");
      else
        sb.append("fail");

    } catch (RepositoryException e) {
      log.error("Can't create versioning node: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer addNewVersion(String repoPath, String newValue) {
    StringBuffer sb = new StringBuffer();

    String normalizePath = getNormalizePath(repoPath);
    try {
      Node srcVersionNode = (Node) session.getItem(normalizePath);

      srcVersionNode.checkin();
      session.save();

      srcVersionNode.checkout();
      srcVersionNode.setProperty("jcr:data", newValue);
      session.save();

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't add versioning node value: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer restorePreviousVersion(String repoPath) {
    StringBuffer sb = new StringBuffer();

    String normalizePath = getNormalizePath(repoPath);
    try {
      Node srcVersionNode = (Node) session.getItem(normalizePath);

      Version baseVersion = srcVersionNode.getBaseVersion();
      srcVersionNode.restore(baseVersion, true);
      session.save();

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't restore previous version: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer restoreBaseVersion(String repoPath) {
    StringBuffer sb = new StringBuffer();

    String normalizePath = getNormalizePath(repoPath);
    try {
      Node srcVersionNode = (Node) session.getItem(normalizePath);

      Version baseVersion1 = srcVersionNode.getBaseVersion();
      Version[] predesessors = baseVersion1.getPredecessors();
      Version restoreToBaseVersion = predesessors[0];

      srcVersionNode.restore(restoreToBaseVersion, true);
      session.save();

      sb.append("ok");
    } catch (RepositoryException e) {
      log.error("Can't restore previous version: ", e);
      sb.append("fail");
    }

    return sb;
  }
}

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

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 19.05.2008
 */
public abstract class BaseReplicationTestCase {
  protected static Log log = ExoLogger.getLogger("ext.AbstractReplicationTestCase");
  
  protected final int BUFFER_SIZE = 1024;

  protected Session    session;

  protected Node       rootNode;

  private Credentials  credentials;

  private Repository   repository;
  
  public BaseReplicationTestCase(RepositoryService repositoryService, String reposytoryName,
      String workspaceName, String userName, String password) {
    try {
      credentials = new CredentialsImpl(userName, password.toCharArray());

      repository = repositoryService.getRepository(reposytoryName);

      session = repository.login(credentials, workspaceName);

      rootNode = session.getRootNode();

    } catch (RepositoryException e) {
      log.error("Can't start BaseReplicationTestCase", e);
    } catch (RepositoryConfigurationException e) {
      log.error("Can't start BaseReplicationTestCase", e);
    }
  }

  protected Node addNodePath(String repoPath) throws RepositoryException {
    Node resultNode = rootNode;
    String[] sArray = repoPath.split("[::]");

    for (String nodeName : sArray)
      if (resultNode.hasNode(nodeName))
        resultNode = resultNode.getNode(nodeName);
      else
        resultNode = resultNode.addNode(nodeName);

    return resultNode;
  }
  
  protected String getNormalizePath(String repoPath) {
   return repoPath.replaceAll("[:][:]", "/"); 
  }
}

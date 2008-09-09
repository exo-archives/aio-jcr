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
package org.exoplatform.services.jcr.ext.replication.test.priority;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.WorkspaceDataTransmitter;
import org.exoplatform.services.jcr.ext.replication.test.BaseReplicationTestCase;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: BasePriorityTestCase.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BasePriorityTestCase extends BaseReplicationTestCase {

  protected WorkspaceDataTransmitter dataTransmitter;

  public BasePriorityTestCase(RepositoryService repositoryService, String reposytoryName,
      String workspaceName, String userName, String password) {
    super(repositoryService, reposytoryName, workspaceName, userName, password);

    WorkspaceContainerFacade wContainer = ((RepositoryImpl) repository)
        .getWorkspaceContainer(session.getWorkspace().getName());

    dataTransmitter = (WorkspaceDataTransmitter) wContainer
        .getComponent(WorkspaceDataTransmitter.class);
  }

  public StringBuffer disconnectClusterNode() {
    StringBuffer sb = new StringBuffer();

    try {
      ChannelManager channelManager = dataTransmitter.getChannelManager();
      channelManager.setAllowConnect(false);
      channelManager.closeChannel();

      channelManager.init();
      channelManager.connect();

      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't disconnected node of cluster: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer disconnectClusterNode(int id) {
    StringBuffer sb = new StringBuffer();

    try {
      ChannelManager channelManager = dataTransmitter.getChannelManager();
      channelManager.setAllowConnect(false, id);
      channelManager.closeChannel();

      channelManager.init();
      channelManager.connect();

      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't disconnected node of cluster: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer allowConnect() {
    StringBuffer sb = new StringBuffer();
    try {
      ChannelManager channelManager = dataTransmitter.getChannelManager();
      channelManager.setAllowConnect(true);

      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't allowed connect node of cluster: ", e);
      sb.append("fail");
    }

    return sb;
  }
  
  public StringBuffer allowConnectForced() {
    StringBuffer sb = new StringBuffer();
    try {
      ChannelManager channelManager = dataTransmitter.getChannelManager();
      channelManager.setAllowConnect(true);
      
      channelManager.closeChannel();
      
      channelManager.init();
      channelManager.connect();

      sb.append("ok");
    } catch (Exception e) {
      log.error("Can't allowed connect node of cluster: ", e);
      sb.append("fail");
    }

    return sb;
  }

  public StringBuffer isReadOnly(String workspaceName) {
    StringBuffer sb = new StringBuffer();
    try {

      WorkspaceContainerFacade wsFacade = ((RepositoryImpl) repository)
          .getWorkspaceContainer(workspaceName);
      WorkspaceDataContainer dataContainer = (WorkspaceDataContainer) wsFacade
          .getComponent(WorkspaceDataContainer.class);
      
      
      if(!dataContainer.isReadOnly())
        throw new Exception("The workspace '"+ dataContainer.getName() +"' was not read-only");

      sb.append("ok");
    } catch (Exception e) {
      log.error("Read-only fail ", e);
      sb.append("fail");
    }

    return sb;
  }
}

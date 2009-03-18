/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.initializer.impl;

import java.io.File;

import org.exoplatform.services.jcr.ext.initializer.RemoteTransport;
import org.exoplatform.services.jcr.ext.initializer.RemoteWorkspaceInitializationException;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 17.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: RemoteTransportImpl.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RemoteTransportImpl implements RemoteTransport {

  private final AsyncChannelManager channelManager;

  private final RemoteTransmitter   remoteTransmitter;

  private final RemoteReceiver      remoteReceiver;

  private final File                tempDir;

  private final String              sourceUrl;

  /**
   * RemoteTransportImpl constructor.
   * 
   */
  public RemoteTransportImpl(AsyncChannelManager channelManager, File tempDir, String sourceUrl) {
    this.channelManager = channelManager;
    this.tempDir = tempDir;
    this.sourceUrl = sourceUrl;
    this.remoteTransmitter = new RemoteTransmitter(this.channelManager);
    this.remoteReceiver = new RemoteReceiver(this.tempDir);
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws RemoteWorkspaceInitializationException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public File getWorkspaceData(String repositoryName, String workspaceName) throws RemoteWorkspaceInitializationException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void init() throws RemoteWorkspaceInitializationException {
    try {
      channelManager.connect();
    } catch (ReplicationException e) {
      throw new RemoteWorkspaceInitializationException("Can not  initialize the transport :"
          + e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void sendWorkspaceData(File workspaceData) throws RemoteWorkspaceInitializationException {
    channelManager.getOtherMembers();

  }

}

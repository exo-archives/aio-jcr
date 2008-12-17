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
package org.exoplatform.services.jcr.ext.replication.async;

import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncTransmitterImpl implements AsyncTransmitter {

  protected final WorkspaceSynchronizer synchronizer;

  protected final AsyncChannelManager   channel;

  AsyncTransmitterImpl(AsyncChannelManager channel, WorkspaceSynchronizer synchronizer) {
    this.channel = channel;
    this.synchronizer = synchronizer;
  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(TransactionChangesLog changes) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void sendGetExport(String nodeId) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void sendExport(TransactionChangesLog changes) {
    // TODO Auto-generated method stub

  }

}

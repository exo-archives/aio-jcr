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

import java.io.IOException;
import java.util.Calendar;

import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.ext.replication.PendingChangesLog;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncInitializer implements AsyncPacketListener {
  
  public static final int WAIT_SYNCHRONOZATION       = 0;
  
  public static final int SYNCHRONIZATION_IS_STARTED = 1;
  
  public static final int SYNCHRONIZATION_IS_DONE    = 2;

  private final int         waitTimeout;
  
  private final String      ownName;
  
  private final int         ownPriority;

  private AsyncChannelManager channelManager;

  /**
   * AsyncInitializer constructor.
   * 
   * @param priority
   *          TODO
   */
  AsyncInitializer(AsyncChannelManager channelManager, String ownName,int priority, int waitTimeout) {
    this.channelManager = channelManager;
    this.ownName = ownName;
    this.ownPriority = priority;
    this.waitTimeout = waitTimeout;
    this.channelManager.addPacketListener(this);
  }

  public void receive(AsyncPacket packet) {
    switch (packet.getType()) {
    case AsyncPacketTypes.GET_STATE_NODE:

      break;

    case AsyncPacketTypes.STATE_NODE:

      break;

    default:
      break;
    }
  }

  private void initSynchronization()  throws Exception{
    AsyncPacket packet = new AsyncPacket(AsyncPacketTypes.GET_CHANGESLOG_UP_TO_DATE, IdGenerator.generate(), ownName);
    packet.setTimeStamp(Calendar.getInstance());
    
    channelManager.sendPacket(packet);
  }
  
  private void initChannel() throws ReplicationException {
    channelManager.init();
    channelManager.connect();
  }
}

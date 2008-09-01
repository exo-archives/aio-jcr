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
package org.exoplatform.services.jcr.ext.replication.recovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.PocketListener;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.ext.replication.ReplicationService;
import org.exoplatform.services.jcr.ext.replication.priority.AbstractPriorityChecker;
import org.exoplatform.services.jcr.ext.replication.priority.DynamicPriorityChecker;
import org.exoplatform.services.jcr.ext.replication.priority.StaticPriorityChecker;
import org.exoplatform.services.jcr.ext.replication.priority.MemberListener;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.View;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ConectionFailDetector.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ConnectionFailDetector implements ChannelListener, MembershipListener, MemberListener {
  private static Log                    log          = ExoLogger
                                                         .getLogger("ext.ConnectionFailDetector");

  private final ChannelManager          channelManager;

  private String                        channelName;

  private ReconectTtread                reconectTtread;

  private int                           lastViewSize = 2;

  private boolean                       allInited    = false;

  private final WorkspaceDataContainer  dataContainer;

  private final RecoveryManager         recoveryManager;

  private List<Address>                 suspectMembers;

  private final int                     ownPriority;

  private final String                  ownName;

  private final List<String>            otherPartisipants;

  private final AbstractPriorityChecker priorityChecker;

  public ConnectionFailDetector(ChannelManager channelManager,
      WorkspaceDataContainer dataContainer, RecoveryManager recoveryManager, int ownPriority,
      List<String> otherParticipants, String ownName, String priprityType) {
    this.channelManager = channelManager;
    this.channelManager.setChannelListener(this);

    this.dataContainer = dataContainer;
    this.recoveryManager = recoveryManager;

    this.ownPriority = ownPriority;

    this.ownName = ownName;
    this.otherPartisipants = new ArrayList<String>(otherParticipants);

    if (priprityType.equals(ReplicationService.PRIORITY_STATIC_TYPE))
      priorityChecker = new StaticPriorityChecker(channelManager, ownPriority, ownName,
          otherParticipants);
    else
      priorityChecker = new DynamicPriorityChecker(channelManager, ownPriority, ownName,
          otherParticipants);

    priorityChecker.setMemberListener(this);
  }

  public void channelClosed(Channel channel) {
    if (log.isDebugEnabled())
      log.debug("Channel closed : " + channel.getClusterName());
  }

  public void channelConnected(Channel channel) {
    if (log.isDebugEnabled())
      log.debug("Channel connected : " + channel.getClusterName());

    channelName = channel.getClusterName();
  }

  public void channelDisconnected(Channel channel) {
    if (log.isDebugEnabled())
      log.debug("Channel disconnected : " + channel.getClusterName());
  }

  public void channelReconnected(Address address) {
  }

  public void channelShunned() {
  }

  public void block() {
  }

  public void suspect(Address adrress) {
    if (log.isDebugEnabled())
      log.debug(" ------->>> MembershipListener.suspect : " + adrress.toString());
    
    if (suspectMembers == null)
      suspectMembers = new ArrayList<Address>();

    if (!suspectMembers.contains(adrress))
      suspectMembers.add(adrress);
  }

  public void viewAccepted(View view) {
    log.info(" Memebers view :" + view.printDetails());

    priorityChecker.informAll();

    if (view.size() > 1)
      allInited = true;

    if (allInited == true)
      lastViewSize = view.size();

    if (priorityChecker instanceof StaticPriorityChecker || otherPartisipants.size() == 1) {

      if (lastViewSize == 1 && !priorityChecker.isMaxPriority()) {
        if (reconectTtread == null || reconectTtread.isStoped() == true) {
          reconectTtread = new ReconectTtread(true);
          reconectTtread.start();
          memberSuspect();
        }
      } else if (reconectTtread != null && priorityChecker.isAllOnline()) {
        reconectTtread.setStop(false);
        reconectTtread = null;
      } else if (lastViewSize > 1 && !priorityChecker.isMaxOnline()) {
        if (reconectTtread == null || reconectTtread.isStoped() == true) {
          reconectTtread = new ReconectTtread(true);
          reconectTtread.start();
          memberSuspect();
        }
      }
    } else if (priorityChecker instanceof DynamicPriorityChecker && otherPartisipants.size() > 1) {
      
      if (lastViewSize == 1 && !priorityChecker.isMaxPriority()) {
        if (reconectTtread == null || reconectTtread.isStoped() == true) {
          reconectTtread = new ReconectTtread(true);
          reconectTtread.start();
          memberSuspect();
        }
      } else if (reconectTtread != null && priorityChecker.isAllOnline()) {
        reconectTtread.setStop(false);
        reconectTtread = null;
      } 
    }
  }

  private class ReconectTtread extends Thread {
    private boolean isStop;

    public ReconectTtread(boolean isStop) {
      this.isStop = isStop;
    }

    @Override
    public void run() {
      while (isStop) {
        try {
          log.info("Connect to channel : " + channelName);
          Thread.sleep(10000);

          int curruntOnlin = 1;

          if (channelManager.getChannel() != null)
            curruntOnlin = channelManager.getChannel().getView().size();

          if (curruntOnlin <= 1 || ((curruntOnlin > 1) && !priorityChecker.isMaxOnline())) {
            channelManager.closeChannel();

            Thread.sleep(Math.round(60000 + Math.random()));
            
            channelManager.init();
            channelManager.connect();
          } else {
            isStop = false;
          }
          Thread.sleep(Math.round(60000 + Math.random()));
        } catch (Exception e) {
          log.info(e, e);
        }
      }
    }

    public void setStop(boolean isStop) {
      this.isStop = isStop;
    }

    public boolean isStoped() {
      return !isStop;
    }
  }

  public void memberRejoin() {
    dataContainer.setReadOnly(false);
    log.info(dataContainer.getName() + " set not read-only");
    log.info(dataContainer.getName() + " recovery start ...");
    recoveryManager.startRecovery();
  }

  public void memberSuspect() {
    log.info(dataContainer.getName() + " set read-only");
    dataContainer.setReadOnly(true);
  }
}

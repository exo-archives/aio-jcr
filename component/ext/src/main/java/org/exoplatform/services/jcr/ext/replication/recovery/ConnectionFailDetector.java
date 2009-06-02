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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.PriorityDucplicatedException;
import org.exoplatform.services.jcr.ext.replication.ReplicationService;
import org.exoplatform.services.jcr.ext.replication.priority.AbstractPriorityChecker;
import org.exoplatform.services.jcr.ext.replication.priority.DynamicPriorityChecker;
import org.exoplatform.services.jcr.ext.replication.priority.GenericPriorityChecker;
import org.exoplatform.services.jcr.ext.replication.priority.MemberListener;
import org.exoplatform.services.jcr.ext.replication.priority.StaticPriorityChecker;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.MembershipListener;
import org.jgroups.View;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ConectionFailDetector.java 111 2008-11-11 11:11:11Z rainf0x $
 */

public class ConnectionFailDetector implements ChannelListener, MembershipListener, MemberListener {
  /**
   * The apache logger.
   */
  private static Log                    log           = ExoLogger.getLogger("ext.ConnectionFailDetector");

  /**
   * Definition the VIEW_CHECK timeout.
   */
  private static final int              VIEW_CHECK    = 200;

  /**
   * The definition timeout for information.
   */
  private static final int              INFORM_TIMOUT = 5000;

  /**
   * Definition the BEFORE_CHECK timeout.
   */
  private static final int              BEFORE_CHECK  = 10000;

  /**
   * Definition the BEFORE_INIT timeout.
   */
  private static final int              BEFORE_INIT   = 60000;

  /**
   * Definition the AFTER_INIT timeout.
   */
  private static final int              AFTER_INIT    = 60000;

  /**
   * The ChannalManager will be transmitted or receive the Packets.
   */
  private final ChannelManager          channelManager;
  
  /**
   * The name of workspace.
   */
  private final String                  workspaceName;

  /**
   * The channel name.
   */
  private String                        channelName;

  /**
   * The ReconectTtread will be initialized reconnect to cluster.
   */
  private ReconectTtread                reconectTtread;

  /**
   * Start value for lastViewSize.
   */
  private int                           lastViewSize  = 2;

  /**
   * Start value for allInited.
   */
  private boolean                       allInited     = false;

  /**
   * The PersistentDataManager will be used to workspace for set state 'read-only'.
   */
  private final PersistentDataManager  dataManager;

  /**
   * The RecoveryManager will be initialized cluster node synchronization.
   */
  private final RecoveryManager         recoveryManager;

  /**
   * The list of address for suspect members.
   */
  private List<Address>                 suspectMembers;

  /**
   * The own priority value.
   */
  private final int                     ownPriority;

  /**
   * The own name in cluster.
   */
  private final String                  ownName;

  /**
   * The list of names to other participants in cluster.
   */
  private final List<String>            otherPartisipants;

  /**
   * The priority checker (static or dynamic).
   */
  private final AbstractPriorityChecker priorityChecker;

  /**
   * The view checker.
   */
  private final ViewChecker             viewChecker;

  /**
   * ConnectionFailDetector constructor.
   * 
   * @param channelManager
   *          the ChannelManager
   * @param dataManager
   *          the PersistentDataManager
   * @param recoveryManager
   *          the RecoveryManager
   * @param ownPriority
   *          the own priority
   * @param otherParticipants
   *          the list of names to other participants in cluster
   * @param ownName
   *          the own name in cluster
   * @param priprityType
   *          the priority type (dynamic or static)s
   * @param workspaceName
   *          String, the name of workspace         
   */
  public ConnectionFailDetector(ChannelManager channelManager,
                                PersistentDataManager dataManager,
                                RecoveryManager recoveryManager,
                                int ownPriority,
                                List<String> otherParticipants,
                                String ownName,
                                String priprityType,
                                String workspaceName) {
    this.channelManager = channelManager;
    this.channelManager.setChannelListener(this);

    this.dataManager = dataManager;
    this.workspaceName = workspaceName;
    this.recoveryManager = recoveryManager;

    this.ownPriority = ownPriority;

    this.ownName = ownName;
    this.otherPartisipants = new ArrayList<String>(otherParticipants);

    if (priprityType.equals(ReplicationService.PRIORITY_STATIC_TYPE))
      priorityChecker = new StaticPriorityChecker(channelManager,
                                                  ownPriority,
                                                  ownName,
                                                  otherParticipants);
    else if (priprityType.equals(ReplicationService.PRIORITY_DYNAMIC_TYPE))
      priorityChecker = new DynamicPriorityChecker(channelManager,
                                                   ownPriority,
                                                   ownName,
                                                   otherParticipants);
    else 
      priorityChecker = new GenericPriorityChecker(channelManager,
                                                   ownPriority,
                                                   ownName,
                                                   otherParticipants);

    priorityChecker.setMemberListener(this);

    viewChecker = new ViewChecker();
    viewChecker.start();
  }

  /**
   * {@inheritDoc}
   */
  public void channelClosed(Channel channel) {
    if (log.isDebugEnabled())
      log.debug("Channel closed : " + channel.getClusterName());
  }

  /**
   * {@inheritDoc}
   */
  public void channelConnected(Channel channel) {
    if (log.isDebugEnabled())
      log.debug("Channel connected : " + channel.getClusterName());

    channelName = channel.getClusterName();
  }

  /**
   * {@inheritDoc}
   */
  public void channelDisconnected(Channel channel) {
    if (log.isDebugEnabled())
      log.debug("Channel disconnected : " + channel.getClusterName());
  }

  /**
   * {@inheritDoc}
   */
  public void channelReconnected(Address address) {
  }

  /**
   * {@inheritDoc}
   */
  public void channelShunned() {
  }

  /**
   * {@inheritDoc}
   */
  public void block() {
  }

  /**
   * {@inheritDoc}
   */
  public void suspect(Address adrress) {
    if (log.isDebugEnabled())
      log.debug(" ------->>> MembershipListener.suspect : " + adrress.toString());

    if (suspectMembers == null)
      suspectMembers = new ArrayList<Address>();

    if (!suspectMembers.contains(adrress))
      suspectMembers.add(adrress);
  }

  /**
   * {@inheritDoc}
   */
  public void viewAccepted(View view) {
    viewChecker.putView(view);
  }

  private void viewAccepted(int viewSise) throws InterruptedException, PriorityDucplicatedException {
    priorityChecker.informAll();

    Thread.sleep(INFORM_TIMOUT);
    
    if (viewSise > 1)
      allInited = true;

    if (allInited == true)
      lastViewSize = viewSise;

    if (priorityChecker.hasDuplicatePriority()) {
      log.info(workspaceName + " set read-only");
      dataManager.setReadOnly(true);

      throw new PriorityDucplicatedException("The priority was duplicated :  own priority = "
          + ownPriority + ", other priority = " + priorityChecker.getOtherPriorities());
    }

    if (priorityChecker.isAllOnline()) {
      if (reconectTtread != null) {
        reconectTtread.setStop(false);
        reconectTtread = null;
      }

      memberRejoin();
      return;
    }

    if (priorityChecker instanceof GenericPriorityChecker) {
      if ( lastViewSize == 1 && (reconectTtread == null || reconectTtread.isStoped() == true)) {
        reconectTtread = new ReconectTtread(true);
        reconectTtread.start();
      }
    } else if (priorityChecker instanceof StaticPriorityChecker || otherPartisipants.size() == 1) {

      if (log.isDebugEnabled()) {
        log.debug("lastViewSize == 1 && !priorityChecker.isMaxPriority() == "
            + (lastViewSize == 1 && !priorityChecker.isMaxPriority()));
        log.debug("lastViewSize > 1 && !priorityChecker.isMaxOnline() == "
            + (lastViewSize > 1 && !priorityChecker.isMaxOnline()));
      }

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

  /**
   * The view checker. Will be check View.
   * 
   */
  private class ViewChecker extends Thread {
    private final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<Integer>();

    public void putView(View view) {
      log.info(" Memebers view :" + view.printDetails());

      queue.offer(view.size());
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      while (true) {
        try {
          Integer viewSize = queue.poll();

          if (viewSize != null)
            viewAccepted(viewSize);

          sleep(VIEW_CHECK * 2);
        } catch (PriorityDucplicatedException e) {
          log.error("The wrong priority :", e);
        } catch (Throwable t) {
          log.error("View check error :", t);
        }
      }

    }
  }

  /**
   * The ReconectTtread will be initialized reconnect to cluster.
   */
  private class ReconectTtread extends Thread {
    /**
     * The 'isStop' is a flag to run() stop.
     */
    private boolean isStop;

    /**
     * ReconectTtread constructor.
     * 
     * @param isStop
     *          the 'isStop' value
     */
    public ReconectTtread(boolean isStop) {
      log.info("Thread '" + getName() + "' is init ...");
      this.isStop = isStop;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      log.info("Thread '" + getName() + "' is run ...");
      while (isStop) {
        try {
          log.info("Connect to channel : " + channelName);
          Thread.sleep(BEFORE_CHECK);

          int curruntOnlin = 1;

          if (channelManager.getChannel() != null) {
            while (channelManager.getChannel().getView() == null)
              Thread.sleep(VIEW_CHECK);

            curruntOnlin = channelManager.getChannel().getView().size();
          }

          if (isStop && (curruntOnlin <= 1 || ((curruntOnlin > 1) && !priorityChecker.isMaxOnline()))) {
            channelManager.closeChannel();

            Thread.sleep(BEFORE_INIT);

            channelManager.init();
            channelManager.connect();
          } else {
            isStop = false;
          }
          Thread.sleep(AFTER_INIT);
        } catch (Exception e) {
          log.info(e, e);
        }
      }
    }

    /**
     * setStop.
     * 
     * @param isStop
     *          the 'isStop' value
     */
    public void setStop(boolean isStop) {
      this.isStop = isStop;
    }

    /**
     * isStoped.
     * 
     * @return boolean return the 'isStop' value
     */
    public boolean isStoped() {
      return !isStop;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void memberRejoin() {
    if (!(priorityChecker instanceof GenericPriorityChecker)) {
      log.info(workspaceName + " set not read-only");
      dataManager.setReadOnly(false);
    }

    log.info(workspaceName + " recovery start ...");
    recoveryManager.startRecovery();
  }

  /**
   * Call this method if maxPriority member was suspected.
   * 
   */
  public void memberSuspect() {
    if (!(priorityChecker instanceof GenericPriorityChecker)) {
      log.info(workspaceName + " set read-only");
      dataManager.setReadOnly(true);
    }
  }
}

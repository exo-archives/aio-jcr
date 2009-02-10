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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.storage.MemberChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.RandomChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizationException;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesSubscriberImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesSubscriberImpl extends SynchronizationLifeCycle implements ChangesSubscriber,
    RemoteEventListener, LocalEventListener, LocalEventProducer {

  /**
   * Logger.
   */
  protected static final Log                    LOG          = ExoLogger.getLogger("ext.ChangesSubscriberImpl");

  protected final MergeDataManager              mergeManager;

  protected final WorkspaceSynchronizer         workspace;

  protected final IncomeStorage                 incomeStorrage;

  protected final ChangesSaveErrorLog           errorLog;

  protected final AsyncTransmitter              transmitter;

  protected final AsyncInitializer              initializer;

  protected final int                           memberWaitTimeout;

  protected final int                           confMembersCount;

  protected final int                           localPriority;

  protected final HashMap<Integer, Counter>     counterMap;

  // protected List<MemberAddress> mergeDoneList = new ArrayList<MemberAddress>();

  protected final CountDownLatch                mergeBarier;

  private FirstChangesWaiter                    firstChangesWaiter;

  /**
   * Map with CRC key and RandomAccess File
   */
  protected HashMap<Integer, MemberChangesFile> incomChanges = new HashMap<Integer, MemberChangesFile>();

  protected MergeWorker                         mergeWorker  = null;

  /**
   * Listeners in order of addition.
   */
  protected final Set<LocalEventListener>       listeners    = new LinkedHashSet<LocalEventListener>();

  class MergeWorker extends Thread {

    private final Log         workerLog = ExoLogger.getLogger("ext.MergeWorker");

    ChangesStorage<ItemState> result    = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      try {
        MemberAddress localAddress = initializer.getLocalMember();

        runMerge(new Member(localAddress, localPriority));

        // add local done;
        if (isStarted()) {
          // mergeDoneList.add(localAddress);
          mergeBarier.countDown();

          try {
            transmitter.sendMerge();
          } catch (IOException ioe) {
            // TODO do we can continue on such error?
            workerLog.error("Cannot send 'Merge done'" + ioe, ioe);
          }
        }
      } catch (RepositoryException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
        return;
      } catch (RemoteExportException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
        return;
      } catch (IOException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
        return;
      } catch (ClassCastException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
        return;
      } catch (ClassNotFoundException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
        return;
      } catch (MergeDataManagerException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
        return;
      } catch (StorageRuntimeException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
        return;
      } catch (Throwable t) {
        workerLog.error("Merge error " + t, t);
        doCancel();
        return;
      }

      if (isStarted()) {
        try {
          workerLog.info("Waiting for other members.");
          mergeBarier.await();
          save();
          doStop();
        } catch (InterruptedException e) {
          workerLog.error("Save error " + e, e);
          doCancel();
        }
      }
    }

    private void runMerge(Member localMember) throws RepositoryException,
                                             RemoteExportException,
                                             IOException,
                                             ClassCastException,
                                             ClassNotFoundException,
                                             MergeDataManagerException,
                                             StorageRuntimeException {

      if (LOG.isDebugEnabled())
        LOG.debug("Run merge on " + localMember);

      // add local changes to the list
      List<MemberChangesStorage<ItemState>> membersChanges = incomeStorrage.getChanges();

      if (membersChanges.get(membersChanges.size() - 1).getMember().getPriority() < localMember.getPriority()) {
        membersChanges.add(new IncomeChangesStorage<ItemState>(workspace.getLocalChanges(),
                                                               localMember));
      } else {
        for (int i = 0; i < membersChanges.size(); i++) {
          if (membersChanges.get(i).getMember().getPriority() > localMember.getPriority()) {
            membersChanges.add(i, new IncomeChangesStorage<ItemState>(workspace.getLocalChanges(),
                                                                      localMember));
            break;
          }
        }
      }

      if (LOG.isDebugEnabled())
        for (MemberChangesStorage<ItemState> ms : membersChanges) {
          LOG.debug(">>> Member " + ms.getMember().getName() + " changes");
          LOG.debug(ms.dump());
        }

      // merge
      workerLog.info("Start merge of " + membersChanges.size() + " members");
      mergeManager.setLocalMember(localMember);
      result = mergeManager.merge(membersChanges.iterator());
      
      workerLog.info("Local merge done");
    }
  }

  private class Counter {
    int total;

    int counter;

    public Counter(int total, int counter) {
      this.total = total;
      this.counter = counter;
    }

    public void countUp() {
      counter++;
    }

    public boolean isTotalTransfer() {
      return total == counter;
    }
  }

  public ChangesSubscriberImpl(AsyncInitializer initializer,
                               AsyncTransmitter transmitter,
                               WorkspaceSynchronizer workspace,
                               MergeDataManager mergeManager,
                               IncomeStorage incomeStorage,
                               ChangesSaveErrorLog errorLog,
                               int memberWaitTimeout,
                               int localPriority,
                               int confMembersCount) {
    this.memberWaitTimeout = memberWaitTimeout;
    this.localPriority = localPriority;
    this.mergeManager = mergeManager;
    this.workspace = workspace;
    this.incomeStorrage = incomeStorage;
    this.errorLog = errorLog;
    this.initializer = initializer;
    this.transmitter = transmitter;
    this.mergeBarier = new CountDownLatch(confMembersCount);
    this.confMembersCount = confMembersCount;
    this.counterMap = new LinkedHashMap<Integer, Counter>();
  }

  public void addLocalListener(LocalEventListener listener) {
    listeners.add(listener);
  }

  public void removeLocalListener(LocalEventListener listener) {
    listeners.remove(listener);
  }

  public void onChanges(ChangesPacket packet, Member member) {

    switch (packet.getType()) {
    case AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET: {
      try {
        LOG.info("Receiving member " + member.getName() + " changes.");

        if (isInitialized()) {
          // Fire START on non-Coordinator
          LOG.info("START. Initiated by " + member.getName());

          // Start first member waiter
          firstChangesWaiter = new FirstChangesWaiter();
          firstChangesWaiter.start();

          doStart();

          for (LocalEventListener syncl : listeners)
            syncl.onStart(initializer.getOtherMembers());
        }

        if (isStarted()) {

          RandomChangesFile cf = incomeStorrage.createChangesFile(packet.getCRC(),
                                                                  packet.getTimeStamp(),
                                                                  member);
          cf.writeData(packet.getBuffer(), packet.getOffset());

          incomChanges.put(packet.getTransmitterPriority(), new MemberChangesFile(cf, member));
        } else
          LOG.error("First data packet received but the Subscriber is not started. Packet from "
              + member.getName() + " skipped");
      } catch (Throwable e) {
        doCancel();
        LOG.error("Error of First data packet processing. Packet from " + member.getName(), e);
      }
      break;
    }
    case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET: {
      if (isStarted()) {
        try {
          MemberChangesFile mcf = incomChanges.get(packet.getTransmitterPriority());
          mcf.getChangesFile().writeData(packet.getBuffer(), packet.getOffset());
        } catch (Throwable e) {
          doCancel();
          LOG.error("Error of Mid data packet processing. Packet from " + member.getName(), e);
        }
      } else
        LOG.error("Mid data packet received but the Subscriber is not started. Packet from "
            + member.getName() + " skipped");
      break;
    }
    case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET: {
      if (isStarted()) {
        LOG.info("Member " + member.getName() + " changes received.");

        try {
          MemberChangesFile mcf = incomChanges.get(packet.getTransmitterPriority());
          mcf.getChangesFile().finishWrite();
          incomeStorrage.addMemberChanges(mcf.getMember(), mcf.getChangesFile());

          Counter counter;
          if (counterMap.containsKey(packet.getTransmitterPriority())) {
            counter = counterMap.get(packet.getTransmitterPriority());
            counter.countUp();
          } else {
            counter = new Counter((int) packet.getFileCount(), 1);
            counterMap.put(packet.getTransmitterPriority(), counter);
          }

          // if all changes here, doStartMerge
          if (counter.isTotalTransfer() && isAllTransfered())
            if (mergeWorker == null) {
              mergeWorker = new MergeWorker();
              mergeWorker.start();
            } else
              LOG.error("Error, merge process laready activated.");

        } catch (Throwable e) {
          doCancel();
          LOG.error("Error of Last data packet processing. Packet from " + member.getName(), e);
        }
      } else
        LOG.error("Last data packet received but the Subscriber is not started. Packet from "
            + member + " skipped");
      break;
    }
    }
  }

  private boolean isAllTransfered() {
    if ((counterMap.size() + 1) != confMembersCount)
      return false;

    for (Map.Entry<Integer, Counter> e : counterMap.entrySet())
      if (e.getValue().isTotalTransfer() == false)
        return false;

    return true;
  }

  private void doCancel() {
    if (LOG.isDebugEnabled())
      LOG.debug("Do CANCEL (local)");

    if (isStarted()) {
      doStop();
      cancelMerge();

      try {
        transmitter.sendCancel();
      } catch (IOException ioe) {
        LOG.error("Cannot send 'Cancel'" + ioe, ioe);
      }

      for (LocalEventListener syncl : listeners)
        // inform all interested
        syncl.onCancel();
    } else
      LOG.warn("Cannot cancel. Already stopped.");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doStop() {
    super.doStop();

    if (firstChangesWaiter != null)
      firstChangesWaiter.cancel();
    else
      LOG.warn("First changes member is not initialized");

    // 04.02.2009, potentially can wait a long time for the merge end (cancel) - run it in
    // independent thread.
    new Thread("Merge canceler, " + new Date()) {
      /**
       * {@inheritDoc}
       */
      @Override
      public void run() {
        if (mergeWorker != null) {
          try {
            mergeWorker.join();
          } catch (InterruptedException e) {
            LOG.error("Error of merge process cancelation " + e, e);
          }

          mergeManager.cleanup();
        }
      }
    }.start();
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    if (LOG.isDebugEnabled())
      LOG.debug("On CANCEL");

    if (isStarted()) {
      doStop();
      cancelMerge();
    } else
      LOG.warn("Not started or already stopped");
  }

  /**
   * Cancel merge process if the ones exists.
   * 
   */
  private void cancelMerge() {
    if (mergeWorker != null) {
      mergeManager.cancel();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(MemberAddress member) {

    if (isStarted()) {

      if (LOG.isDebugEnabled())
        LOG.debug("On Merge member " + member + ", done=" + mergeBarier.getCount() + " members="
          + confMembersCount);

      mergeBarier.countDown();
    } else
      LOG.warn("Subscriber stopped. On Merge member " + member + " ignored.");
  }

  private synchronized void save() {
    LOG.info("Save changes.");

    try {
      if (LOG.isDebugEnabled())
        try {
          LOG.debug("save \r\n" + mergeWorker.result.dump());
        } catch (Throwable e1) {
          LOG.error("Changes dump error " + e1);
        }

      workspace.save(mergeWorker.result);

    } catch (InvalidItemStateException e) {
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    } catch (UnsupportedOperationException e) {
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    } catch (RepositoryException e) {
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    } catch (SynchronizationException e) {
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    }

    if (LOG.isDebugEnabled())
      LOG.debug("Fire Stop (local)");

    for (LocalEventListener ll : listeners)
      ll.onStop();
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // not interested
  }

  public void onStart(List<MemberAddress> members) {
    // not interested actually
    if (LOG.isDebugEnabled())
      LOG.debug("On START (local) " + members.size() + " members");

    // Start first member waiter
    firstChangesWaiter = new FirstChangesWaiter();
    firstChangesWaiter.start();

    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    if (LOG.isDebugEnabled())
      LOG.debug("On STOP (local)");

    if (isStarted())
      doStop();
    else
      LOG.warn("Not started or already stopped");
  }

  private class MemberChangesFile {
    private final RandomChangesFile changesFile;

    private final Member            member;

    public MemberChangesFile(RandomChangesFile changesFile, Member member) {
      this.changesFile = changesFile;
      this.member = member;
    }

    public RandomChangesFile getChangesFile() {
      return changesFile;
    }

    public Member getMember() {
      return member;
    }
  }

  /**
   * FirstChangesWaiter will be canceled when no changes from member.
   * 
   */
  private class FirstChangesWaiter extends Thread {

    private volatile boolean run = true;

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        Thread.sleep(memberWaitTimeout);

        if (run)
          if ((counterMap.size() + 1) != confMembersCount) {
            LOG.error("No changes from one of members, received " + (counterMap.size() + 1)
                + ", expected " + confMembersCount + ".");
            doCancel();
          } else if (LOG.isDebugEnabled())
            LOG.debug("FirstChangesWaiter stopped. Changes from all members ("
                + (counterMap.size() + 1) + ") received.");
      } catch (InterruptedException e) {
        LOG.error("FirstChangesWaiter is interrupted : " + e, e);
      }
    }

    void cancel() {
      run = false;
    }
  }
}

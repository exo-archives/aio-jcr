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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  protected static final Log                LOG           = ExoLogger.getLogger("ext.ChangesSubscriberImpl");

  protected final MergeDataManager          mergeManager;

  protected final WorkspaceSynchronizer     workspace;

  protected final IncomeStorage             incomeStorrage;

  protected final ChangesSaveErrorLog       errorLog;

  protected final AsyncTransmitter          transmitter;

  protected final AsyncInitializer          initializer;
  
  protected final int                       memberWaitTimeout;

  protected final int                       membersCount;

  protected final int                       localPriority;

  protected final HashMap<Integer, Counter> counterMap;

  protected List<MemberAddress>             mergeDoneList = new ArrayList<MemberAddress>();
  
  private FirstChangesWaiter                firstChangesWaiter;

  /**
   * Map with CRC key and RandomAccess File
   */
  protected HashMap<Key, MemberChangesFile> incomChanges  = new HashMap<Key, MemberChangesFile>();

  protected MergeWorker                     mergeWorker   = null;

  /**
   * Listeners in order of addition.
   */
  protected final Set<LocalEventListener>   listeners     = new LinkedHashSet<LocalEventListener>();

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
          mergeDoneList.add(localAddress);

          try {
            transmitter.sendMerge();
          } catch (IOException ioe) {
            // TODO do we can continue on such error?
            LOG.error("Cannot send 'Cancel'" + ioe, ioe);
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

      if (isStarted() && mergeDoneList.size() == membersCount) {
        save();
        doStop();
      }
    }

    /**
     * Try cancel merge.
     * 
     * @throws RemoteExportException
     * @throws RepositoryException
     * 
     */
    public void cancel() throws RepositoryException, RemoteExportException {
      mergeManager.cancel();
    }

    private void runMerge(Member localMember) throws RepositoryException,
                                             RemoteExportException,
                                             IOException,
                                             ClassCastException,
                                             ClassNotFoundException,
                                             MergeDataManagerException,
                                             StorageRuntimeException {

      LOG.info("run merge on " + localMember);

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

      // TODO debug
      for (MemberChangesStorage<ItemState> ms : membersChanges) {
        LOG.info(">>> Member " + ms.getMember().getName() + " changes");
        LOG.info(ms.dump());
      }

      // merge
      workerLog.info("start merge of " + membersChanges.size() + " members");
      mergeManager.setLocalMember(localMember);
      result = mergeManager.merge(membersChanges.iterator());
      workerLog.info("merge done");
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
                               int membersCount) {
    this.memberWaitTimeout = memberWaitTimeout;
    this.localPriority = localPriority;
    this.mergeManager = mergeManager;
    this.workspace = workspace;
    this.incomeStorrage = incomeStorage;
    this.errorLog = errorLog;
    this.initializer = initializer;
    this.transmitter = transmitter;
    this.membersCount = membersCount;
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
        LOG.info("BINARY_CHANGESLOG_FIRST_PACKET " + member.getName());

        if (isInitialized()) {
          // Fire START on non-Coordinator
          LOG.info("On START (remote) from " + member.getName());

          doStart();

          for (LocalEventListener syncl : listeners)
            syncl.onStart(initializer.getOtherMembers());
        }

        RandomChangesFile cf = incomeStorrage.createChangesFile(packet.getCRC(),
                                                                packet.getTimeStamp(),
                                                                member);
        cf.writeData(packet.getBuffer(), packet.getOffset());

        // packet.getFileCount(); // TODO remeber whole packets count for this member

        incomChanges.put(new Key(packet.getCRC(), packet.getTimeStamp()),
                         new MemberChangesFile(cf, member));
      } catch (Throwable e) {
        doCancel();
        LOG.error("Error of First data packet processing. Member " + member, e);
      }
      break;
    }
    case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET: {
      try {
        // LOG.info("BINARY_CHANGESLOG_MIDDLE_PACKET " + member.getName());

        MemberChangesFile mcf = incomChanges.get(new Key(packet.getCRC(), packet.getTimeStamp()));
        mcf.getChangesFile().writeData(packet.getBuffer(), packet.getOffset());
      } catch (Throwable e) {
        doCancel();
        LOG.error("Error of Mid data packet processing. Member " + member, e);
      }
      break;
    }
    case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET: {
      try {
        LOG.info("BINARY_CHANGESLOG_LAST_PACKET " + member.getName());

        MemberChangesFile mcf = incomChanges.get(new Key(packet.getCRC(), packet.getTimeStamp()));
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
        LOG.error("Error of Last data packet processing. Member " + member, e);
      }
      break;
    }
    }
  }

  private boolean isAllTransfered() {
    if ((counterMap.size() + 1) != membersCount)
      return false;

    for (Map.Entry<Integer, Counter> e : counterMap.entrySet())
      if (e.getValue().isTotalTransfer() == false)
        return false;

    return true;
  }

  private void doCancel() {
    LOG.error("Do CANCEL (local)");

    if (isStarted()) {
      cancelMerge();

      try {
        transmitter.sendCancel();
      } catch (IOException ioe) {
        LOG.error("Cannot send 'Cancel'" + ioe, ioe);
      }

      doStop();

      for (LocalEventListener syncl : listeners)
        // inform all interested
        syncl.onCancel(); // local done - null
    } else
      LOG.warn("Cannot cancel. Already stopped.");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doStop() {
    super.doStop();
    mergeDoneList = null;
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    LOG.info("On CANCEL");

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
    if (mergeWorker != null)
      try {
        mergeWorker.cancel();

      } catch (RepositoryException e) {
        LOG.error("Error of merge process cancelation " + e, e);
      } catch (RemoteExportException e) {
        LOG.error("Error of merge process cancelation " + e, e);
      }
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(MemberAddress member) {

    if (isStarted()) {

      mergeDoneList.add(member);

      LOG.info("On Merge member " + member + ", doneList.size=" + mergeDoneList.size()
          + " membersCount=" + membersCount);

      if (mergeDoneList.size() == membersCount) {
        save();
        doStop();
      }
    } else
      LOG.warn("Subscriber stopped. On Merge member " + member + " ignored.");
  }

  private synchronized void save() {
    LOG.info("save");

    try {
      workspace.save(mergeWorker.result);
    } catch (InvalidItemStateException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    } catch (UnsupportedOperationException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    } catch (RepositoryException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    } catch (SynchronizationException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
      errorLog.reportError(e);
    }

    LOG.info("Fire Stop (local)");
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
    LOG.info("On START (local) " + members.size() + " members");
    
    //Start first member waiter
    firstChangesWaiter = new FirstChangesWaiter();
    firstChangesWaiter.start();

    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    LOG.info("On STOP (local)");

    if (isStarted())
      doStop();
    else
      LOG.warn("Not started or already stopped");
  }

  private class Key {
    private final String crc;

    private final Long   timeStamp;

    public Key(String crc, long timeStamp) {
      this.crc = crc;
      this.timeStamp = timeStamp;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
      Key k = (Key) o;

      return crc.equals(k.getCrc()) && timeStamp == k.getTimeStamp();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
      return crc.hashCode() ^ timeStamp.hashCode();
    }

    public String getCrc() {
      return crc;
    }

    public long getTimeStamp() {
      return timeStamp;
    }
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
    protected volatile boolean run = true;

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        Thread.sleep(memberWaitTimeout);
        
        if ((counterMap.size() + 1) != membersCount) {
          LOG.error("No changes from member : ");
          doCancel();
        }
      } catch (InterruptedException e) {
        LOG.error("FirstChangesWaiter is interrupted : " + e, e);
      }
    }

  }
}

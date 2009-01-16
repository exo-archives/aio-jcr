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
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizationException;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
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
  private static final Log                        LOG         = ExoLogger.getLogger("ext.ChangesSubscriberImpl");

  /**
   * Map with CRC key and RandomAccess File
   */
  protected final HashMap<Key, MemberChangesFile> incomChanges;

  protected final MergeDataManager                mergeManager;

  protected final WorkspaceSynchronizer           workspace;

  protected final IncomeStorage                   incomeStorrage;

  protected final AsyncTransmitter                transmitter;

  protected HashMap<Integer, Counter>             counterMap;

  protected volatile List<Integer>                doneList;

  protected final int                             localPriority;

  protected final int                             membersCount;

  protected MergeWorker                           mergeWorker = null;

  /**
   * Listeners in order of addition.
   */
  protected final Set<LocalEventListener>         listeners   = new LinkedHashSet<LocalEventListener>();

  class MergeWorker extends Thread {

    private final Log         workerLog = ExoLogger.getLogger("ext.MergeWorker");

    ChangesStorage<ItemState> result    = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      try {
        runMerge();

        // add local done;
        addMergeDone(localPriority);

        try {
          transmitter.sendMerge();
        } catch (IOException ioe) {
          // TODO do we can continue on such error?
          LOG.error("Cannot send 'Cancel'" + ioe, ioe);
        }

        if (doneList.size() == membersCount) { 
          save();
          doStop();
        }

      } catch (RepositoryException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
      } catch (RemoteExportException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
      } catch (IOException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
      } catch (ClassCastException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
      } catch (ClassNotFoundException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
      } catch (MergeDataManagerException e) {
        workerLog.error("Merge error " + e, e);
        doCancel();
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

    private void runMerge() throws RepositoryException,
                           RemoteExportException,
                           IOException,
                           ClassCastException,
                           ClassNotFoundException,
                           MergeDataManagerException {

      LOG.error("run merge");

      // add local changes to the list
      List<ChangesStorage<ItemState>> membersChanges = incomeStorrage.getChanges();
      if (membersChanges.get(membersChanges.size() - 1).getMember().getPriority() < localPriority) {
        membersChanges.add(workspace.getLocalChanges());
      } else {
        for (int i = 0; i < membersChanges.size(); i++) {
          if (membersChanges.get(i).getMember().getPriority() > localPriority) {
            membersChanges.add(i, workspace.getLocalChanges());
            break;
          }
        }
      }

      // merge
      workerLog.info("start merge of " + (membersChanges.size() - 1) + " members");
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

  public ChangesSubscriberImpl(WorkspaceSynchronizer workspace,
                               MergeDataManager mergeManager,
                               IncomeStorage incomeStorage,
                               AsyncTransmitter transmitter,
                               int localPriority,
                               int membersCount) {
    this.incomChanges = new HashMap<Key, MemberChangesFile>();
    this.mergeManager = mergeManager;
    this.workspace = workspace;
    this.incomeStorrage = incomeStorage;
    this.transmitter = transmitter;
    this.localPriority = localPriority;
    this.membersCount = membersCount;
  }

  public void addLocalListener(LocalEventListener listener) {
    listeners.add(listener);
  }

  public void removeLocalListener(LocalEventListener listener) {
    listeners.remove(listener);
  }

  public void onChanges(ChangesPacket packet, Member member) {

    try {
      switch (packet.getType()) {
      case AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET: {
        LOG.info("BINARY_CHANGESLOG_FIRST_PACKET " + member.getName());

        // Fire event to Publisher to send own changes out
        if (isInitialized()) {
          LOG.info("On START (remote) from " + member.getName());
          
          doStart();

          for (LocalEventListener syncl : listeners)
            syncl.onStart(transmitter.getOtherMembers());
        }

        ChangesFile cf = incomeStorrage.createChangesFile(packet.getCRC(),
                                                          packet.getTimeStamp(),
                                                          member);
        cf.writeData(packet.getBuffer(), packet.getOffset());

        // packet.getFileCount(); // TODO remeber whole packets count for this member

        incomChanges.put(new Key(packet.getCRC(), packet.getTimeStamp()),
                         new MemberChangesFile(cf, member));
        break;
      }
      case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET: {
        LOG.info("BINARY_CHANGESLOG_MIDDLE_PACKET " + member.getName());

        MemberChangesFile mcf = incomChanges.get(new Key(packet.getCRC(), packet.getTimeStamp()));
        mcf.getChangesFile().writeData(packet.getBuffer(), packet.getOffset());
        break;
      }
      case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET: {
        LOG.info("BINARY_CHANGESLOG_LAST_PACKET " + member.getName());

        MemberChangesFile mcf = incomChanges.get(new Key(packet.getCRC(), packet.getTimeStamp()));
        mcf.getChangesFile().finishWrite();
        incomeStorrage.addMemberChanges(mcf.getMember(), mcf.getChangesFile());

        if (counterMap == null)
          counterMap = new LinkedHashMap<Integer, Counter>();

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

        break;
      }
      }
    } catch (IOException e) {
      LOG.error("Cannot save changes from member " + member.getAddress() + ". Error: " + e, e);

      // local cancel, incl. merge.
      // and remote cancel.
      doCancel();
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
  }

  /**
   * Add 'done' to list.
   * 
   * @param priority
   *          the value of priority.
   */
  private void addMergeDone(int priority) {
    if (doneList == null)
      doneList = new ArrayList<Integer>();

    doneList.add(priority);
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    LOG.info("On CANCEL");

    doStop();

    cancelMerge();
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
  public void onMerge(Member member) {

    addMergeDone(member.getPriority());
    
    LOG.info("On Merge member " + member.getName() + ", doneList.size=" + doneList.size() + " membersCount="
      + membersCount);

    if (doneList.size() == membersCount) {
      save();
      doStop();
    }
  }

  private synchronized void save() {
    LOG.info("save");
    
    try {
      workspace.save(mergeWorker.result);
    } catch (InvalidItemStateException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
    } catch (UnsupportedOperationException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
    } catch (RepositoryException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
    } catch (SynchronizationException e) {
      // TODO fire Cancel for local modules
      LOG.error("Save error " + e, e);
    }
    
    LOG.info("Fire Stop (local)");
    for (LocalEventListener ll : listeners)
      ll.onStop();
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // TODO Auto-generated method stub

  }

  public void onStart(List<Member> members) {
    // not interested actually
    LOG.info("On START (local) " + members.size() + " members");

    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    // TODO Auto-generated method stub
    LOG.info("On STOP (local)");

    doStop();
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
    private final ChangesFile changesFile;

    private final Member      member;

    public MemberChangesFile(ChangesFile changesFile, Member member) {
      this.changesFile = changesFile;
      this.member = member;
    }

    public ChangesFile getChangesFile() {
      return changesFile;
    }

    public Member getMember() {
      return member;
    }
  }
}

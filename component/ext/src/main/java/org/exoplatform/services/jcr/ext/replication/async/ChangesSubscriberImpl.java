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

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorage;
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
public class ChangesSubscriberImpl implements ChangesSubscriber, SynchronizationEventListener,
    SynchronizationEventProducer {

  /**
   * Logger.
   */
  private static Log                                log          = ExoLogger.getLogger("ext.ChangesSubscriberImpl");

  /**
   * Map with CRC key and RandomAccess File
   */
  protected final HashMap<Key, MemberChangesFile>   incomChanges;

  protected final MergeDataManager                  mergeManager;

  protected final WorkspaceSynchronizer             workspace;

  protected final IncomeStorage                     incomeStorrage;

  protected final AsyncTransmitter                  transmitter;
  
  protected HashMap<Integer, Counter>               counterMap;
  
  protected List<Integer>                           doneList;

  protected final int                               localPriority;
  
  protected final int                               membersCount;
  
  protected MergeWorker                             mergeWorker = null;

  /**
   * Listeners in order of addition.
   */
  protected final Set<SynchronizationEventListener> listeners    = new LinkedHashSet<SynchronizationEventListener>();

  class MergeWorker extends Thread {

    ChangesStorage<ItemState> result = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      try {
        runMerge();
        
        doDone();
        
        
      } catch (RepositoryException e) {
        log.error("Merge error " + e, e);
        doCancel();
      } catch (RemoteExportException e) {
        log.error("Merge error " + e, e);
        doCancel();
      } catch (IOException e) {
        log.error("Merge error " + e, e);
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

    private void runMerge() throws RepositoryException, RemoteExportException, IOException {
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
      result = mergeManager.merge(membersChanges.iterator());
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

  public void addSynchronizationListener(SynchronizationEventListener listener) {
    listeners.add(listener);
  }

  public void removeSynchronizationListener(SynchronizationEventListener listener) {
    listeners.remove(listener);
  }

  public void onChanges(ChangesPacket packet, Member member) {

    try {
      switch (packet.getType()) {
      case AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET:
        ChangesFile cf = incomeStorrage.createChangesFile(packet.getCRC(), packet.getTimeStamp());
        Member mem = new Member(member.getAddress(), packet.getTransmitterPriority());

        cf.writeData(packet.getBuffer(), packet.getOffset());

        packet.getFileCount(); // TODO remeber whole packets count for this member

        incomChanges.put(new Key(packet.getCRC(), packet.getTimeStamp()),
                         new MemberChangesFile(cf, mem));
        break;

      case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET:
        cf = incomChanges.get(new Key(packet.getCRC(), packet.getTimeStamp())).getChangesFile();
        cf.writeData(packet.getBuffer(), packet.getOffset());
        break;

      case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET:
        MemberChangesFile mcf = incomChanges.get(new Key(packet.getCRC(), packet.getTimeStamp()));
        incomeStorrage.addMemberChanges(mcf.getMember(), mcf.changesFile);

        if (counterMap == null)
          counterMap = new LinkedHashMap<Integer, Counter>();
        
        if (counterMap.containsKey(packet.getTransmitterPriority())) {
          Counter counter = counterMap.get(packet.getTransmitterPriority()); 
          counter.countUp();
          
          if (counter.isTotalTransfer()) 
            if (isAllTransfered())
              doMerge();
          
        } else {
          Counter counter = new Counter((int)packet.getFileCount(), 1);
          counterMap.put(packet.getTransmitterPriority(), counter);
        }
          
        // TODO if all changes here, doMerge

        break;

      }
    } catch (IOException e) {
      log.error("Cannot save changes " + e, e);
      
      // local cancel, incl. merge.
      // and remote cancel.
      doCancel();
    }
  }
  
  private boolean isAllTransfered() {
    if ((counterMap.size() + 1) != membersCount )
      return false;
    
    for( Map.Entry<Integer, Counter>  e: counterMap.entrySet()) 
      if (e.getValue().isTotalTransfer() == false)
        return false;
    
    return true;
  }

  protected void doMerge() {
    if (mergeWorker == null) {
      mergeWorker = new MergeWorker();
      mergeWorker.start();
    } else 
      log.error("Error, merge process laready activated.");
  }

  protected void doCancel() {
    log.error("Do CANCEL");
    
    mergeCancel();
    
    for (SynchronizationEventListener syncl : listeners)
      // inform all interested
      syncl.onCancel(null); // local done - null
    
    try {
      transmitter.sendCancel();
    } catch (IOException ioe) {
      log.error("Cannot send 'Cancel'" + ioe, ioe);
    }
  }
  
  protected void doDone() {
    log.error("Do DONE");
    for (SynchronizationEventListener syncl : listeners)
      // inform all interested
      syncl.onDone(null); // local done - null
    
    try {
      transmitter.sendDone();
    } catch (IOException ioe) {
      log.error("Cannot send 'Cancel'" + ioe, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel(Member member) {
    mergeCancel();
  }
  
  private void mergeCancel() {
    if (mergeWorker != null)
      try {
        mergeWorker.cancel();
      } catch (RepositoryException e) {
        log.error("Error of merge process cancelation " + e, e);
      } catch (RemoteExportException e) {
        log.error("Error of merge process cancelation " + e, e);
      }
  }

  /**
   * {@inheritDoc}
   */
  public void onDone(Member member) {
     if (doneList == null) 
       doneList = new ArrayList<Integer>();
     
     // check if cell it self.
     if (member == null)
       doneList.add(localPriority);
     else 
       doneList.add(member.getPriority());
     
     // TODO run save 
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // TODO Auto-generated method stub

  }

  public void onStart(List<Member> members) {
    // nothing to do
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

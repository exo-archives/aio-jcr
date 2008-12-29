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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
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
  private static Log                                log       = ExoLogger.getLogger("ext.ChangesSubscriberImpl");

  /**
   * Map with CRC key and RandomAccess File
   */
  protected final HashMap<Key, MemberChangesFile>   incomChanges;

  protected final MergeDataManager                  mergeManager;

  protected final IncomeStorage                     incomeStorrage;

  protected final AsyncTransmitter                  transmitter;

  /**
   * Listeners in order of addition.
   */
  protected final Set<SynchronizationEventListener> listeners = new LinkedHashSet<SynchronizationEventListener>();

  public ChangesSubscriberImpl(MergeDataManager mergeManager,
                               IncomeStorage incomeStorage,
                               AsyncTransmitter transmitter) {
    this.incomChanges = new HashMap<Key, MemberChangesFile>();
    this.mergeManager = mergeManager;
    this.incomeStorrage = incomeStorage;
    this.transmitter = transmitter;
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

        // TODO if all changes here, doMerge

        break;

      }
    } catch (IOException e) {
      // TODO
      log.error("Cannot save changes " + e, e);
      // transmitter.sendCancel(error, address)

      // TODO local cancel, incl. merge
    }
  }

  protected void doMerge() throws RepositoryException, RemoteExportException, IOException {
    // TODO run merge in dedicated Thread, the merge can be canceled see merege manager cancel
    mergeManager.merge(incomeStorrage.getChanges());
  }

  protected void doCancel() {
    for (SynchronizationEventListener syncl : listeners)
      // inform all interested
      syncl.onDone(null); // TODO local done - null
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel(Member member) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void onDone(Member member) {
    // TODO Auto-generated method stub

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

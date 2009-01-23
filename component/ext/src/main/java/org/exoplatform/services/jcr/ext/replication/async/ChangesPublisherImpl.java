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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 29.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ChangesPublisherImpl.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ChangesPublisherImpl extends SynchronizationLifeCycle implements ChangesPublisher,
    RemoteEventListener, LocalEventListener, LocalEventProducer {

  /**
   * Logger.
   */
  private static final Log                LOG       = ExoLogger.getLogger("ext.ChangesPublisherImpl");

  protected final AsyncTransmitter        transmitter;

  protected final LocalStorage            storage;

  /**
   * Listeners in order of addition.
   */
  protected final Set<LocalEventListener> listeners = new LinkedHashSet<LocalEventListener>();

  protected PublisherWorker               publisherWorker;

  private class PublisherWorker extends Thread {
    private List<Member> subscribers;

    public PublisherWorker(List<Member> subscribers) {
      this.subscribers = new ArrayList<Member>(subscribers);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        LOG.info("Local chahges : " + storage.getLocalChanges().getChangesFile().length);
        
        List<MemberAddress> sa = new ArrayList<MemberAddress>();
        for (Member m: subscribers) 
          sa.add(m.getAddress());
        
        transmitter.sendChanges(storage.getLocalChanges().getChangesFile(), sa);
      } catch (IOException e) {
        LOG.error("Cannot send changes " + e, e);
        doCancel();
      }
    }
  }

  public ChangesPublisherImpl(AsyncTransmitter transmitter, LocalStorage storage) {
    this.transmitter = transmitter;
    this.storage = storage;
  }

  public void sendChanges(List<Member> members) {
    publisherWorker = new PublisherWorker(members);
    publisherWorker.start();
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    doStop();

    cancelWorker();
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(Member member) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(Member localMember, List<Member> members) {
    LOG.info("On START (local) " + members.size() + " members");

    doStart();

    sendChanges(members);
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    LOG.info("On STOP (local)");

    doStop();
  }

  private void cancelWorker() {
    if (publisherWorker != null)
      try {
        // Stop publisher.
        publisherWorker.join();
      } catch (InterruptedException e) {
        LOG.error("Error of publisher process cancelation " + e, e);
      }
  }

  protected void doCancel() {
    LOG.error("Do CANCEL (local)");

    try {
      transmitter.sendCancel();
    } catch (IOException ioe) {
      LOG.error("Cannot send 'Cancel' " + ioe, ioe);
    }

    for (LocalEventListener syncl : listeners)
      // inform all interested
      syncl.onCancel();
  }

  /**
   * {@inheritDoc}
   */
  public void addLocalListener(LocalEventListener listener) {
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void removeLocalListener(LocalEventListener listener) {
    listeners.remove(listener);
  }

}

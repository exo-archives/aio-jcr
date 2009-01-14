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
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 29.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ChangesPublisherImpl.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ChangesPublisherImpl implements ChangesPublisher, RemoteEventListener,
    LocalEventListener, LocalEventProducer {

  /**
   * Logger.
   */
  private static Log                      log       = ExoLogger.getLogger("ext.ChangesSubscriberImpl");

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
        runSendChanges();
      } catch (IOException e) {
        log.error("Cannot send changes " + e, e);
        doCancel();
      }
    }

    private void runSendChanges() throws IOException {
      ChangesStorage<ItemState> local = storage.getLocalChanges();

      ChangesFile[] files = local.getChangesFile();

      List<ChangesFile> filesList = new ArrayList<ChangesFile>(files.length);
      for (ChangesFile cf : files)
        filesList.add(cf);

      transmitter.sendChanges(filesList, subscribers);
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
    publisherCancel();
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(Member member) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<Member> members) {
    sendChanges(members); 
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    // TODO
    // Publisher will stop work, run local storage rotation and set Repository RW state.
  }

  private void publisherCancel() {
    if (publisherWorker != null)
      try {
        // Stop publisher.
        publisherWorker.join();
      } catch (InterruptedException e) {
        log.error("Error of publisher process cancelation " + e, e);
      }
  }

  protected void doCancel() {
    log.error("Do CANCEL");

    for (LocalEventListener syncl : listeners)
      // inform all interested
      syncl.onCancel(); // local done - null

    try {
      transmitter.sendCancel();
    } catch (IOException ioe) {
      log.error("Cannot send 'Cancel' " + ioe, ioe);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addLocalListener(LocalEventListener listener) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void removeLocalListener(LocalEventListener listener) {
    // TODO Auto-generated method stub

  }

}

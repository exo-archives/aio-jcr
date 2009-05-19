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
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChannelNotConnectedException;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChannelWasDisconnectedException;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 29.12.2008
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

  protected final AsyncInitializer        initializer;

  protected final AsyncTransmitter        transmitter;

  protected final LocalStorage            storage;

  /**
   * Listeners in order of addition.
   */
  protected final Set<LocalEventListener> listeners = new LinkedHashSet<LocalEventListener>();

  protected PublisherWorker               publisherWorker;

  private class PublisherWorker extends Thread {

    private final List<MemberAddress> subscribers;

    private volatile boolean          run = true;

    public PublisherWorker(List<MemberAddress> subscribers) {
      this.subscribers = new ArrayList<MemberAddress>(subscribers);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        ChangesFile[] localChanges = storage.getLocalChanges(true).getChangesFile();
        if (LOG.isDebugEnabled())
          LOG.debug("Local changes : " + localChanges.length);

        if (isStarted() && run) {
          for (ChangesFile cf : localChanges)
            if (run)
              transmitter.sendChanges(cf, subscribers, localChanges.length);
            else
              return;
        }
      } catch (ChannelWasDisconnectedException e) {
        // The channel was disconnected -->> stop send changes.
      } catch (ChannelNotConnectedException e) {
        LOG.error("Cannot send changes " + e, e);
        doCancel();
      } catch (IOException e) {
        LOG.error("Cannot send changes " + e, e);
        doCancel();
      }
    }

    void cancel() {
      run = false;
    }
  }

  public ChangesPublisherImpl(AsyncInitializer initializer,
                              AsyncTransmitter transmitter,
                              LocalStorage storage) {
    this.initializer = initializer;
    this.transmitter = transmitter;
    this.storage = storage;
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    if (LOG.isDebugEnabled())
      LOG.debug("On CANCEL (local)");

    if (isStarted()) {
      cancelWorker();
      doStop();
    } else
      LOG.warn("Not started or already stopped");
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
  public void onMerge(MemberAddress member) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<MemberAddress> members) {
    if (LOG.isDebugEnabled())
      LOG.debug("On START (local) " + members.size() + " members");

    doStart();

    publisherWorker = new PublisherWorker(members);
    publisherWorker.start();
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    if (LOG.isDebugEnabled())
      LOG.debug("On STOP (local)");

    if (isStarted()) {
      cancelWorker();

      doStop();

      publisherWorker = null;
    } else
      LOG.warn("Not started or already stopped");
  }

  private void cancelWorker() {
    if (publisherWorker != null)
      publisherWorker.cancel();
  }

  protected void doCancel() {
    if (LOG.isDebugEnabled())
      LOG.debug("Do CANCEL (local)");

    if (isStarted()) {
      try {
        transmitter.sendCancel();
      } catch (IOException ioe) {
        LOG.error("Cannot send 'Cancel' " + ioe, ioe);
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

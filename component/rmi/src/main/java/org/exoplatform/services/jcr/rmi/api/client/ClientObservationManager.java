/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.iterator.ArrayEventListenerIterator;
import org.exoplatform.services.jcr.rmi.api.observation.ClientEventPoll;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteObservationManager;

/**
 * The <code>ClientObservationManager</code> class
 * <p>
 * This class uses an instance of the
 * {@link org.exoplatform.services.jcr.rmi.api.observation.ClientEventPoll}
 * class for the actual registration and event dispatching.
 * <p>
 * This class does not require the
 * {@link org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory} and
 * consequently calls the base class constructor with a <code>null</code>
 * factory.
 * <p>
 * See the <a href="../observation/package.html><code>observation</code></a>
 * package comment for a description on how event listener registration and
 * notification is implemented.
 * 
 * @see org.exoplatform.services.jcr.rmi.api.observation.ClientEventPoll
 */
public class ClientObservationManager extends ClientObject implements ObservationManager {

  /** The remote observation manager */
  private final RemoteObservationManager remote;

  /** The <code>Workspace</code> to which this observation manager belongs. */
  private final Workspace                workspace;

  /** The ClientEventPoll class internally used for event dispatching */
  private ClientEventPoll                poller;

  /**
   * Creates an instance of this class talking to the given remote observation
   * manager.
   * 
   * @param remote The {@link RemoteObservationManager} backing this client-side
   *          observation manager.
   * @param workspace The <code>Workspace</code> instance to which this
   *          observation manager belongs.
   */
  public ClientObservationManager(Workspace workspace, RemoteObservationManager remote) {
    super(null);
    this.remote = remote;
    this.workspace = workspace;
  }

  /** {@inheritDoc} */
  public void addEventListener(EventListener listener, int eventTypes, String absPath,
      boolean isDeep, String[] uuid, String[] nodeTypeName, boolean noLocal)
      throws RepositoryException {
    try {
      long listenerId = getClientEventPoll().addListener(listener);
      remote.addEventListener(listenerId, eventTypes, absPath, isDeep, uuid, nodeTypeName, noLocal);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void removeEventListener(EventListener listener) throws RepositoryException {
    try {
      long id = getClientEventPoll().removeListener(listener);
      remote.removeEventListener(id);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public EventListenerIterator getRegisteredEventListeners() {
    return new ArrayEventListenerIterator(getClientEventPoll().getListeners());
  }

  // ---------- internal ------------------------------------------------------

  /**
   * Returns the {@link ClientEventPoll} instance used by this (client-side)
   * observation manager. This method creates the instance on the first call and
   * starts the poller thread to wait for remote events.
   * 
   * @return poller instance
   */
  private synchronized ClientEventPoll getClientEventPoll() {
    if (poller == null) {
      poller = new ClientEventPoll(remote, workspace.getSession());
      poller.start();
    }
    return poller;
  }
}

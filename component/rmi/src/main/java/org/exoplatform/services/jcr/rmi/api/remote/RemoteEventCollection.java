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
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>RemoteEventCollection</code> class serves as a container for
 * notifications sent to registered event listeners. Instances of this class are
 * created by the server-side event listener proxies and sent to the client-side
 * event poller. On the client-side the enclosed list of events is then sent to
 * the listener identified by the contained listener identifier.
 */
public interface RemoteEventCollection extends Remote {

  /**
   * Returns unique identifier of the client-side listener to which the enclosed
   * events should be sent.
   * 
   * @return unique listener identifier
   * @throws RemoteException on RMI errors
   */
  long getListenerId() throws RemoteException;

  /**
   * Returns the list of events to be sent to the client-side listener
   * identified by {@link #getListenerId()}.
   * 
   * @return list of events
   * @throws RemoteException on RMI errors
   */
  RemoteEvent[] getEvents() throws RemoteException;

  /**
   * The <code>RemoteEvent</code> class provides an encapsulation of single
   * events in an event list sent to a registered listener.
   */
  public static interface RemoteEvent extends Remote {
    /**
     * Returns the event type.
     * 
     * @return event type
     * @throws RemoteException on RMI errors
     */
    int getType() throws RemoteException;

    /**
     * Returns the absolute path of the underlying item.
     * 
     * @return item path
     * @throws RemoteException on RMI errors
     */
    String getPath() throws RemoteException;

    /**
     * Returns the userID of the session causing this event.
     * 
     * @return user identifier
     * @throws RemoteException on RMI errors
     */
    String getUserID() throws RemoteException;
  }

}

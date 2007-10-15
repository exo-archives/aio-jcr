/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteEventCollection;

/**
 * The <code>ServerEventCollection</code> class implemnts the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteEventCollection}event
 * to actually sent the server-side event to the client.
 * <p>
 * This class does not directly relate to any JCR class because beside the list
 * of events the unique identifier of the client-side listener has to be
 * provided such that the receiving listener may be identified on the
 * client-side.
 * <p>
 * This class does not require the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.RemoteAdapterFactory}and
 * consequently calls the base class constructor with a <code>null</code>
 * factory.
 */
public class ServerEventCollection extends ServerObject implements RemoteEventCollection {

  /**
   * 
   */
  private static final long   serialVersionUID = 4068396059549155011L;

  /** The unique identifier of the receiving listener */
  private final long          listenerId;

  /**
   * The list of
   * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteEventCollection.RemoteEvent}.
   */
  private final RemoteEvent[] events;

  /**
   * Creates an instance of this class.
   * 
   * @param listenerId The unique identifier of the client-side listener to
   *          which the events should be sent.
   * @param events The list of {@link RemoteEvent remote events}.
   * @throws RemoteException on RMI errors
   */
  ServerEventCollection(long listenerId, RemoteEvent[] events) throws RemoteException {
    super(null);

    this.listenerId = listenerId;
    this.events = events;
  }

  /** {@inheritDoc} */
  public long getListenerId() {
    return listenerId;
  }

  /** {@inheritDoc} */
  public RemoteEvent[] getEvents() {
    return events;
  }

  /**
   * Server side implementation of the {@link RemoteEvent} interface.
   * {@inheritDoc}
   */
  public static class ServerEvent extends ServerObject implements RemoteEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -7734381965933679052L;

    /** Event type */
    private final int         type;

    /** Item path */
    private final String      path;

    /** User identifier */
    private final String      userID;

    /**
     * Creates an instance of this class.
     * 
     * @param type The event type.
     * @param path The absolute path to the underlying item.
     * @param userId The userID of the originating session.
     * @throws RemoteException declared because of the declaration in the base
     *           class constructor called. In fact this exception is never
     *           thrown.
     */
    ServerEvent(int type, String path, String userId) throws RemoteException {
      super(null);
      this.type = type;
      this.path = path;
      this.userID = userId;
    }

    /** {@inheritDoc} */
    public String getPath() {
      return path;
    }

    /** {@inheritDoc} */
    public int getType() {
      return type;
    }

    /** {@inheritDoc} */
    public String getUserID() {
      return userID;
    }
  }
}

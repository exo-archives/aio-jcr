/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.exoplatform.services.cifs.server;

import java.net.InetAddress;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.exoplatform.services.cifs.server.auth.AuthContext;
import org.exoplatform.services.cifs.server.core.SharedDevice;
import org.exoplatform.services.cifs.server.core.SharedDeviceList;
import org.exoplatform.services.transaction.TransactionService;

/**
 * Server Session Base Class
 * <p>
 * Base class for server session implementations for different protocols.
 */
public abstract class SrvSession {

  /**
   * Network server this session is associated with.
   */
  private NetworkServer    m_server;

  /**
   * Session id/slot number.
   */
  private int              m_sessId;

  /**
   * Unique session id string.
   */
  private String           m_uniqueId;

  /**
   * Process id.
   */
  private int              m_processId = -1;

  /**
   * Session/user is logged on/validated.
   */
  private boolean          m_loggedOn;

  /**
   * Debug flags for this session.
   */
  private int              m_debug;

  /**
   * Session shutdown flag.
   */
  private boolean          m_shutdown;

  /**
   * Protocol type.
   */
  private String           m_protocol;

  /**
   * Remote client/host name.
   */
  private String           m_remoteName;

  /**
   * Authentication token, used during login.
   */
  private Object           m_authToken;

  /**
   * Authentication context, used during the initial session setup phase.
   */
  private AuthContext      m_authContext;

  /**
   * List of dynamic/temporary shares created for this session.
   */
  private SharedDeviceList m_dynamicShares;

  /**
   * Active transaction.
   */
  private UserTransaction  m_transaction;

  /**
   * Read/write flag.
   */
  private boolean          m_readOnlyTrans;

  /**
   * Request count.
   */
  protected int            m_reqCount;

  /**
   * Transaction count.
   */
  protected int            m_transCount;

  // protected int m_transConvCount;

  /**
   * Class constructor.
   * 
   * @param sessId int
   * @param srv NetworkServer
   * @param proto String
   * @param remName String
   */
  public SrvSession(int sessId, NetworkServer srv, String proto, String remName) {
    m_sessId = sessId;
    m_server = srv;

    setProtocolName(proto);
    setRemoteName(remName);
  }

  /**
   * Add a dynamic share to the list of shares created for this session.
   * 
   * @param shrDev SharedDevice
   */
  public final void addDynamicShare(SharedDevice shrDev) {

    // Check if the dynamic share list must be allocated

    if (m_dynamicShares == null)
      m_dynamicShares = new SharedDeviceList();

    // Add the new share to the list

    m_dynamicShares.addShare(shrDev);
  }

  /**
   * Return the authentication token.
   * 
   * @return Object
   */
  public final Object getAuthenticationToken() {
    return m_authToken;
  }

  /**
   * Determine if the authentication token is set.
   * 
   * @return boolean
   */
  public final boolean hasAuthenticationToken() {
    return m_authToken != null ? true : false;
  }

  /**
   * Return the process id.
   * 
   * @return int
   */
  public final int getProcessId() {
    return m_processId;
  }

  /**
   * Return the remote client network address.
   * 
   * @return InetAddress
   */
  public abstract InetAddress getRemoteAddress();

  /**
   * Return the session id for this session.
   * 
   * @return int
   */
  public final int getSessionId() {
    return m_sessId;
  }

  /**
   * Return the server this session is associated with.
   * 
   * @return NetworkServer
   */
  public final NetworkServer getServer() {
    return m_server;
  }

  /**
   * Check if the session has an authentication context.
   * 
   * @return boolean
   */
  public final boolean hasAuthenticationContext() {
    return m_authContext != null ? true : false;
  }

  /**
   * Return the authentication context for this session.
   * 
   * @return AuthContext
   */
  public final AuthContext getAuthenticationContext() {
    return m_authContext;
  }

  /**
   * Determine if the session has any dynamic shares.
   * 
   * @return boolean
   */
  public final boolean hasDynamicShares() {
    return m_dynamicShares != null ? true : false;
  }

  /**
   * Return the list of dynamic shares created for this session.
   * 
   * @return SharedDeviceList
   */
  public final SharedDeviceList getDynamicShares() {
    return m_dynamicShares;
  }

  /**
   * Determine if the protocol type has been set.
   * 
   * @return boolean
   */
  public final boolean hasProtocolName() {
    return m_protocol != null ? true : false;
  }

  /**
   * Return the protocol name.
   * 
   * @return String
   */
  public final String getProtocolName() {
    return m_protocol;
  }

  /**
   * Determine if the remote client name has been set.
   * 
   * @return boolean
   */
  public final boolean hasRemoteName() {
    return m_remoteName != null ? true : false;
  }

  /**
   * Return the remote client name.
   * 
   * @return String
   */
  public final String getRemoteName() {
    return m_remoteName;
  }

  /**
   * Determine if the session is logged on/validated.
   * 
   * @return boolean
   */
  public final boolean isLoggedOn() {
    return m_loggedOn;
  }

  /**
   * Determine if the session has been shut down.
   * 
   * @return boolean
   */
  public final boolean isShutdown() {
    return m_shutdown;
  }

  /**
   * Return the unique session id.
   * 
   * @return String
   */
  public final String getUniqueId() {
    return m_uniqueId;
  }

  /**
   * Determine if the specified debug flag is enabled.
   * 
   * @param dbgFlag int
   * @return boolean
   */
  public final boolean hasDebug(int dbgFlag) {
    if ((m_debug & dbgFlag) != 0)
      return true;
    return false;
  }

  /**
   * Set the authentication token.
   * 
   * @param authToken Object
   */
  public final void setAuthenticationToken(Object authToken) {
    m_authToken = authToken;
  }

  /**
   * Set the authentication context, used during the initial session setup
   * phase.
   * 
   * @param ctx AuthContext
   */
  public final void setAuthenticationContext(AuthContext ctx) {
    m_authContext = ctx;
  }

  /**
   * Set the debug output interface.
   * 
   * @param flgs int
   */
  public final void setDebug(int flgs) {
    m_debug = flgs;
  }

  /**
   * Set the logged on/validated status for the session.
   * 
   * @param loggedOn boolean
   */
  public final void setLoggedOn(boolean loggedOn) {
    m_loggedOn = loggedOn;
  }

  /**
   * Set the process id.
   * 
   * @param id int
   */
  public final void setProcessId(int id) {
    m_processId = id;
  }

  /**
   * Set the protocol name.
   * 
   * @param name String
   */
  public final void setProtocolName(String name) {
    m_protocol = name;
  }

  /**
   * Set the remote client name.
   * 
   * @param name String
   */
  public final void setRemoteName(String name) {
    m_remoteName = name;
  }

  /**
   * Set the session id for this session.
   * 
   * @param id int
   */
  public final void setSessionId(int id) {
    m_sessId = id;
  }

  /**
   * Set the unique session id.
   * 
   * @param unid String
   */
  public final void setUniqueId(String unid) {
    m_uniqueId = unid;
  }

  /**
   * Set the shutdown flag.
   * 
   * @param flag boolean
   */
  protected final void setShutdown(boolean flag) {
    m_shutdown = flag;
  }

  /**
   * Close the network session.
   */
  public void closeSession() {
    // Release any dynamic shares owned by this session

    if (hasDynamicShares()) {

      // Close the dynamic shares

      m_dynamicShares.removeAllShares();
    }
  }

  /**
   * Create and start a transaction, if not already active.
   * 
   * @param transService TransactionService
   * @param readOnly boolean
   * @return boolean
   */
  private final boolean beginTransaction(TransactionService transService, boolean readOnly) {
    boolean created = false;

    // If there is an active transaction check that it is the required type

    if (m_transaction != null) {
      // Check if the current transaction is marked for rollback

      try {

        if (m_transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK
            || m_transaction.getStatus() == Status.STATUS_ROLLEDBACK
            || m_transaction.getStatus() == Status.STATUS_ROLLING_BACK) {
          // Rollback the current transaction

          m_transaction.rollback();
        }
      } catch (SystemException ex) {
      }

      // Check if the transaction is a write transaction, if write has
      // been requested

      if (!readOnly && m_readOnlyTrans) {
        // Commit the read-only transaction

        try {
          m_transaction.commit();
          // m_transConvCount++;
        } catch (Exception ex) {
          // throw new AlfrescoRuntimeException(
          // "Failed to commit read-only transaction, "
          // + ex.getMessage());
        } finally {
          // Clear the active transaction

          m_transaction = null;
        }
      }
    }

    // Create the transaction

    if (m_transaction == null) {
      try {
        m_transaction.begin();

        created = true;

        m_readOnlyTrans = readOnly;

        m_transCount++;
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    return created;
  }

  /**
   * End a transaction by either committing or rolling back.
   */
  public final void endTransaction() {
    // Check if there is an active transaction

    if (m_transaction != null) {
      try {
        // Commit or rollback the transaction

        if (m_transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
          // Transaction is marked for rollback

          m_transaction.rollback();
        } else {
          // Commit the transaction

          m_transaction.commit();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        // Clear the current transaction

        m_transaction = null;
      }
    }
  }

  /**
   * Determine if the session has an active transaction.
   * 
   * @return boolean
   */
  public final boolean hasUserTransaction() {
    return m_transaction != null ? true : false;
  }

  /**
   * Get the active transaction and clear the stored transaction.
   * 
   * @return UserTransaction
   */
  public final UserTransaction getUserTransaction() {
    UserTransaction trans = m_transaction;
    m_transaction = null;
    return trans;
  }
}

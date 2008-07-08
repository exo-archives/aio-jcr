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

import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.ServerConfiguration;
import org.exoplatform.services.cifs.server.core.SharedDevice;
import org.exoplatform.services.cifs.server.core.SharedDeviceList;
import org.exoplatform.services.log.ExoLogger;

/**
 * Network Server Base Class.
 * <p>
 * Base class for server implementations for different protocols. Class extended
 * by SMBServer and probably NetBIOSNameServer
 */
public abstract class NetworkServer {
  private static final Log    logger   = ExoLogger
                                           .getLogger("org.exoplatform.services.cifs.server");

  /**
   * Server version.
   */
  private String              version;

  /**
   * Server configuration.
   */
  private ServerConfiguration config;

  /**
   * Debug enabled flag.
   */
  private boolean             debug;

  /**
   * Debug flags.
   */
  private int                 debugFlags;

  /**
   * List of addresses that the server is bound to.
   */
  private InetAddress[]       ipAddr;

  /**
   * Server shutdown flag.
   */
  private volatile boolean    shutdown = false;

  /**
   * Server active flag.
   */
  private volatile boolean    active   = false;

  /**
   * Server error exception details.
   */
  private Exception           exception;

  /**
   * Shared devices which server consists opened sessions has own dynamic
   * shares.
   */
  private SharedDeviceList    shares;

  /**
   * Class constructor.
   * 
   * @param config ServerConfiguration
   */
  public NetworkServer(ServerConfiguration config) {
    this.config = config;
    shares = new SharedDeviceList();
  }

  /**
   * Returns the server configuration.
   * 
   * @return ServerConfiguration
   */
  public final ServerConfiguration getConfiguration() {
    return config;
  }

  /**
   * Return the main server name.
   * 
   * @return String
   */
  public final String getServerName() {
    return config.getServerName();
  }

  /**
   * Return the list of IP addresses that the server is bound to.
   * 
   * @return java.net.InetAddress[]
   */
  public final InetAddress[] getServerAddresses() {
    return ipAddr;
  }

  /**
   * Return SharedDeviceList which server consist.
   * 
   * @return SharedDeviceList
   */
  public SharedDeviceList getShares() {
    return shares;
  }

  /**
   * Return the list of available shares for opened session.
   * 
   * @param sess SrvSession
   * @return SharedDeviceList
   */
  public SharedDeviceList getAllShares(SrvSession sess) {

    // TODO Check if the session is valid, if so then check if the session has
    // any dynamic shares

    SharedDeviceList shrList = new SharedDeviceList(getShares());

    if (sess != null && sess.hasDynamicShares()) {

      // Add the per session dynamic shares

      shrList.addShares(sess.getDynamicShares());
    }

    return shrList;
  }

  /**
   * Find the shared device with the specified name.
   * 
   * @param name Name of the shared device to find.
   * @param typ Shared device type
   * @param sess Session details
   * @return SharedDevice with the specified name and type, else null.
   * @throws Exception
   */
  public final SharedDevice findShare(String name, int typ, SrvSession sess) throws Exception {

    SharedDevice share = null;

    // Search the sessions dynamic share list first

    if (sess.hasDynamicShares()) {

      // Check if the required share exists in the sessions dynamic share
      // list

      share = sess.getDynamicShares().findShare(name, typ, true);
    }

    // If we did not find a share then search the global share list

    if (share == null) {
      // Find the required share by name/type. Use a case sensitive search
      // first, if that fails
      // use a case insensitive search.

      share = getShares().findShare(name, typ, false);

      if (share == null) {

        // Try a case insensitive search for the required share

        share = getShares().findShare(name, typ, true);
      }
    }

    // TODO Check if the share is available
    // Return the shared device, or null if no matching device was found

    return share;
  }

  /**
   * Determine if the SMB server is active.
   * 
   * @return boolean
   */
  public final boolean isActive() {
    return active;
  }

  /**
   * Return the server version string, in 'n.n.n' format.
   * 
   * @return String
   */

  public final String isVersion() {
    return version;
  }

  /**
   * Check if there is a stored server exception.
   * 
   * @return boolean
   */
  public final boolean hasException() {
    return exception != null ? true : false;
  }

  /**
   * Return the stored exception.
   * 
   * @return Exception
   */
  public final Exception getException() {
    return exception;
  }

  /**
   * Clear the stored server exception.
   */
  public final void clearException() {
    exception = null;
  }

  /**
   * Determine if debug output is enabled.
   * 
   * @return boolean
   */
  public final boolean hasDebug() {
    return debug;
  }

  /**
   * Determine if the specified debug flag is enabled.
   * 
   * @param flag debug flag
   * @return boolean
   */
  public final boolean hasDebugFlag(int flag) {
    return (debugFlags & flag) != 0 ? true : false;
  }

  /**
   * Check if the shutdown flag is set.
   * 
   * @return boolean
   */
  public final boolean hasShutdown() {
    return shutdown;
  }

  /**
   * Set/clear the server active flag.
   * 
   * @param active boolean
   */
  protected void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Set the stored server exception.
   * 
   * @param ex Exception
   */
  protected final void setException(Exception ex) {
    exception = ex;
  }

  /**
   * Set the addresses that the server is bound to.
   * 
   * @param addrs InetAddress[]
   */
  protected final void setServerAddresses(InetAddress[] addrs) {
    ipAddr = addrs;
  }

  /**
   * Set the server version.
   * 
   * @param ver String
   */
  protected final void setVersion(String ver) {
    version = ver;
  }

  /**
   * Enable/disable debug output for the server.
   * 
   * @param dbg boolean
   */
  protected final void setDebug(boolean dbg) {
    debug = dbg;
  }

  /**
   * Set the debug flags.
   * 
   * @param flags int
   */
  protected final void setDebugFlags(int flags) {
    debugFlags = flags;
    setDebug(flags == 0 ? false : true);
  }

  /**
   * Set/clear the shutdown flag.
   * 
   * @param value boolean
   */
  protected final void setShutdown(final boolean value) {
    shutdown = value;
  }

  /**
   * Start the network server.
   */
  public abstract void startServer();

  /**
   * Shutdown the network server.
   * 
   * @param immediate boolean
   */
  public abstract void shutdownServer(boolean immediate);
}

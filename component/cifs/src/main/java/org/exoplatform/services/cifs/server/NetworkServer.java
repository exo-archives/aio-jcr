package org.exoplatform.services.cifs.server;

import java.net.InetAddress;

import org.exoplatform.services.cifs.ServerConfiguration;
import org.exoplatform.services.cifs.server.core.SharedDevice;
import org.exoplatform.services.cifs.server.core.SharedDeviceList;
import org.exoplatform.services.log.ExoLogger; 

import org.apache.commons.logging.Log;

/**
 * Network Server Base Class
 * <p>
 * Base class for server implementations for different protocols. Class extended
 * by SMBServer and probably NetBIOSNameServer
 */
public abstract class NetworkServer {
  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs.server");

  // Protocol name

  private String m_protoName;

  // Server version

  private String m_version;

  // Server configuration
  private ServerConfiguration m_config;

  // Debug enabled flag and debug flags

  private boolean m_debug;

  private int m_debugFlags;

  // List of addresses that the server is bound to

  private InetAddress[] m_ipAddr;

  // Server shutdown flag and server active flag

  private boolean m_shutdown = false;

  private boolean m_active = false;

  // Server error exception details

  private Exception m_exception;

  /**
   * Shared devices which server consists opened sessions has own dynamic shares
   */
  private SharedDeviceList m_shares;

  // Server events listener
  // Session listener list

  /**
   * Class constructor
   * 
   * @param proto
   *          String
   * @param config
   *          ServerConfiguration
   */
  public NetworkServer(String proto, ServerConfiguration config) {
    m_protoName = proto;
    m_config = config;
    m_shares = new SharedDeviceList();
  }

  /**
   * Returns the server configuration.
   * 
   * @return ServerConfiguration
   */
  public final ServerConfiguration getConfiguration() {
    return m_config;
  }

  /**
   * Return the main server name
   * 
   * @return String
   */
  public final String getServerName() {
    return m_config.getServerName();
  }

  /**
   * Return the list of IP addresses that the server is bound to.
   * 
   * @return java.net.InetAddress[]
   */
  public final InetAddress[] getServerAddresses() {
    return m_ipAddr;
  }

  /**
   * Determine if the SMB server is active.
   * 
   * @return boolean
   */
  public final boolean isActive() {
    return m_active;
  }

  /**
   * Return the server version string, in 'n.n.n' format
   * 
   * @return String
   */
  public final String isVersion() {
    return m_version;
  }

  /**
   * Check if there is a stored server exception
   * 
   * @return boolean
   */
  public final boolean hasException() {
    return m_exception != null ? true : false;
  }

  /**
   * Return the stored exception
   * 
   * @return Exception
   */
  public final Exception getException() {
    return m_exception;
  }

  /**
   * Clear the stored server exception
   */
  public final void clearException() {
    m_exception = null;
  }

  /**
   * Return the server protocol name
   * 
   * @return String
   */
  public final String getProtocolName() {
    return m_protoName;
  }

  /**
   * Determine if debug output is enabled
   * 
   * @return boolean
   */
  public final boolean hasDebug() {
    return m_debug;
  }

  /**
   * Determine if the specified debug flag is enabled
   * 
   * @return boolean
   */
  public final boolean hasDebugFlag(int flg) {
    return (m_debugFlags & flg) != 0 ? true : false;
  }

  /**
   * Check if the shutdown flag is set
   * 
   * @return boolean
   */
  public final boolean hasShutdown() {
    return m_shutdown;
  }

  /**
   * Set/clear the server active flag
   * 
   * @param active
   *          boolean
   */
  protected void setActive(boolean active) {
    m_active = active;
  }

  /**
   * Set the stored server exception
   * 
   * @param ex
   *          Exception
   */
  protected final void setException(Exception ex) {
    m_exception = ex;
  }

  /**
   * Set the addresses that the server is bound to
   * 
   * @param adds
   *          InetAddress[]
   */
  protected final void setServerAddresses(InetAddress[] addrs) {
    m_ipAddr = addrs;
  }

  /**
   * Set the server version
   * 
   * @param ver
   *          String
   */
  protected final void setVersion(String ver) {
    m_version = ver;
  }

  /**
   * Enable/disable debug output for the server
   * 
   * @param dbg
   *          boolean
   */
  protected final void setDebug(boolean dbg) {
    m_debug = dbg;
  }

  /**
   * Set the debug flags
   * 
   * @param flags
   *          int
   */
  protected final void setDebugFlags(int flags) {
    m_debugFlags = flags;
    setDebug(flags == 0 ? false : true);
  }

  /**
   * Set/clear the shutdown flag
   * 
   * @param ena
   *          boolean
   */
  protected final void setShutdown(boolean ena) {
    m_shutdown = ena;
  }

  /**
   * Start the network server
   */
  public abstract void startServer();

  /**
   * Shutdown the network server
   * 
   * @param immediate
   *          boolean
   */
  public abstract void shutdownServer(boolean immediate);

  /**
   * Return SharedDeviceList which server consist
   */
  public SharedDeviceList getShares() {
    return m_shares;
  }

  /**
   * Find the shared device with the specified name.
   * 
   * @param host
   *          Host name from the UNC path
   * @param name
   *          Name of the shared device to find.
   * @param typ
   *          Shared device type
   * @param sess
   *          Session details
   * @param create
   *          Create share flag, false indicates lookup only
   * @return SharedDevice with the specified name and type, else null.
   * @exception Exception
   */
  public final SharedDevice findShare(String name, int typ, SrvSession sess,
      boolean create) throws Exception {

    SharedDevice share = null;

    // Search the sessions dynamic share list first

    if (sess.hasDynamicShares()) {

      // Check if the required share exists in the sessions dynamic share
      // list

      share = sess.getShares().findShare(name, typ, true);
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

    // Check if the share is available
    // TODO how to check is workspace is available?
    // Return the shared device, or null if no matching device was found

    return share;
  }

  /**
   * Return the list of available shares for opened session.
   * 
   * @param sess
   *          SrvSession
   * @return SharedDeviceList
   */
  public SharedDeviceList getAllShares(SrvSession sess) {

    // Check if the session is valid, if so then check if the session has
    // any dynamic shares

    // Make a copy of the global share list and add the per session dynamic
    // shares

    SharedDeviceList shrList = new SharedDeviceList(getShares());

    if (sess != null && sess.hasDynamicShares()) {

      // Add the per session dynamic shares

      shrList.addShares(sess.getShares());
    }

    return shrList;
  }

}

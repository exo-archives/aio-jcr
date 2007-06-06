package org.exoplatform.services.cifs.smb.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;
import java.util.Vector;

import org.exoplatform.services.cifs.ServerConfiguration;
import org.exoplatform.services.cifs.netbios.NetworkSettings;
import org.exoplatform.services.cifs.server.NetworkServer;
import org.exoplatform.services.cifs.server.SrvSession;
import org.exoplatform.services.cifs.server.SrvSessionList;
import org.exoplatform.services.cifs.server.core.SharedDevice;
import org.exoplatform.services.cifs.server.core.SharedDeviceList;
import org.exoplatform.services.cifs.smb.SMBException;
import org.exoplatform.services.cifs.smb.ServerType;
import org.exoplatform.services.cifs.smb.ShareType;
import org.exoplatform.services.cifs.smb.mailslot.HostAnnouncer;
import org.exoplatform.services.cifs.smb.server.win32.Win32NetBIOSLanaMonitor;
import org.exoplatform.services.cifs.smb.server.win32.Win32NetBIOSSessionSocketHandler;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import javax.jcr.Repository;
import org.exoplatform.services.log.ExoLogger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * SMB Server Class
 * 
 * <p>
 * Creates an SMB server with the specified host name.
 * 
 * <p>
 * The server can optionally announce itself so that it will appear under the
 * Network Neighborhood, by enabling the host announcer in the server
 * configuration or using the enableAnnouncer() method.
 */

public class SMBServer extends NetworkServer implements Runnable {

  // Debug logging

  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.CIFS.smb.server.SMBServerS");

  // Server version

  private static final String ServerVersion = "1.0.1";

  // Server thread

  private Thread m_srvThread;

  // Session socket handlers (NetBIOS over TCP/IP, native SMB and/or Win32
  // NetBIOS)

  private Vector<SessionSocketHandler> m_sessionHandlers;

  // Host announcers, server will appear under Network Neighborhood

  private Vector<HostAnnouncer> m_hostAnnouncers;

  // Active session list

  private SrvSessionList m_sessions;

  // Server type flags, used when announcing the host

  private int m_srvType = ServerType.WorkStation + ServerType.Server
      + ServerType.NTServer;

  // Server GUID

  private UUID m_serverGUID;

  private RepositoryService m_repositoryService;

  /**
   * Create an SMB server using the specified configuration.
   * 
   * @param serviceRegistry
   *          repository connection
   * @param cfg
   *          ServerConfiguration
   */
  public SMBServer(ServerConfiguration cfg) throws IOException {
    super("SMB", cfg);

    // Call the common constructor
    CommonConstructor();
  }

  public SMBServer(ServerConfiguration config,
      RepositoryService repositoryService) throws IOException {

    super("SMB", config);
    m_repositoryService = repositoryService;
    CommonConstructor();
  }

  public ManageableRepository getRepository() {
    try {
      return m_repositoryService.getRepository();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Add a session handler
   * 
   * @param sessHandler
   *          SessionSocketHandler
   */
  public final void addSessionHandler(SessionSocketHandler handler) {

    // Check if the session handler list has been allocated

    if (m_sessionHandlers == null)
      m_sessionHandlers = new Vector<SessionSocketHandler>();

    // Add the session handler

    m_sessionHandlers.addElement(handler);
  }

  /**
   * Add a host announcer
   * 
   * @param announcer
   *          HostAnnouncer
   */
  public final void addHostAnnouncer(HostAnnouncer announcer) {

    // Check if the host announcer list has been allocated

    if (m_hostAnnouncers == null)
      m_hostAnnouncers = new Vector<HostAnnouncer>();

    // Add the host announcer

    m_hostAnnouncers.addElement(announcer);
  }

  /**
   * Add a new session to the server
   * 
   * @param sess
   *          SMBSrvSession
   */
  public final void addSession(SMBSrvSession sess) {

    // Add the session to the session list

    m_sessions.addSession(sess);

    // Propagate the debug settings to the new session

    sess.setDebug(getConfiguration().getSessionDebugFlags());
  }

  /**
   * Common constructor code.
   */
  private void CommonConstructor() throws IOException {

    // Set the server version

    setVersion(ServerVersion);

    // Create the session socket handler list

    m_sessionHandlers = new Vector<SessionSocketHandler>();

    // Create the active session list

    m_sessions = new SrvSessionList();

    // Set the global domain name

    NetworkSettings.setDomain(getConfiguration().getDomainName());
    NetworkSettings.setBroadcastMask(getConfiguration().getBroadcastMask());
  }

  /**
   * Close the host announcer, if enabled
   */
  protected void closeHostAnnouncers() {

    // Check if there are active host announcers

    if (m_hostAnnouncers != null) {

      // Shutdown the host announcers

      for (int i = 0; i < m_hostAnnouncers.size(); i++) {

        // Get the current host announcer from the active list

        HostAnnouncer announcer = (HostAnnouncer) m_hostAnnouncers.elementAt(i);

        // Shutdown the host announcer

        announcer.shutdownAnnouncer();
      }
    }
  }

  /**
   * Close the session handlers
   */
  protected void closeSessionHandlers() {

    // Close the session handlers

    for (SessionSocketHandler handler : m_sessionHandlers) {

      // Request the handler to shutdown

      handler.shutdownRequest();
    }

    // Clear the session handler list

    m_sessionHandlers.removeAllElements();
  }

  /**
   * Return the server comment.
   * 
   * @return java.lang.String
   */
  public final String getComment() {
    return getConfiguration().getComment();
  }

  /**
   * Return the server type flags.
   * 
   * @return int
   */
  public final int getServerType() {
    return m_srvType;
  }

  /**
   * Return the per session debug flag settings.
   */
  public final int getSessionDebug() {
    return getConfiguration().getSessionDebugFlags();
  }

  /**
   * Return the active session list
   * 
   * @return SrvSessionList
   */
  public final SrvSessionList getSessions() {
    return m_sessions;
  }

  /**
   * Start the SMB server.
   */
  public void run() {

    // Indicate that the server is active

    setActive(true);

    // Check if we are running under Windows

    boolean isWindows = isWindowsNTOnwards();

    // Generate a GUID for the server based on the server name

    m_serverGUID = UUID.nameUUIDFromBytes(getServerName().getBytes());

    // Debug

    if (logger.isInfoEnabled()) {

      // Dump the server name and GUID

      logger.info("SMB Server " + getServerName() + " starting");
      logger.info("GUID " + m_serverGUID);

      // Output the authenticator details

      // Display the timezone offset/name

      if (getConfiguration().getTimeZone() != null)
        logger.info("Server timezone " + getConfiguration().getTimeZone()
            + ", offset from UTC = " + getConfiguration().getTimeZoneOffset()
            / 60 + "hrs");
      else
        logger.info("Server timezone offset = "
            + getConfiguration().getTimeZoneOffset() / 60 + "hrs");

      // Dump the share list
    }

    // Create a server socket to listen for incoming session requests

    try {

      // Add the IPC$ named pipe shared device
      SharedDevice adminpipe = new SharedDevice("IPC$", ShareType.ADMINPIPE);
      // Set the device attributes
      adminpipe.setAttributes(SharedDevice.Admin + SharedDevice.Hidden);

      getShares().addShare(adminpipe);

      // Clear the server shutdown flag

      setShutdown(false);

      // Get the list of IP addresses the server is bound to

      getServerIPAddresses();

      // Check if the socket connection debug flag is enabled

      boolean sockDbg = false;

      if ((getSessionDebug() & SMBSrvSession.DBG_SOCKET) != 0)
        sockDbg = true;

      // Create the NetBIOS session socket handler, if enabled

      if (getConfiguration().hasNetBIOSSMB()) {

        // Create the TCP/IP NetBIOS SMB/CIFS session handler(s), and
        // host announcer(s) if
        // enabled

        // NetBIOSSessionSocketHandler.createSessionHandlers(this, sockDbg);
      }

      // Create the TCP/IP SMB session socket handler, if enabled

      if (getConfiguration().hasTcpipSMB()) {

        // Create the TCP/IP native SMB session handler(s)

        // TcpipSMBSessionSocketHandler.createSessionHandlers(this, sockDbg);
      }

      // Create the Win32 NetBIOS session handler, if enabled

      if (getConfiguration().hasWin32NetBIOS()) {

        // Only enable if running under Windows

        if (isWindows == true) {

          // Create the Win32 NetBIOS SMB handler(s), and host
          // announcer(s) if enabled

          Win32NetBIOSSessionSocketHandler.createSessionHandlers(this, sockDbg);
        }
      }

      // Check if there are any session handlers installed, if not then
      // close the server

      if (m_sessionHandlers.size() > 0 || getConfiguration().hasWin32NetBIOS()) {

        // Wait for incoming connection requests

        while (hasShutdown() == false) {

          // Sleep for a while

          try {
            Thread.sleep(1000L);
          } catch (InterruptedException ex) {
          }
        }
      } else if (logger.isInfoEnabled()) {

        // DEBUG

        logger.info("No valid session handlers, server closing");
      }
    } catch (Exception ex) {

      // Do not report an error if the server has shutdown, closing the
      // server socket
      // causes an exception to be thrown.

      if (hasShutdown() == false) {
        logger.error("Server error : ", ex);

        // Store the error, fire a server error event

        setException(ex);
      }
    }

    // Debug

    if (logger.isInfoEnabled())
      logger.info("SMB Server shutting down ...");

    // Close the host announcer and session handlers

    closeHostAnnouncers();
    closeSessionHandlers();

    // Shutdown the Win32 NetBIOS LANA monitor, if enabled

    if (isWindows && Win32NetBIOSLanaMonitor.getLanaMonitor() != null)
      Win32NetBIOSLanaMonitor.getLanaMonitor().shutdownRequest();

    // Indicate that the server is not active

    setActive(false);

  }

  /**
   * Notify the server that a session has been closed.
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected final void sessionClosed(SMBSrvSession sess) {

    // Remove the session from the active session list

    m_sessions.removeSession(sess);

  }

  /**
   * Notify the server that a user has logged on. /emty/
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected final void sessionLoggedOn(SMBSrvSession sess) {

  }

  /**
   * Notify the server that a session has been closed. /empty/
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected final void sessionOpened(SMBSrvSession sess) {

  }

  /**
   * Shutdown the SMB server
   * 
   * @param immediate
   *          boolean
   */
  public final void shutdownServer(boolean immediate) {

    // Indicate that the server is closing

    setShutdown(true);

    try {

      // Close the session handlers

      closeSessionHandlers();
    } catch (Exception ex) {
    }

    // Close the active sessions

    Enumeration<Integer> enm = m_sessions.enumerate();

    while (enm.hasMoreElements()) {

      // Get the session id and associated session

      Integer sessId = enm.nextElement();
      SMBSrvSession sess = (SMBSrvSession) m_sessions.findSession(sessId);

      // Close the session

      sess.closeSession();
    }

    // Wait for the main server thread to close

    if (m_srvThread != null) {

      try {
        m_srvThread.join(3000);
      } catch (Exception ex) {
      }
    }
  }

  /**
   * Start the SMB server in a seperate thread
   */
  public void startServer() {

    // Create a seperate thread to run the SMB server

    m_srvThread = new Thread(this);
    m_srvThread.setName("SMB Server");
    m_srvThread.setDaemon(true);

    m_srvThread.start();
  }

  /**
   * Determine if we are running under Windows NT onwards
   * 
   * @return boolean
   */
  private final boolean isWindowsNTOnwards() {

    // Get the operating system name property

    String osName = System.getProperty("os.name");

    if (osName.startsWith("Windows")) {
      if (osName.endsWith("95") || osName.endsWith("98")
          || osName.endsWith("ME")) {

        // Windows 95-ME

        return false;
      }

      // Looks like Windows NT onwards

      return true;
    }

    // Not Windows

    return false;
  }

  /**
   * Get the list of local IP addresses
   */
  private final void getServerIPAddresses() {

    try {

      // Get the local IP address list

      Enumeration<NetworkInterface> enm = NetworkInterface
          .getNetworkInterfaces();
      Vector<InetAddress> addrList = new Vector<InetAddress>();

      while (enm.hasMoreElements()) {

        // Get the current network interface

        NetworkInterface ni = enm.nextElement();

        // Get the address list for the current interface

        Enumeration<InetAddress> addrs = ni.getInetAddresses();

        while (addrs.hasMoreElements())
          addrList.add(addrs.nextElement());
      }

      // Convert the vector of addresses to an array

      if (addrList.size() > 0) {

        // Convert the address vector to an array

        InetAddress[] inetAddrs = new InetAddress[addrList.size()];

        // Copy the address details to the array

        for (int i = 0; i < addrList.size(); i++)
          inetAddrs[i] = (InetAddress) addrList.elementAt(i);

        // Set the server IP address list

        setServerAddresses(inetAddrs);
      }
    } catch (Exception ex) {

      // DEBUG

      logger.error("Error getting local IP addresses", ex);
    }
  }

  /**
   * Return the server GUID
   * 
   * @return UUID
   */
  public final UUID getServerGUID() {
    return m_serverGUID;
  }
}
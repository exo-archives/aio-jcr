/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cifs.netbios.NetBIOSName;
import org.exoplatform.services.cifs.netbios.NetBIOSNameList;
import org.exoplatform.services.cifs.netbios.NetBIOSSession;
import org.exoplatform.services.cifs.netbios.RFCNetBIOSProtocol;
import org.exoplatform.services.cifs.netbios.win32.Win32NetBIOS;
import org.exoplatform.services.cifs.smb.Dialect;
import org.exoplatform.services.cifs.smb.DialectSelector;
import org.exoplatform.services.cifs.smb.ServerType;
import org.exoplatform.services.cifs.smb.TcpipSMB;
import org.exoplatform.services.cifs.smb.server.SecurityMode;
import org.exoplatform.services.cifs.util.IPAddress;
import org.exoplatform.services.cifs.util.X64;
import org.exoplatform.services.log.ExoLogger;

/**
 * Service (server) configuration class
 */
public class ServerConfiguration {
  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs");

  private static final String m_sessDbgStr[] = { "NETBIOS", "STATE",
      "NEGOTIATE", "TREE", "SEARCH", "INFO", "FILE", "FILEIO", "TRANSACT",
      "ECHO", "ERROR", "IPC", "LOCK", "PKTTYPE", "DCERPC", "STATECACHE",
      "NOTIFY", "STREAMS", "SOCKET" };

  // Platform types

  public enum PlatformType {
    Unknown, WINDOWS, LINUX, SOLARIS, MACOSX
  };

  // Runtime platform type

  private PlatformType m_platform;

  // Main server enable flags, to enable SMB and/or NFS server components

  private boolean m_smbEnable;

  // Server name

  private String m_name;

  // Server type, used by the host announcer

  private int m_srvType = ServerType.WorkStation + ServerType.Server;// +

  // ServerType.NTServer;

  // Server comment

  private String m_comment;

  // Server domain

  private String m_domain;

  // Network broadcast mask string

  private String m_broadcast;

  // NetBIOS ports

  private int m_nbNamePort = RFCNetBIOSProtocol.NAME_PORT;

  private int m_nbSessPort = RFCNetBIOSProtocol.PORT;

  private int m_nbDatagramPort = RFCNetBIOSProtocol.DATAGRAM;

  // Native SMB port

  private int m_tcpSMBPort = TcpipSMB.PORT;

  // Announce the server to network neighborhood, announcement interval in
  // minutes used in NetBIOS (java and JNI)

  private boolean m_announce = false;

  private int m_announceInterval = 5; // default 5

  // Default session debugging setting

  private int m_sessDebug;

  // Flags to indicate if NetBIOS, native TCP/IP SMB and/or Win32 NetBIOS
  // should be enabled

  private boolean m_netBIOSEnable = true;

  private boolean m_tcpSMBEnable = true;

  private boolean m_win32NBEnable = true;

  // Address to bind the SMB server to, if null all local addresses are used

  private InetAddress m_smbBindAddress = null;

  // Address to bind the NetBIOS name server to, if null all addresses are
  // used

  // private InetAddress m_nbBindAddress=null;

  // WINS servers

  private InetAddress m_winsPrimary;

  private InetAddress m_winsSecondary;

  // Enable/disable Macintosh extension SMBs

  private boolean m_macExtensions = false;

  // --------------------------------------------------------------------------------
  // Win32 NetBIOS configuration
  //
  // Server name to register under Win32 NetBIOS, if not set the main server
  // name is used

  private String m_win32NBName;

  // LANA to be used for Win32 NetBIOS, if not specified the first available
  // is used

  private int m_win32NBLANA = -1;

  // Send out host announcements via the Win32 NetBIOS interface

  private boolean m_win32NBAnnounce = false;

  private int m_win32NBAnnounceInterval = 5;

  // Use Winsock NetBIOS interface if true, else use the Netbios() API
  // interface

  private boolean m_win32NBUseWinsock;

  // --------------------------------------------------------------------------------

  // Global server configuration
  //
  // Timezone name and offset from UTC in minutes

  private String m_timeZone;

  private int m_tzOffset;

  // Local server name and domain/workgroup name

  private String m_localName;

  private String m_localDomain;

  private String[] workspaceList;

  private String repoName;

  private boolean fromJndi = true;

  private int securityMode;

  /*
   * Configuration by XML-config
   */
  public ServerConfiguration(InitParams params) {

    // Enable server
    ValueParam pEnable = params.getValueParam("enable_smb");
    if (pEnable != null) {
      if (pEnable.getValue().equalsIgnoreCase("true")) {
        setSMBServerEnabled(true);
      } else if (pEnable.getValue().equalsIgnoreCase("false")) {
        setSMBServerEnabled(false);
        return;
      } else {
        logger.error("Illegal value of parameter enable_smb");
        setSMBServerEnabled(false);
        return;
      }
    } else {
      logger.error("Server is not Enabled!");
      setSMBServerEnabled(false);
      return;
    }

    determinePlatformType();

    // set broadcast mask, before transport is specified

    ValueParam pmask = params.getValueParam("broadcast_mask");
    if (pmask != null) {
      // check is mask is valid
      if (IPAddress.isNumericAddress(pmask.getValue()) == false) {
        logger.error("Invalid broadcast mask, must be n.n.n.n format!");
        setBroadcastMask("255.255.255.0");
      } else {
        setBroadcastMask(pmask.getValue());
      }
    } else {
      setBroadcastMask("255.255.255.0");
    }

    // Set the CIFS server name
    String hostName;

    ValueParam pHostName = params.getValueParam("host_name");
    if (pHostName != null) {
      hostName = pHostName.getValue();
    } else {
      logger.error("Server name is not assigned!!");
      this.setSMBServerEnabled(false);
      return;
    }

    if (hostName.length() > 15) {
      // Truncate the CIFS server name
      hostName = hostName.substring(0, 15);

      // Output a warning
      logger
          .warn("CIFS server name is longer than 15 characters, truncated to " +
              hostName);
    }

    setServerName(hostName);

    // Set the domain/workgroup name

    ValueParam pDomainName = params.getValueParam("workgroup");
    if (pDomainName != null) {
      m_localDomain = pDomainName.getValue();
    }

    setDomainName(getLocalDomainName().toUpperCase());

    // Check for a server comment
    ValueParam pComment = params.getValueParam("comment");
    if (pComment != null) {
      setComment(pComment.getValue());
    } else
      setComment("Default server coment");

    // Check the bind address

    ValueParam pBindAddress = params.getValueParam("bind_address");

    if (pBindAddress != null) {
      try {
        InetAddress bindAddr = InetAddress.getByName(pBindAddress.getValue());

        // Set the bind address for the server
        setSMBBindAddress(bindAddr);
      } catch (UnknownHostException e) {

        setSMBBindAddress(null);
      }
    }

    // Configure NetBIOS SMB (Java Impl)

    PropertiesParam netBiosJava = params.getPropertiesParam("netbios_java");
    if (netBiosJava != null &&
        netBiosJava.getProperty("enabled").equals("true")) {
      setNetBIOSSMB(true);

      if (netBiosJava.getProperty("session_port") != null) {
        int sessport = Integer
            .parseInt(netBiosJava.getProperty("session_port"));
        if (sessport < 0 || sessport > 65535) {
          logger
              .error("Illegal value of netbios_java session_port partameter!");
          setNetBIOSSMB(false);
        } else {
          m_nbSessPort = sessport;
        }
      }

      if (netBiosJava.getProperty("datagram_port") != null) {
        int dtgport = Integer
            .parseInt(netBiosJava.getProperty("datagram_port"));
        if (dtgport < 0 || dtgport > 65535) {
          logger
              .error("Illegal value of netbios_java datagram_port partameter!");
          setNetBIOSSMB(false);
        } else {
          m_nbDatagramPort = dtgport;
        }
      }

      if (netBiosJava.getProperty("name_port") != null) {
        int nmport = Integer.parseInt(netBiosJava.getProperty("name_port"));
        if (nmport < 0 || nmport > 65535) {
          logger
              .error("Illegal value of netbios_java datagram_port partameter!");
          setNetBIOSSMB(false);
        } else {
          m_nbNamePort = nmport;
        }
      }

      // setup NetBIOS host announcer
      if (netBiosJava.getProperty("announce_enabled") != null &&
          netBiosJava.getProperty("announce_enabled").equalsIgnoreCase("true")) {
        setHostAnnounce(true);

        if (netBiosJava.getProperty("announce_interval") != null) {
          int interval = Integer.parseInt(netBiosJava
              .getProperty("announce_interval"));
          if (interval > 0) {
            setHostAnnounceInterval(interval);
          } else {
            logger
                .error("Illegal value of netbios_java host announcer interval partameter!");
          }
        } else {
          // use default value
          setHostAnnounceInterval(5);
        }
      }

    } else {
      setNetBIOSSMB(false);
    }

    // Configure TCP/IP SMB
/*
 * PropertiesParam tcpSmb = params.getPropertiesParam("tcpip"); if (tcpSmb !=
 * null && tcpSmb.getProperty("enabled").equals("true")) { setTcpipSMB(true); if
 * (tcpSmb.getProperty("port") != null) { int sessport =
 * Integer.parseInt(tcpSmb.getProperty("port")); if (sessport < 0 || sessport >
 * 65535) { logger.error("Illegal value of tcpip port partameter!");
 * setTcpipSMB(false); } else { m_tcpSMBPort = sessport; } } } else {
 * setTcpipSMB(false); }
 */

    // TODO Tcpip transport not used for now
    setTcpipSMB(false);

    // Configure win32 NetBIOS (netbios or wins)

    PropertiesParam winnbt = params.getPropertiesParam("winnetbios");

    if (winnbt != null && winnbt.getProperty("enabled").equals("true")) {

      // Check if the Win32 NetBIOS server name has been specified

      String win32Name = winnbt.getProperty("netbiosname");
      if (win32Name != null && win32Name.length() > 0) {

        // Validate the name

        if (win32Name.length() > 16) {
          logger.error("Invalid Win32 NetBIOS name, " + win32Name);
        } else {
          // Set the Win32 NetBIOS file server name

          setWin32NetBIOSName(win32Name);
        }
      }

      // Check if the Win32 NetBIOS LANA has been specified

      String lanaStr = winnbt.getProperty("lanas");
      if (lanaStr != null && lanaStr.length() > 0) {
        // Check if the LANA has been specified as an IP address or adapter name

        setWin32LANA(new Integer(lanaStr));
      } else {
        setWin32LANA(-1); // all lanas is available;
      }
      // Check if the native NetBIOS interface has been specified, either
      // 'winsock' or 'netbios'

      String nativeAPI = winnbt.getProperty("nativeapi");
      if (nativeAPI != null && nativeAPI.length() > 0) {
        // Validate the API type

        boolean useWinsock = true;

        if (nativeAPI.equalsIgnoreCase("netbios"))
          useWinsock = false;
        else if (nativeAPI.equalsIgnoreCase("winsock") == false) {
          logger
              .error("Invalid NetBIOS API type, spefify 'winsock' or 'netbios'");
          setSMBServerEnabled(false);
          return;
        }
        // Set the NetBIOS API to use

        setWin32WinsockNetBIOS(useWinsock);
      }

      // Force the older NetBIOS API code to be used on 64Bit Windows

      if (useWinsockNetBIOS() == true && X64.isWindows64()) {
        // Log a warning

        logger.warn("Using older Netbios() API code");

        // Use the older NetBIOS API code

        setWin32WinsockNetBIOS(false);
      }

      // Check if the current operating system is supported by the Win32
      // NetBIOS handler

      String osName = System.getProperty("os.name");
      if (osName.startsWith("Windows") &&
          (osName.endsWith("95") == false && osName.endsWith("98") == false && osName
              .endsWith("ME") == false)) {

        // Call the Win32NetBIOS native code to make sure it is initialized

        if (Win32NetBIOS.LanaEnumerate() != null) {
          // Enable Win32 NetBIOS

          setWin32NetBIOS(true);
        } else {
          logger.warn("No NetBIOS LANAs available");
        }
      } else {

        // Win32 NetBIOS not supported on the current operating system

        setWin32NetBIOS(false);
      }

      // setup Win32 NetBIOS host announcer
      if (winnbt.getProperty("announce_enabled") != null &&
          winnbt.getProperty("announce_enabled").equalsIgnoreCase("true")) {
        setWin32HostAnnouncer(true);

        if (winnbt.getProperty("announce_interval") != null) {
          int interval = Integer.parseInt(winnbt
              .getProperty("announce_interval"));
          if (interval > 0) {
            setWin32HostAnnounceInterval(interval);
          } else {
            logger
                .error("Illegal value of netbios_java host announcer interval partameter!");
            setWin32HostAnnouncer(false);
          }
        } else {
          // use default value
          setWin32HostAnnounceInterval(5);
        }
      } else {
        setWin32HostAnnouncer(false);
      }

    } else {

      // Disable Win32 NetBIOS

      setWin32NetBIOS(false);
    }
    // Check if NetBIOS and/or TCP/IP SMB have been enabled

    if (hasNetBIOSSMB() == false && hasTcpipSMB() == false &&
        hasWin32NetBIOS() == false) {
      setSMBServerEnabled(false);
    } else {
      setSMBServerEnabled(true);
    }

    String flags = "NETBIOS,Negotiate,Socket,Tree,PktType,StateCache,State,Search,Info,File,FileIO,Echo,Error,Notify,IPC,DCERPC,STREAMS";// elem.getAttribute("flags");
    int sessDbg = 0;

    if (flags != null) {

      // Parse the flags

      flags = flags.toUpperCase();
      StringTokenizer token = new StringTokenizer(flags, ",");

      while (token.hasMoreTokens()) {

        // Get the current debug flag token

        String dbg = token.nextToken().trim();

        // Find the debug flag name

        int idx = 0;

        while (idx < m_sessDbgStr.length &&
            m_sessDbgStr[idx].equalsIgnoreCase(dbg) == false)
          idx++;
        // Set the debug flag

        sessDbg += 1 << idx;
      }
    }

    // Set the session debug flags

    setSessionDebugFlags(sessDbg);

    // repo params

    ValueParam repoParam;
    repoParam = params.getValueParam("jndi-repository-name");

    if (repoParam == null) {
      fromJndi = false;
      repoParam = params.getValueParam("repository-name");
    }
    if (repoParam != null) {
      repoName = repoParam.getValue();
    }

    if (repoName == null)
      logger
          .warn("Neither jndi-repository-name nor repository-name is set. Default repository will be obtained");

    ValueParam wsParam = params.getValueParam("workspaces");
    if (wsParam != null) {
      workspaceList = wsParam.getValue().split(",");
    }

    // security config

    PropertiesParam secur = params.getPropertiesParam("security_param");

    if (secur != null) {
      securityMode = SecurityMode.UserMode;

      String challResp = winnbt.getProperty("challenge_response");
      if (challResp != null && challResp.equalsIgnoreCase("true")) {
        securityMode = SecurityMode.UserMode + SecurityMode.EncryptedPasswords;
      }
    } else {
      // set all available security modes for default;
      securityMode = SecurityMode.UserMode + SecurityMode.EncryptedPasswords;
      
      
    }
    
    securityMode = SecurityMode.UserMode + SecurityMode.EncryptedPasswords;
  }

  /**
   * Default (test) configuration
   */
  public ServerConfiguration() {

    determinePlatformType();

    // set broadcast mask? before netBIOS is used
    setBroadcastMask("255.255.255.0");

    // Set the CIFS server name

    setServerName("SMB_SERVER");

    // Set the domain/workgroup name

    try {
      setDomainName(getLocalDomainName().toUpperCase());
    } catch (UnsatisfiedLinkError e) {
      setSMBServerEnabled(false);
      return;
    }

    // Check for a server comment

    setComment("This server is specific SMB server which works with JCR repository.");

    // Check for a bind address

    // Check if the host announcer should be enabled

    // Check if NetBIOS SMB is enabled
    setNetBIOSSMB(true);

    // Check if TCP/IP SMB is enabled
    setTcpipSMB(false);

    // Check if Win32 NetBIOS is enabled

    setWin32NetBIOSName("SMBWINSERV");
    setWin32LANA(-1); // all lanas is available;

    // Check if the native NetBIOS interface has been specified, either
    // 'winsock' or 'netbios'

    String nativeAPI = "netbios";
    if (nativeAPI != null && nativeAPI.length() > 0) {
      // Validate the API type

      boolean useWinsock = false;

      if (nativeAPI.equalsIgnoreCase("netbios"))
        useWinsock = false;
      else if (nativeAPI.equalsIgnoreCase("winsock") == false) {
        setSMBServerEnabled(false);

      }
      // Set the NetBIOS API to use

      setWin32WinsockNetBIOS(useWinsock);
    }

    // Force the older NetBIOS API code to be used on 64Bit Windows

    if (useWinsockNetBIOS() == true && X64.isWindows64()) {
      // Log a warning

      logger.warn("Using older Netbios() API code");

      // Use the older NetBIOS API code

      setWin32WinsockNetBIOS(false);
    }

    // Check if the current operating system is supported by the Win32
    // NetBIOS handler

    String osName = System.getProperty("os.name");
    if (osName.startsWith("Windows") &&
        (osName.endsWith("95") == false && osName.endsWith("98") == false && osName
            .endsWith("ME") == false)) {

      // Call the Win32NetBIOS native code to make sure it is
      // initialized

      if (Win32NetBIOS.LanaEnumerate() != null) {
        // Enable Win32 NetBIOS

        setWin32NetBIOS(true);
      } else {
        logger.warn("No NetBIOS LANAs available");
      }
    } else {

      // Win32 NetBIOS not supported on the current operating system

      setWin32NetBIOS(false);
    }

    // Check if the host announcer should be enabled
    // win32 announcer
    setWin32HostAnnounceInterval(5);
    setWin32HostAnnouncer(true);

    // Check if NetBIOS and/or TCP/IP SMB have been enabled

    if (hasNetBIOSSMB() == false && hasTcpipSMB() == false &&
        hasWin32NetBIOS() == false) {
      setSMBServerEnabled(false);
    } else {
      setSMBServerEnabled(true);
    }

    // Check if session debug is enabled
    // Check for session debug flags
    // "NETBIOS", "STATE", "NEGOTIATE", "TREE", "SEARCH", "INFO", "FILE",
    // "FILEIO", "TRANSACT", "ECHO", "ERROR", "IPC", "LOCK", "PKTTYPE",
    // "DCERPC", "STATECACHE", "NOTIFY",
    // "STREAMS", "SOCKET"
    String flags = "Negotiate,Socket,Tree,PktType,StateCache,State,Search,Info,File,FileIO,Echo,Error,Notify,NETBIOS,IPC,DCERPC,STREAMS";// elem.getAttribute("flags");
    int sessDbg = 0;

    if (flags != null) {

      // Parse the flags

      flags = flags.toUpperCase();
      StringTokenizer token = new StringTokenizer(flags, ",");

      while (token.hasMoreTokens()) {

        // Get the current debug flag token

        String dbg = token.nextToken().trim();

        // Find the debug flag name

        int idx = 0;

        while (idx < m_sessDbgStr.length &&
            m_sessDbgStr[idx].equalsIgnoreCase(dbg) == false)
          idx++;
        // Set the debug flag

        sessDbg += 1 << idx;
      }
    }

    // Set the session debug flags

    setSessionDebugFlags(sessDbg);
  }

  private void setSessionDebugFlags(int sessDbg) {
    m_sessDebug = sessDbg;
  }

  private void setWin32HostAnnouncer(boolean b) {
    m_win32NBAnnounce = b;

  }

  private void setWin32HostAnnounceInterval(int i) {
    m_win32NBAnnounceInterval = i;

  }

  private void setWin32NetBIOS(boolean b) {
    m_win32NBEnable = b;

  }

  private void setWin32WinsockNetBIOS(boolean useWinsock) {
    m_win32NBUseWinsock = useWinsock;

  }

  private void setWin32LANA(int lana) {
    m_win32NBLANA = lana;

  }

  private void setWin32NetBIOSName(String win32Name) {
    m_win32NBName = win32Name;

  }

  private void setTcpipSMB(boolean b) {
    m_tcpSMBEnable = b;

  }

  private void setNetBIOSSMB(boolean b) {
    m_netBIOSEnable = b;

  }

  private void setSMBBindAddress(InetAddress bindAddr) {
    m_smbBindAddress = bindAddr;
  }

  private void setComment(String string) {
    m_comment = string;

  }

  private void setDomainName(String localDomainName) {
    m_domain = localDomainName;

  }

  private void setServerName(String string) {
    m_name = string;
  }

  public boolean isSMBServerEnabled() {

    return m_smbEnable;
  }

  /**
   * Return the server name.
   * 
   * @return java.lang.String
   */
  public String getServerName() {
    return m_name;
  }

  public int getSessionDebugFlags() {

    return m_sessDebug;
  }

  public String getDomainName() {

    return m_domain;
  }

  public String getBroadcastMask() {

    return m_broadcast;
  }

  public String getComment() {
    return m_comment;
  }

  public String getTimeZone() {
    return m_timeZone;
  }

  public int getTimeZoneOffset() {
    return m_tzOffset;
  }

  /**
   * Determine if NetBIOS SMB is enabled (NetBIOS not for windows)
   * 
   * @return boolean
   */
  public final boolean hasNetBIOSSMB() {
    return m_netBIOSEnable;
  }

  /**
   * Determine if TCP/IP SMB is enabled
   * 
   * @return boolean
   */
  public final boolean hasTcpipSMB() {
    return m_tcpSMBEnable;
  }

  /**
   * Determine if Win32 NetBIOS is enabled
   * 
   * @return boolean
   */
  public final boolean hasWin32NetBIOS() {
    return m_win32NBEnable;
  }

  /**
   * Return the Win3 NetBIOS LANA number to use, or -1 for the first available
   * 
   * @return int
   */
  public final int getWin32LANA() {
    return m_win32NBLANA;
  }

  /**
   * Determine if the server should be announced via Win32 NetBIOS, so that it
   * appears under Network Neighborhood.
   * 
   * @return boolean
   */
  public final boolean hasWin32EnableAnnouncer() {
    return m_win32NBAnnounce;
  }

  /**
   * Return the Win32 NetBIOS host announcement interval, in minutes
   * 
   * @return int
   */
  public final int getWin32HostAnnounceInterval() {
    return m_win32NBAnnounceInterval;
  }

  /**
   * Return the Win32 NetBIOS server name, if null the default server name will
   * be used
   * 
   * @return String
   */
  public final String getWin32ServerName() {
    return m_win32NBName;
  }

  /**
   * Determine if the Win32 Netbios() API or Winsock Netbios calls should be
   * used
   * 
   * @return boolean
   */
  public final boolean useWinsockNetBIOS() {
    return m_win32NBUseWinsock;
  }

  /**
   * Return DialectSelector object which consist all enabled dialects
   * 
   * @return DialectSelector
   */
  public DialectSelector getEnabledDialects() {
    DialectSelector dialects = new DialectSelector();

    dialects.AddDialect(Dialect.Core);
    dialects.AddDialect(Dialect.DOSLanMan1);
    dialects.AddDialect(Dialect.DOSLanMan2);
    dialects.AddDialect(Dialect.LanMan1);
    dialects.AddDialect(Dialect.LanMan2);
    dialects.AddDialect(Dialect.LanMan2_1);
    dialects.AddDialect(Dialect.NT);

    return dialects;
  }

  /**
   * Determine if Macintosh extension SMBs are enabled
   * 
   * @return boolean
   */
  public final boolean hasMacintoshExtensions() {
    return m_macExtensions;
  }

  /**
   * Determine the platform type
   */
  private final void determinePlatformType() {
    // Get the operating system type

    String osName = System.getProperty("os.name");

    if (osName.startsWith("Windows"))
      m_platform = PlatformType.WINDOWS;
    else if (osName.equalsIgnoreCase("Linux"))
      m_platform = PlatformType.LINUX;
    else if (osName.startsWith("Mac OS X"))
      m_platform = PlatformType.MACOSX;
    else if (osName.startsWith("Solaris") || osName.startsWith("SunOS"))
      m_platform = PlatformType.SOLARIS;
  }

  /**
   * Get the local domain/workgroup name
   * 
   * @return String
   */
  public final String getLocalDomainName() {
    // Check if the local domain has been set

    if (m_localDomain != null)
      return m_localDomain;

    // Find the local domain name

    String domainName = null;

    if (getPlatformType() == PlatformType.WINDOWS) {
      // Get the local domain/workgroup name via JNI

      if (Win32NetBIOS.isInitialized()) {
        domainName = Win32NetBIOS.GetLocalDomainName();

        // Debug

        if (logger.isDebugEnabled())
          logger.debug("Local domain name is " + domainName + " (via JNI)");

      } else {
        logger
            .error("Win32NetBIOS.dll isnt loaded so can't find local domain name!");
      }
    } else {
      NetBIOSName nbName = null;

      try {
        // Try and find the browse master on the local network

        nbName = NetBIOSSession.FindName(NetBIOSName.BrowseMasterName,
            NetBIOSName.BrowseMasterGroup, 5000);

        // Log the browse master details

        if (logger.isDebugEnabled())
          logger
              .debug("Found browse master at " + nbName.getIPAddressString(0));

        // Get the NetBIOS name list from the browse master

        NetBIOSNameList nbNameList = NetBIOSSession.FindNamesForAddress(nbName
            .getIPAddressString(0));
        if (nbNameList != null) {
          nbName = nbNameList.findName(NetBIOSName.MasterBrowser, false);
          // Set the domain/workgroup name
          if (nbName != null)
            domainName = nbName.getName();
        }
      } catch (IOException ex) {
      }
    }

    // Save the local domain name

    m_localDomain = domainName;

    // Return the local domain/workgroup name

    return domainName;
  }

  private PlatformType getPlatformType() {

    return m_platform;
  }

  /**
   * Set the SMB server enabled state
   * 
   * @param ena
   *          boolean
   */
  public final void setSMBServerEnabled(boolean ena) {
    m_smbEnable = ena;
  }

  /**
   * Set the broadcast mask to be used for broadcast datagrams.
   * 
   * @param mask
   *          String
   */
  public final void setBroadcastMask(String mask) {
    m_broadcast = mask;

    // Copy settings to the NetBIOS session class

    NetBIOSSession.setSubnetMask(mask);
  }

  /**
   * Set the primary WINS server address
   * 
   * @param addr
   *          InetAddress
   */
  public final void setPrimaryWINSServer(InetAddress addr) {
    m_winsPrimary = addr;
  }

  /**
   * Set the secondary WINS server address
   * 
   * @param addr
   *          InetAddress
   */
  public final void setSecondaryWINSServer(InetAddress addr) {
    m_winsSecondary = addr;
  }

  public boolean isFromJndi() {
    return fromJndi;
  }

  public String[] getWorkspaceList() {
    return workspaceList;
  }

  public String getRepoName() {
    return repoName;
  }

  /**
   * Return the NetBIOS session port
   * 
   * @return int
   */
  public final int getNetBIOSSessionPort() {
    return m_nbSessPort;
  }

  /**
   * Return the local address that the SMB server should bind to.
   * 
   * @return java.net.InetAddress
   */
  public final InetAddress getSMBBindAddress() {
    return m_smbBindAddress;
  }

  /**
   * Determine if the server should be announced so that it appears under
   * Network Neighborhood.
   * 
   * @return boolean
   */
  public final boolean hasEnableAnnouncer() {
    return m_announce;
  }

  /**
   * Return the NetBIOS datagram port
   * 
   * @return int
   */
  public final int getNetBIOSDatagramPort() {
    return m_nbDatagramPort;
  }

  /**
   * Return the host announcement interval, in minutes
   * 
   * @return int
   */
  public final int getHostAnnounceInterval() {
    return m_announceInterval;
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
   * Return the native SMB port
   * 
   * @return int
   */
  public final int getTcpipSMBPort() {
    return m_tcpSMBPort;
  }

  /**
   * Determine if the SMB server should bind to a particular local address
   * 
   * @return boolean
   */
  public final boolean hasSMBBindAddress() {
    return m_smbBindAddress != null ? true : false;
  }

  private void setHostAnnounce(boolean b) {
    m_announce = b;
  }

  private void setHostAnnounceInterval(int interval) {
    m_announceInterval = interval;
  }

  public int getSecurity() {
    return securityMode;
  }

}
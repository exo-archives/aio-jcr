package org.exoplatform.services.cifs;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.StringTokenizer;


import org.apache.commons.logging.Log;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cifs.netbios.NetBIOSName;
import org.exoplatform.services.cifs.netbios.NetBIOSNameList;
import org.exoplatform.services.cifs.netbios.NetBIOSSession;
import org.exoplatform.services.cifs.netbios.RFCNetBIOSProtocol;
import org.exoplatform.services.cifs.netbios.win32.Win32NetBIOS;
import org.exoplatform.services.cifs.smb.Dialect;
import org.exoplatform.services.cifs.smb.DialectSelector;
import org.exoplatform.services.cifs.smb.ServerType;
import org.exoplatform.services.cifs.util.IPAddress;
import org.exoplatform.services.cifs.util.X64;
import org.exoplatform.services.log.ExoLogger;
/*
 * Service (server) configuration class
 */
public class ServerConfiguration {
	private static final Log logger = ExoLogger.getLogger("org.exoplatform.smb.protocol");
	
    private static final String m_sessDbgStr[] = { "NETBIOS", "STATE", "NEGOTIATE", "TREE", "SEARCH", "INFO", "FILE",
        "FILEIO", "TRANSACT", "ECHO", "ERROR", "IPC", "LOCK", "PKTTYPE", "DCERPC", "STATECACHE", "NOTIFY",
        "STREAMS", "SOCKET" };

    // Platform types

    public enum PlatformType
    {
        Unknown, WINDOWS, LINUX, SOLARIS, MACOSX
    };

    // Token name to substitute current server name into the CIFS server name

    private static final String TokenLocalName = "${localname}";

    // Runtime platform type
    
    private PlatformType m_platform = PlatformType.WINDOWS;

    // Main server enable flags, to enable SMB and/or NFS server components

    private boolean m_smbEnable;
    
    // Server name

    private String m_name;

    // Server type, used by the host announcer

    private int m_srvType = ServerType.WorkStation + ServerType.Server + ServerType.NTServer;

    // Server comment

    private String m_comment;

    // Server domain

    private String m_domain;

    // Network broadcast mask string

    private String m_broadcast;

    // NetBIOS ports

    private int m_nbNamePort;
    private int m_nbSessPort;
    private int m_nbDatagramPort;

  	// Native SMB port
  	
  	private int m_tcpSMBPort;

    // Announce the server to network neighborhood, announcement interval in
    // minutes

    private boolean m_announce;
    private int m_announceInterval; // default 5

    // Default session debugging setting

    private int m_sessDebug;

    // Flags to indicate if NetBIOS, native TCP/IP SMB and/or Win32 NetBIOS
    // should be enabled

    private boolean m_netBIOSEnable = true;
    private boolean m_tcpSMBEnable = true;
    private boolean m_win32NBEnable = true;

    // Address to bind the SMB server to, if null all local addresses are used

    private InetAddress m_smbBindAddress=null;
	
    // Address to bind the NetBIOS name server to, if null all addresses are
	// used

//    private InetAddress m_nbBindAddress=null;

    // WINS servers

    private InetAddress m_winsPrimary;
    private InetAddress m_winsSecondary;

    // Enable/disable Macintosh extension SMBs

    private boolean m_macExtensions=false;

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

    private boolean m_win32NBAnnounce;
    private int m_win32NBAnnounceInterval;
    
    // Use Winsock NetBIOS interface if true, else use the Netbios() API
	// interface
    
    private boolean m_win32NBUseWinsock;

    // --------------------------------------------------------------------------------


    // Global server configuration
    //
    // Timezone name and offset from UTC in minutes

    private String m_timeZone;
    private int m_tzOffset;

    // JCE provider class name

    //private String m_jceProviderClass;

    // Local server name and domain/workgroup name

    private String m_localName;
    private String m_localDomain;

    // flag to indicate successful initialization
    
    private boolean initialised;
    
    
    
    
    //private SharedDeviceList m_shareList;

	/*
	 * Configuration by XML-config
	 */
	public ServerConfiguration(InitParams params) {
		
	}
	
	/**
	 * Default (test) configuration
	 */
	public ServerConfiguration(){
		
		
		determinePlatformType();

		//set broadcast mask? before netBIOS is used
		setBroadcastMask("255.255.255.0");
		
      // Set the CIFS server name

        setServerName("SMB_SERVER");

        // Set the domain/workgroup name

        setDomainName(getLocalDomainName().toUpperCase());
        
        // Check for a server comment

        setComment("This server is specific SMB server which works with JCR repository.");

        // Check for a bind address

        // Check if the host announcer should be enabled

        // Check if NetBIOS SMB is enabled
            setNetBIOSSMB(false);
 
        // Check if TCP/IP SMB is enabled
            setTcpipSMB(false);

        // Check if Win32 NetBIOS is enabled
            
            setWin32NetBIOSName("SMBWINSERV");
            setWin32LANA(-1); //all lanas is availavle;
          
            // Check if the native NetBIOS interface has been specified, either
			// 'winsock' or 'netbios'
            
            String nativeAPI = "netbios";
            if ( nativeAPI != null && nativeAPI.length() > 0)
            {
                // Validate the API type
                
                boolean useWinsock = true;
                
                if ( nativeAPI.equalsIgnoreCase("netbios"))
                    useWinsock = false;
                else if ( nativeAPI.equalsIgnoreCase("winsock") == false){
                	setSMBServerEnabled(false);
                	              
                }
                // Set the NetBIOS API to use
                
                setWin32WinsockNetBIOS( useWinsock);
            }

            
            // Force the older NetBIOS API code to be used on 64Bit Windows
            
            if ( useWinsockNetBIOS() == true && X64.isWindows64())
            {
                // Log a warning
                
                logger.warn("Using older Netbios() API code");
                
                // Use the older NetBIOS API code
                
                setWin32WinsockNetBIOS( false);
            }
            
            // Check if the current operating system is supported by the Win32
            // NetBIOS handler

            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")
                    && (osName.endsWith("95") == false && osName.endsWith("98") == false && osName.endsWith("ME") == false))
            {

                // Call the Win32NetBIOS native code to make sure it is
				// initialized

                if ( Win32NetBIOS.LanaEnumerate() != null)
                {
                    // Enable Win32 NetBIOS
    
                    setWin32NetBIOS(true);
                }
                else
                {
                    logger.warn("No NetBIOS LANAs available");
                }
            }
            else
            {

                // Win32 NetBIOS not supported on the current operating system

                setWin32NetBIOS(false);
            }
            
       
        // Check if the host announcer should be enabled
            //win32 announcer
            setWin32HostAnnounceInterval(5);
            setWin32HostAnnouncer(true);


        // Check if NetBIOS and/or TCP/IP SMB have been enabled

        if (hasNetBIOSSMB() == false && hasTcpipSMB() == false && hasWin32NetBIOS() == false){
        	setSMBServerEnabled(false);
        }
        else{
        	setSMBServerEnabled(true);
        }

        // Check if WINS servers are configured
/*
        elem = config.getConfigElement("WINS");

        if (elem != null)
        {

            // Get the primary WINS server

            ConfigElement priWinsElem = elem.getChild("primary");

            if (priWinsElem == null || priWinsElem.getValue().length() == 0)
                throw new AlfrescoRuntimeException("No primary WINS server configured");

            // Validate the WINS server address

            InetAddress primaryWINS = null;

            try
            {
                primaryWINS = InetAddress.getByName(priWinsElem.getValue());
            }
            catch (UnknownHostException ex)
            {
                throw new AlfrescoRuntimeException("Invalid primary WINS server address, " + priWinsElem.getValue());
            }

            // Check if a secondary WINS server has been specified

            ConfigElement secWinsElem = elem.getChild("secondary");
            InetAddress secondaryWINS = null;

            if (secWinsElem != null)
            {

                // Validate the secondary WINS server address

                try
                {
                    secondaryWINS = InetAddress.getByName(secWinsElem.getValue());
                }
                catch (UnknownHostException ex)
                {
                    throw new AlfrescoRuntimeException("Invalid secondary WINS server address, "
                            + secWinsElem.getValue());
                }
            }

            // Set the WINS server address(es)

            setPrimaryWINSServer(primaryWINS);
            if (secondaryWINS != null)
                setSecondaryWINSServer(secondaryWINS);

            // Pass the setting to the NetBIOS session class

            NetBIOSSession.setWINSServer(primaryWINS);
        }

        // Check if WINS is configured, if we are running on Windows and socket
		// based NetBIOS is enabled

        else if (hasNetBIOSSMB() && getPlatformType() == PlatformType.WINDOWS)
        {
            // Get the WINS server list

            String winsServers = Win32NetBIOS.getWINSServerList();

            if (winsServers != null)
            {
                // Use the first WINS server address for now

                StringTokenizer tokens = new StringTokenizer(winsServers, ",");
                String addr = tokens.nextToken();

                try
                {
                    // Convert to a network address and check if the WINS server
					// is accessible

                    InetAddress winsAddr = InetAddress.getByName(addr);

                    Socket winsSocket = new Socket();
                    InetSocketAddress sockAddr = new InetSocketAddress( winsAddr, RFCNetBIOSProtocol.NAME_PORT);
                    
                    winsSocket.connect(sockAddr, 3000);
                    winsSocket.close();
                    
                    // Set the primary WINS server address
                    
                    setPrimaryWINSServer(winsAddr);

                    // Debug

                    if (logger.isDebugEnabled())
                        logger.debug("Configuring to use WINS server " + addr);
                }
                catch (IOException ex)
                {
                    if ( logger.isDebugEnabled())
                        logger.debug("Failed to connect to auto WINS server " + addr);
                }
            }
        }*/


        // Check if session debug is enabled

   
            // Check for session debug flags
            //	 "NETBIOS", "STATE", "NEGOTIATE", "TREE", "SEARCH", "INFO", "FILE",
            //     "FILEIO", "TRANSACT", "ECHO", "ERROR", "IPC", "LOCK", "PKTTYPE", "DCERPC", "STATECACHE", "NOTIFY",
            //      "STREAMS", "SOCKET" 
            String flags = "Negotiate,Socket,Tree,PktType,StateCache,State,Search,Info,File,FileIO,Echo,Error,Notify";// elem.getAttribute("flags");
            int sessDbg = 0;


            if (flags != null)
            {

                // Parse the flags

                flags = flags.toUpperCase();
                StringTokenizer token = new StringTokenizer(flags, ",");

                while (token.hasMoreTokens())
                {

                    // Get the current debug flag token

                    String dbg = token.nextToken().trim();

                    // Find the debug flag name

                    int idx = 0;

                    while (idx < m_sessDbgStr.length && m_sessDbgStr[idx].equalsIgnoreCase(dbg) == false)
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

	/**
	 * Return global Shared device list
	 * @return SharedDeviceList
	 */
	//public SharedDeviceList getShares() {
	//	return m_shareList;
	//}

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
    public final boolean hasNetBIOSSMB()
    {
        return m_netBIOSEnable;
    }

    /**
	 * Determine if TCP/IP SMB is enabled
	 * 
	 * @return boolean
	 */
    public final boolean hasTcpipSMB()
    {
        return m_tcpSMBEnable;
    }

    /**
	 * Determine if Win32 NetBIOS is enabled
	 * 
	 * @return boolean
	 */
    public final boolean hasWin32NetBIOS()
    {
        return m_win32NBEnable;
    }

    /**
	 * Return the Win3 NetBIOS LANA number to use, or -1 for the first available
	 * 
	 * @return int
	 */
    public final int getWin32LANA()
    {
        return m_win32NBLANA;
    }
    /**
	 * Determine if the server should be announced via Win32 NetBIOS, so that it
	 * appears under Network Neighborhood.
	 * 
	 * @return boolean
	 */
    public final boolean hasWin32EnableAnnouncer()
    {
        return m_win32NBAnnounce;
    }

    /**
	 * Return the Win32 NetBIOS host announcement interval, in minutes
	 * 
	 * @return int
	 */
    public final int getWin32HostAnnounceInterval()
    {
        return m_win32NBAnnounceInterval;
    }
    
    /**
	 * Return the Win32 NetBIOS server name, if null the default server name
	 * will be used
	 * 
	 * @return String
	 */
    public final String getWin32ServerName()
    {
        return m_win32NBName;
    }

    /**
	 * Determine if the Win32 Netbios() API or Winsock Netbios calls should be
	 * used
	 * 
	 * @return boolean
	 */
    public final boolean useWinsockNetBIOS()
    {
        return m_win32NBUseWinsock;
    }

    /**
     * Return DialectSelector object which consist all enabled dialects
     * 
     * @return DialectSelector
     */
	public DialectSelector getEnabledDialects() {
		DialectSelector dialects = new DialectSelector();

		//dialects.AddDialect(Dialect.Core);
		dialects.AddDialect(Dialect.DOSLanMan1);
		dialects.AddDialect(Dialect.DOSLanMan2);
		dialects.AddDialect(Dialect.LanMan1);
		dialects.AddDialect(Dialect.LanMan2);
		dialects.AddDialect(Dialect.LanMan2_1);
	//	dialects.AddDialect(Dialect.NT);
		
		return dialects;
	}
	
    /**
	 * Determine if Macintosh extension SMBs are enabled
	 * 
	 * @return boolean
	 */
    public final boolean hasMacintoshExtensions()
    {
        return m_macExtensions;
    }
    /**
	 * Determine the platform type
	 */
    private final void determinePlatformType()
    {
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
    public final String getLocalDomainName()
    {
        // Check if the local domain has been set

        if (m_localDomain != null)
            return m_localDomain;

        // Find the local domain name

        String domainName = null;

        if (getPlatformType() == PlatformType.WINDOWS)
        {
            // Get the local domain/workgroup name via JNI

            domainName = Win32NetBIOS.GetLocalDomainName();

            // Debug

            if (logger.isDebugEnabled())
                logger.debug("Local domain name is " + domainName + " (via JNI)");
        }
        else
        {
            NetBIOSName nbName = null;

            try
            {
                // Try and find the browse master on the local network

                nbName = NetBIOSSession.FindName(NetBIOSName.BrowseMasterName, NetBIOSName.BrowseMasterGroup, 5000);

                // Log the browse master details

                if (logger.isDebugEnabled())
                    logger.debug("Found browse master at " + nbName.getIPAddressString(0));

                // Get the NetBIOS name list from the browse master

                NetBIOSNameList nbNameList = NetBIOSSession.FindNamesForAddress(nbName.getIPAddressString(0));
                if (nbNameList != null)
                {
                    nbName = nbNameList.findName(NetBIOSName.MasterBrowser, false);
                    // Set the domain/workgroup name
                    if (nbName != null)
                        domainName = nbName.getName();
                }
            }
            catch (IOException ex)
            {
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
	 *            boolean
	 */
    public final void setSMBServerEnabled(boolean ena)
    {
        m_smbEnable = ena;
    }
    /**
	 * Set the broadcast mask to be used for broadcast datagrams.
	 * 
	 * @param mask
	 *            String
	 */
    public final void setBroadcastMask(String mask)
    {
        m_broadcast = mask;

        // Copy settings to the NetBIOS session class

        NetBIOSSession.setSubnetMask(mask);
    }
    /**
	 * Set the primary WINS server address
	 * 
	 * @param addr
	 *            InetAddress
	 */
    public final void setPrimaryWINSServer(InetAddress addr)
    {
        m_winsPrimary = addr;
    }

    /**
	 * Set the secondary WINS server address
	 * 
	 * @param addr
	 *            InetAddress
	 */
    public final void setSecondaryWINSServer(InetAddress addr)
    {
        m_winsSecondary = addr;
    }

}

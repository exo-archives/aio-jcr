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
package org.exoplatform.services.cifs.smb.server;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.netbios.RFCNetBIOSProtocol;
import org.exoplatform.services.cifs.server.core.ShareType;
import org.exoplatform.services.cifs.server.core.SharedDevice;
import org.exoplatform.services.cifs.server.filesys.DiskInfo;
import org.exoplatform.services.cifs.server.filesys.FileAccess;
import org.exoplatform.services.cifs.server.filesys.FileAction;
import org.exoplatform.services.cifs.server.filesys.FileAttribute;
import org.exoplatform.services.cifs.server.filesys.FileExistsException;
import org.exoplatform.services.cifs.server.filesys.FileInfo;
import org.exoplatform.services.cifs.server.filesys.FileOpenParams;
import org.exoplatform.services.cifs.server.filesys.FileSystem;
import org.exoplatform.services.cifs.server.filesys.JCRDriver;
import org.exoplatform.services.cifs.server.filesys.JCRNetworkFile;
import org.exoplatform.services.cifs.server.filesys.NameCoder;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.cifs.server.filesys.SearchContext;
import org.exoplatform.services.cifs.server.filesys.TooManyConnectionsException;
import org.exoplatform.services.cifs.server.filesys.TooManyFilesException;
import org.exoplatform.services.cifs.server.filesys.TreeConnection;
import org.exoplatform.services.cifs.server.filesys.UnsupportedInfoLevelException;
import org.exoplatform.services.cifs.server.filesys.VolumeInfo;
import org.exoplatform.services.cifs.smb.DataType;
import org.exoplatform.services.cifs.smb.FileInfoLevel;
import org.exoplatform.services.cifs.smb.FindFirstNext;
import org.exoplatform.services.cifs.smb.InvalidUNCPathException;
import org.exoplatform.services.cifs.smb.NTIOCtl;
import org.exoplatform.services.cifs.smb.NTTime;
import org.exoplatform.services.cifs.smb.PCShare;
import org.exoplatform.services.cifs.smb.PacketType;
import org.exoplatform.services.cifs.smb.SMBDate;
import org.exoplatform.services.cifs.smb.SMBException;
import org.exoplatform.services.cifs.smb.SMBStatus;
import org.exoplatform.services.cifs.smb.WinNT;
import org.exoplatform.services.cifs.util.DataBuffer;
import org.exoplatform.services.cifs.util.DataPacker;
import org.exoplatform.services.cifs.util.HexDump;
import org.exoplatform.services.cifs.util.WildCard;
import org.exoplatform.services.jcr.core.ExtendedProperty;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * NT SMB Protocol Handler Class
 * <p>
 * The NT protocol handler processes the additional SMBs that were added to the
 * protocol in the NT SMB dialect.
 */
public class NTProtocolHandler extends CoreProtocolHandler {
  private static Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs.smb.server.NTProtocolHandler");

  // Constants
  //
  // Flag to enable returning of '.' and '..' directory information in FindFirst
  // request

  public static final boolean ReturnDotFiles = true;

  // Flag to enable faking of oplock requests when opening files

  public static final boolean FakeOpLocks = false;

  // Number of write requests per file to report file size change notifications

  public static final int FileSizeChangeRate = 10;

  // Security descriptor to allow Everyone access, returned by the
  // QuerySecurityDescrptor NT
  // transaction when NTFS streams are enabled for a virtual filesystem.

  private static byte[] _sdEveryOne = { 0x01, 0x00, 0x04, (byte) 0x80, 0x14,
      0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2c,
      0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
      0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
      0x00, 0x00, 0x00, 0x02, 0x00, 0x1c, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x14, 0x00, (byte) 0xff, 0x01, 0x1f, 0x00, 0x01, 0x01, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };

  /**
   * Class constructor.
   */
  protected NTProtocolHandler() {
    super();
  }

  /**
   * Class constructor
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected NTProtocolHandler(SMBSrvSession sess) {
    super(sess);
  }

  /**
   * Return the protocol name
   * 
   * @return String
   */
  public String getName() {
    return "NT";
  }

  /**
   * Run the NT SMB protocol handler to process the received SMB packet
   * 
   * @exception IOException
   * @exception SMBSrvException
   * @exception TooManyConnectionsException
   */
  public boolean runProtocol() throws java.io.IOException, SMBSrvException,
      TooManyConnectionsException {

    // Check if the SMB packet is initialized

    if (m_smbPkt == null)
      m_smbPkt = m_sess.getReceivePacket();

    // Check if the received packet has a valid SMB signature

    if (m_smbPkt.checkPacketSignature() == false)
      throw new IOException("Invalid SMB signature");

    // Determine if the request has a chained command, if so then we will copy
    // the incoming request so that/ a chained reply can be built.

    SMBSrvPacket outPkt = m_smbPkt;
    boolean chainedCmd = hasChainedCommand(m_smbPkt);

    if (chainedCmd) {

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_STATE))
        logger.debug("AndX Command = 0x" +
            Integer.toHexString(m_smbPkt.getAndXCommand()));

      // Copy the request packet into a new packet for the reply

      outPkt = new SMBSrvPacket(m_smbPkt, m_smbPkt.getPacketLength());
    }

    // Reset the byte unpack offset

    m_smbPkt.resetBytePointer();

    // Set the process id from the received packet, this can change for the same
    // session and
    // needs to be set
    // for lock ownership checking

    m_sess.setProcessId(m_smbPkt.getProcessId());

    // Determine the SMB command type

    boolean handledOK = true;

    switch (m_smbPkt.getCommand()) {

    // NT Session setup

    case PacketType.SessionSetupAndX:
      procSessionSetup(outPkt);
      break;

    // Tree connect

    case PacketType.TreeConnectAndX:
      procTreeConnectAndX(outPkt);
      break;

    // Transaction/transaction2

    case PacketType.Transaction:
    case PacketType.Transaction2:
      procTransact2(outPkt);
      break;

    // Transaction/transaction2 secondary

    case PacketType.TransactionSecond:
    case PacketType.Transaction2Second:
      procTransact2Secondary(outPkt);
      break;

    // Close a search started via the FindFirst transaction2 command

    case PacketType.FindClose2:
      procFindClose(outPkt);
      break;

    // Open a file

    case PacketType.OpenAndX:
      procOpenAndX(outPkt);
      break;

    // Close a file

    case PacketType.CloseFile:
      procCloseFile(outPkt);
      break;

    // Read a file

    case PacketType.ReadAndX:
      procReadAndX(outPkt);
      break;

    // Write to a file

    case PacketType.WriteAndX:
      procWriteAndX(outPkt);
      break;

    // Rename file

    case PacketType.RenameFile:
      procRenameFile(outPkt);
      break;

    // Delete file

    case PacketType.DeleteFile:
      procDeleteFile(outPkt);
      break;

    // Delete directory

    case PacketType.DeleteDirectory:
      procDeleteDirectory(outPkt);
      break;

    // Tree disconnect

    case PacketType.TreeDisconnect:
      procTreeDisconnect(outPkt);
      break;

    // Lock/unlock regions of a file

    case PacketType.LockingAndX:
      procLockingAndX(outPkt);
      break;

    // Logoff a user

    case PacketType.LogoffAndX:
      procLogoffAndX(outPkt);
      break;

    // NT Create/open file

    case PacketType.NTCreateAndX:
      procNTCreateAndX(outPkt);
      break;

    // Tree connection (without AndX batching)

    case PacketType.TreeConnect:
      super.runProtocol();
      break;

    // NT cancel

    case PacketType.NTCancel:
      procNTCancel(outPkt);
      break;

    // NT transaction

    case PacketType.NTTransact:
      procNTTransaction(outPkt);
      break;

    // NT transaction secondary

    case PacketType.NTTransactSecond:
      procNTTransactionSecondary(outPkt);
      break;

    // Echo request

    case PacketType.Echo:
      super.procEcho(outPkt);
      break;

    // Default

    default:

      // Get the tree connection details, if it is a disk or printer type
      // connection then pass
      // the request to the
      // core protocol handler

      int treeId = m_smbPkt.getTreeId();
      TreeConnection conn = null;
      if (treeId != -1)
        conn = m_sess.findTreeConnection(treeId);

      if (conn != null) {

        // Check if this is a disk or print connection, if so then send the
        // request to the
        // core protocol handler

        if (conn.getSharedDevice().getType() == ShareType.DISK ||
            conn.getSharedDevice().getType() == ShareType.PRINTER) {

          // Chain to the core protocol handler

          handledOK = super.runProtocol();
        } else if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

          // Send the request to IPC$ remote admin handler

          IPCHandler.processIPCRequest(m_sess, outPkt);
          handledOK = true;
        }
      }
      break;
    }

    // Return the handled status

    return handledOK;
  }

  /**
   * Process the NT SMB session setup request.
   * 
   * @param outPkt
   *          Response SMB packet.
   */
  protected void procSessionSetup(SMBSrvPacket outPkt) throws SMBSrvException,
      IOException, TooManyConnectionsException {

    // Check that the received packet looks like a valid NT session setup andX
    // request

    if (m_smbPkt.checkPacketIsValid(13, 0) == false) {
      throw new SMBSrvException(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
    }

    // Extract the session details

    int maxBufSize = m_smbPkt.getParameter(2); // max client buffer
    int maxMpx = m_smbPkt.getParameter(3); // max multiplex
    int vcNum = m_smbPkt.getParameter(4); // virtual circuit number
    int ascPwdLen = m_smbPkt.getParameter(7); // caseInsensitive pathword length
    int uniPwdLen = m_smbPkt.getParameter(8); // caseSensiteve pathword length
    int capabs = m_smbPkt.getParameterLong(11); // capabilities

    // Extract the client details from the session setup request

    byte[] buf = m_smbPkt.getBuffer();

    // Determine if ASCII or unicode strings are being used

    boolean isUni = m_smbPkt.isUnicode();

    // Extract the password strings

    byte[] ascPwd = m_smbPkt.unpackBytes(ascPwdLen);
    byte[] uniPwd = m_smbPkt.unpackBytes(uniPwdLen);

    // Extract the user name string

    String user = m_smbPkt.unpackString(isUni);

    if (user == null) {
      throw new SMBSrvException(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
    }

    // Extract the clients primary domain name string

    String domain = "";

    if (m_smbPkt.hasMoreData()) {

      // Extract the callers domain name

      domain = m_smbPkt.unpackString(isUni);

      if (domain == null) {
        throw new SMBSrvException(SMBStatus.NTInvalidParameter,
            SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      }
    }

    // Extract the clients native operating system

    String clientOS = "";

    if (m_smbPkt.hasMoreData()) {

      // Extract the callers operating system name

      clientOS = m_smbPkt.unpackString(isUni);

      if (clientOS == null) {
        throw new SMBSrvException(SMBStatus.NTInvalidParameter,
            SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      }
    }

    // DEBUG

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE)) {
      logger.debug("NT Session setup from user=" + user + ", password=" +
          (uniPwd != null ? HexDump.hexString(uniPwd) : "none") + ", ANSIpwd=" +
          (ascPwd != null ? HexDump.hexString(ascPwd) : "none") + ", domain=" +
          domain + ", os=" + clientOS + ", VC=" + vcNum + ", maxBuf=" +
          maxBufSize + ", maxMpx=" + maxMpx + ", authCtx=" +
          m_sess.getAuthenticationContext());
      logger.debug("  MID=" + m_smbPkt.getMultiplexId() + ", UID=" +
          m_smbPkt.getUserId() + ", PID=" + m_smbPkt.getProcessId());
    }

    // Store the client maximum buffer size, maximum multiplexed requests count
    // and client capability flags

    m_sess.setClientMaximumBufferSize(maxBufSize != 0 ? maxBufSize
        : SMBSrvSession.DefaultBufferSize);
    m_sess.setClientMaximumMultiplex(maxMpx);
    m_sess.setClientCapabilities(capabs);

    // Create the client information and store in the session

    // TODO Here must be Authenticate the user

    boolean isGuest = true;
    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
      logger.debug("User " + user + ", logged on as guest");

    // Indicate that the session is logged on

    m_sess.setLoggedOn(true);

    // Build the session setup response SMB

    outPkt.setParameterCount(3);
    outPkt.setParameter(0, 0); // No chained response
    outPkt.setParameter(1, 0); // Offset to chained response
    outPkt.setParameter(2, isGuest ? 1 : 0);
    outPkt.setByteCount(0);

    outPkt.setTreeId(0);
    outPkt.setUserId(0);

    // Set the various flags

    int flags = outPkt.getFlags();
    flags &= ~SMBSrvPacket.FLG_CASELESS;
    outPkt.setFlags(flags);

    int flags2 = SMBSrvPacket.FLG2_LONGFILENAMES;
    if (isUni)
      flags2 += SMBSrvPacket.FLG2_UNICODE;
    outPkt.setFlags2(flags2);

    // Pack the OS, dialect and domain name strings.

    int pos = outPkt.getByteOffset();
    buf = outPkt.getBuffer();

    if (isUni)
      pos = DataPacker.wordAlign(pos);

    pos = DataPacker.putString("Java", buf, pos, true, isUni);
    pos = DataPacker.putString("Alfresco CIFS Server " +
        m_sess.getServer().isVersion(), buf, pos, true, isUni);
    pos = DataPacker.putString(m_sess.getServer().getConfiguration()
        .getDomainName(), buf, pos, true, isUni);

    outPkt.setByteCount(pos - outPkt.getByteOffset());

    // Check if there is a chained command, or commands

    pos = outPkt.getLength();

    if (m_smbPkt.hasAndXCommand() &&
        m_smbPkt.getPosition() < m_smbPkt.getReceivedLength()) {

      // Process any chained commands, AndX

      pos = procAndXCommands(outPkt);
      pos -= RFCNetBIOSProtocol.HEADER_LEN;
    } else {
      // Indicate that there are no chained replies

      outPkt.setAndXCommand(SMBSrvPacket.NO_ANDX_CMD);
    }

    // Send the session setup response

    m_sess.sendResponseSMB(outPkt, pos);

    // Update the session state if the response indicates a success status. A
    // multi stage session setup
    // response returns a warning status.

    if (outPkt.getLongErrorCode() == SMBStatus.NTSuccess) {
      // Update the session state

      m_sess.setState(SMBSrvSessionState.SMBSESSION);

      // Notify listeners that a user has logged onto the session

      m_sess.getSMBServer().sessionLoggedOn(m_sess);
    }
  }

  /**
   * Process the chained SMB commands (AndX).
   * 
   * @param outPkt
   *          Reply packet.
   * @return New offset to the end of the reply packet
   */
  protected final int procAndXCommands(SMBSrvPacket outPkt) {

    // Use the byte offset plus length to calculate the current output packet
    // end position

    return procAndXCommands(outPkt, outPkt.getByteOffset() +
        outPkt.getByteCount(), null);
  }

  /**
   * Process the chained SMB commands (AndX).
   * 
   * @param outPkt
   *          Reply packet.
   * @param endPos
   *          Current end of packet position
   * @param file
   *          Current file , or null if no file context in chain
   * @return New offset to the end of the reply packet
   */
  protected final int procAndXCommands(SMBSrvPacket outPkt, int endPos,
      NetworkFile file) {

    // Get the chained command and command block offset

    int andxCmd = m_smbPkt.getAndXCommand();
    int andxOff = m_smbPkt.getParameter(1) + RFCNetBIOSProtocol.HEADER_LEN;

    // Set the initial chained command and offset

    outPkt.setAndXCommand(andxCmd);
    outPkt.setParameter(1, andxOff - RFCNetBIOSProtocol.HEADER_LEN);

    // Pointer to the last parameter block, starts with the main command
    // parameter block

    int paramBlk = SMBSrvPacket.WORDCNT;

    // Get the current end of the reply packet offset

    int endOfPkt = endPos;
    boolean andxErr = false;

    while (andxCmd != SMBSrvPacket.NO_ANDX_CMD && andxErr == false) {

      // Determine the chained command type

      int prevEndOfPkt = endOfPkt;
      boolean endOfChain = false;

      switch (andxCmd) {

      // Tree connect

      case PacketType.TreeConnectAndX:
        endOfPkt = procChainedTreeConnectAndX(andxOff, outPkt, endOfPkt);
        break;

      // Close file

      case PacketType.CloseFile:
        endOfPkt = procChainedClose(andxOff, outPkt, endOfPkt);
        endOfChain = true;
        break;

      // Read file

      case PacketType.ReadAndX:
        endOfPkt = procChainedReadAndX(andxOff, outPkt, endOfPkt, file);
        break;

      // Chained command was not handled

      default:
        break;
      }

      // Set the next chained command details in the current parameter block

      outPkt.setAndXCommand(paramBlk, andxCmd);
      outPkt.setAndXParameter(paramBlk, 1, prevEndOfPkt -
          RFCNetBIOSProtocol.HEADER_LEN);

      // Check if the end of chain has been reached, if not then look for the
      // next
      // chained command in the request. End of chain might be set if the
      // current command
      // is not an AndX SMB command.

      if (endOfChain == false) {

        // Advance to the next chained command block

        andxCmd = m_smbPkt.getAndXParameter(andxOff, 0) & 0x00FF;
        andxOff = m_smbPkt.getAndXParameter(andxOff, 1);

        // Advance the current parameter block

        paramBlk = prevEndOfPkt;
      } else {

        // Indicate that the end of the command chain has been reached

        andxCmd = SMBSrvPacket.NO_ANDX_CMD;
      }

      // Check if the chained command has generated an error status

      if (outPkt.getErrorCode() != SMBStatus.Success)
        andxErr = true;
    }

    // Return the offset to the end of the reply packet

    return endOfPkt;
  }

  /**
   * Process a chained tree connect request.
   * 
   * @return New end of reply offset.
   * @param cmdOff
   *          int Offset to the chained command within the request packet.
   * @param outPkt
   *          SMBSrvPacket Reply packet.
   * @param endOff
   *          int Offset to the current end of the reply packet.
   */
  protected final int procChainedTreeConnectAndX(int cmdOff,
      SMBSrvPacket outPkt, int endOff) {
    logger.debug(":procChainedTreeConnectAndX");
    // Extract the parameters

    int pwdLen = m_smbPkt.getAndXParameter(cmdOff, 3);

    // Reset the byte pointer for data unpacking

    m_smbPkt.setBytePointer(m_smbPkt.getAndXByteOffset(cmdOff), m_smbPkt
        .getAndXByteCount(cmdOff));

    // Extract the password string

    String pwd = null;

    if (pwdLen > 0) {
      byte[] pwdByt = m_smbPkt.unpackBytes(pwdLen);
      pwd = new String(pwdByt);
    }

    // Extract the requested share name, as a UNC path

    boolean unicode = m_smbPkt.isUnicode();

    String uncPath = m_smbPkt.unpackString(unicode);
    if (uncPath == null) {
      outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return endOff;
    }

    // Extract the service type string

    String service = m_smbPkt.unpackString(false);
    if (service == null) {
      outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return endOff;
    }

    // Convert the service type to a shared device type, client may specify
    // '?????' in which
    // case we ignore the error.

    int servType = ShareType.ServiceAsType(service);
    if (servType == ShareType.UNKNOWN && service.compareTo("?????") != 0) {
      outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return endOff;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
      logger.debug("NT ANDX Tree Connect AndX - " + uncPath + ", " + service);

    // Parse the requested share name

    PCShare share = null;

    try {
      share = new PCShare(uncPath);
    } catch (InvalidUNCPathException ex) {
      outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return endOff;
    }

    // Map the IPC$ share to the admin pipe type

    if ((servType == ShareType.NAMEDPIPE) &&
        (share.getShareName().compareTo("IPC$") == 0))
      servType = ShareType.ADMINPIPE;

    // Find the requested shared device

    SharedDevice shareDev = null;

    try {

      // Get/create the shared device

      shareDev = m_sess.getSMBServer().findShare(share.getShareName(),
          servType, m_sess, true);

      // Return a logon failure status

      // outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTLogonFailure,
      // SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      // return endOff;
    } catch (Exception ex) {

      // Log the generic error

      logger.error("Exception in TreeConnectAndX", ex);

      // Return a general status, bad network name

      outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTBadNetName,
          SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
      return endOff;
    }

    // Check if the share is valid

    if (shareDev == null ||
        (servType != ShareType.UNKNOWN && shareDev.getType() != servType)) {

      // Set the error status

      outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTBadNetName,
          SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
      return endOff;
    }

    // TODO here must be security for connection

    int sharePerm = FileAccess.Writeable;

    // Allocate a tree id for the new connection

    TreeConnection tree = null;

    try {

      // Allocate the tree id for this connection

      int treeId = m_sess.addConnection(shareDev);
      outPkt.setTreeId(treeId);

      // Set the file permission that this user has been granted for this share

      tree = m_sess.findTreeConnection(treeId);
      tree.setPermission(sharePerm);

      // create jcr-session for user
      // TODO here must be security block
      Session s = null;
      if (shareDev.getType() == ShareType.DISK) {
        Credentials credentials = new CredentialsImpl("admin", "admin"
            .toCharArray());
        try {
          // obtain session of shared device - workspace and
          if (logger.isDebugEnabled()) {
            logger.debug("Create JCR-session: login admin");
          }
          s = (SessionImpl) (this.m_sess.getSMBServer().getRepository()).login(
              credentials, shareDev.getName());
        } catch (Exception ex) {
          System.err.println(ex);
        }
      }
      tree.setSession(s);

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
        logger.debug("ANDX Tree Connect AndX - Allocated Tree Id = " + treeId);
    } catch (TooManyConnectionsException ex) {

      // Too many connections open at the moment

      outPkt.setError(SMBStatus.SRVNoResourcesAvailable, SMBStatus.ErrSrv);
      return endOff;
    }

    // Build the tree connect response

    outPkt.setAndXParameterCount(endOff, 2);
    outPkt.setAndXParameter(endOff, 0, SMBSrvPacket.NO_ANDX_CMD);
    outPkt.setAndXParameter(endOff, 1, 0);

    // Pack the service type

    int pos = outPkt.getAndXByteOffset(endOff);
    byte[] outBuf = outPkt.getBuffer();
    pos = DataPacker.putString(ShareType.TypeAsService(shareDev.getType()),
        outBuf, pos, true);

    // Determine the filesystem type, for disk shares

    String devType = FileSystem.TypeFAT;
    // Pack the filesystem type

    pos = DataPacker.putString(devType, outBuf, pos, true, outPkt.isUnicode());

    int bytLen = pos - outPkt.getAndXByteOffset(endOff);
    outPkt.setAndXByteCount(endOff, bytLen);

    // Return the new end of packet offset

    return pos;
  }

  /**
   * Process a chained read file request
   * 
   * @param cmdOff
   *          Offset to the chained command within the request packet.
   * @param outPkt
   *          Reply packet.
   * @param endOff
   *          Offset to the current end of the reply packet.
   * @param netFile
   *          File to be read, passed down the chained requests
   * @return New end of reply offset.
   */
  protected final int procChainedReadAndX(int cmdOff, SMBSrvPacket outPkt,
      int endOff, NetworkFile netFile) {
    logger.debug(":procChainedReadAndX");
    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      outPkt.setError(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return endOff;
    }

    // Extract the read file parameters

    long offset = (long) m_smbPkt.getAndXParameterLong(cmdOff, 3); // bottom
    // 32bits of
    // read
    // offset
    offset &= 0xFFFFFFFFL;
    int maxCount = m_smbPkt.getAndXParameter(cmdOff, 5);

    // Check for the NT format request that has the top 32bits of the file
    // offset

    if (m_smbPkt.getAndXParameterCount(cmdOff) == 12) {
      long topOff = (long) m_smbPkt.getAndXParameterLong(cmdOff, 10);
      offset += topOff << 32;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Chained File Read AndX : Size=" + maxCount + " ,Pos=" +
          offset);

    // Read data from the file

    byte[] buf = outPkt.getBuffer();
    int dataPos = 0;
    int rdlen = 0;

    try {

      // Set the returned parameter count so that the byte offset can be
      // calculated

      outPkt.setAndXParameterCount(endOff, 12);
      dataPos = outPkt.getAndXByteOffset(endOff);
      dataPos = DataPacker.wordAlign(dataPos); // align the data buffer

      // Check if the requested data length will fit into the buffer

      int dataLen = buf.length - dataPos;
      if (dataLen < maxCount)
        maxCount = dataLen;

      // Read from the file

      rdlen = JCRDriver.readFile(m_sess, conn, netFile, buf, dataPos, maxCount,
          offset);

      // Return the data block

      outPkt.setAndXParameter(endOff, 0, SMBSrvPacket.NO_ANDX_CMD);
      outPkt.setAndXParameter(endOff, 1, 0);

      outPkt.setAndXParameter(endOff, 2, 0); // bytes remaining, for pipes only
      outPkt.setAndXParameter(endOff, 3, 0); // data compaction mode
      outPkt.setAndXParameter(endOff, 4, 0); // reserved
      outPkt.setAndXParameter(endOff, 5, rdlen); // data length
      outPkt.setAndXParameter(endOff, 6, dataPos -
          RFCNetBIOSProtocol.HEADER_LEN); // offset
      // to
      // data

      // Clear the reserved parameters

      for (int i = 7; i < 12; i++)
        outPkt.setAndXParameter(endOff, i, 0);

      // Set the byte count

      outPkt.setAndXByteCount(endOff, (dataPos + rdlen) -
          outPkt.getAndXByteOffset(endOff));

      // Update the end offset for the new end of packet

      endOff = dataPos + rdlen;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface

      outPkt.setError(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return endOff;
    } catch (java.io.IOException ex) {
    }

    // Return the new end of packet offset

    return endOff;
  }

  /**
   * Process a chained close file request
   * 
   * @param cmdOff
   *          int Offset to the chained command within the request packet.
   * @param outPkt
   *          SMBSrvPacket Reply packet.
   * @param endOff
   *          int Offset to the current end of the reply packet.
   * @return New end of reply offset.
   */
  protected final int procChainedClose(int cmdOff, SMBSrvPacket outPkt,
      int endOff) {
    logger.debug(":procChainedClose");

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      outPkt.setError(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return endOff;
    }

    // Get the file id from the request

    int fid = m_smbPkt.getAndXParameter(cmdOff, 0);
    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      outPkt.setError(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return endOff;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Chained File Close [" + m_smbPkt.getTreeId() + "] fid=" +
          fid);

    // Close the file

    // Indicate that the file has been closed

    netFile.setClosed(true);

    // Clear the returned parameter count and byte count

    outPkt.setAndXParameterCount(endOff, 0);
    outPkt.setAndXByteCount(endOff, 0);

    endOff = outPkt.getAndXByteOffset(endOff) - RFCNetBIOSProtocol.HEADER_LEN;

    // Remove the file from the connections list of open files

    conn.removeFile(fid, getSession());

    // Return the new end of packet offset

    return endOff;
  }

  /**
   * Process the SMB tree connect request.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   * @exception TooManyConnectionsException
   *              Too many concurrent connections on this session.
   */

  protected void procTreeConnectAndX(SMBSrvPacket outPkt)
      throws SMBSrvException, TooManyConnectionsException, java.io.IOException {
    logger.debug("procTreeConnectAndX");
    // Check that the received packet looks like a valid tree connect request

    if (m_smbPkt.checkPacketIsValid(4, 3) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Extract the parameters

    int pwdLen = m_smbPkt.getParameter(3);

    // Initialize the byte area pointer

    m_smbPkt.resetBytePointer();

    // Determine if ASCII or unicode strings are being used

    boolean unicode = m_smbPkt.isUnicode();

    // Extract the password string

    String pwd = null;

    if (pwdLen > 0) {
      byte[] pwdByts = m_smbPkt.unpackBytes(pwdLen);
      pwd = new String(pwdByts);
    }

    // Extract the requested share name, as a UNC path

    String uncPath = m_smbPkt.unpackString(unicode);
    if (uncPath == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Extract the service type string, always seems to be ASCII

    String service = m_smbPkt.unpackString(false);
    if (service == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Convert the service type to a shared device type, client may specify
    // '?????' in which
    // case we ignore the error.

    int servType = ShareType.ServiceAsType(service);
    if (servType == ShareType.UNKNOWN && service.compareTo("?????") != 0) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
      logger.debug("NT Tree Connect AndX - " + uncPath + ", " + service);

    // Parse the requested share name

    String shareName = null;
    String hostName = null;

    if (uncPath.startsWith("\\")) {

      try {
        PCShare share = new PCShare(uncPath);
        shareName = share.getShareName();
        hostName = share.getNodeName();
      } catch (InvalidUNCPathException ex) {
        m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
            SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        return;
      }
    } else
      shareName = uncPath;

    // Map the IPC$ share to the admin pipe type

    if (shareName.compareTo("IPC$") == 0)
      servType = ShareType.ADMINPIPE;

    // Check if the session is a null session, only allow access to the IPC$
    // named pipe share

    // Find the requested shared device

    SharedDevice shareDev = null;

    try {

      // Get/create the shared device

      shareDev = m_sess.getSMBServer().findShare(shareName, servType, m_sess,
          true);

    } catch (Exception ex) {

      // Log the generic error

      logger.error("TreeConnectAndX error", ex);

      // Return a general status, bad network name

      m_sess.sendErrorResponseSMB(SMBStatus.NTBadNetName,
          SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
      return;
    }

    // Check if the share is valid

    if (shareDev == null ||
        (servType != ShareType.UNKNOWN && shareDev.getType() != servType)) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTBadNetName,
          SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
      return;
    }

    int sharePerm = FileAccess.Writeable;

    // Allocate a tree id for the new connection

    int treeId = m_sess.addConnection(shareDev);
    outPkt.setTreeId(treeId);

    // Set the file permission that this user has been granted for this share

    TreeConnection tree = m_sess.findTreeConnection(treeId);
    tree.setPermission(sharePerm);

    // TODO security, errors
    Session s = null;
    if (shareDev.getType() == ShareType.DISK) {
      Credentials credentials = new CredentialsImpl("admin", "admin"
          .toCharArray());
      try {
        // obtain session of shared device - workspace and
        if (logger.isDebugEnabled()) {
          logger.debug("Create JCR-session: login admin");
        }
        s = (SessionImpl) (this.m_sess.getSMBServer().getRepository()).login(
            credentials, shareDev.getName());
      } catch (Exception ex) {
        System.err.println(ex);
      }
    }
    tree.setSession(s);

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
      logger.debug("Tree Connect AndX - Allocated Tree Id = " + treeId +
          ", Permission = " + FileAccess.asString(sharePerm));

    // Build the tree connect response

    outPkt.setParameterCount(3);
    outPkt.setAndXCommand(0xFF); // no chained reply
    outPkt.setParameter(1, 0);
    outPkt.setParameter(2, 0);

    // Pack the service type

    int pos = outPkt.getByteOffset();
    pos = DataPacker.putString(ShareType.TypeAsService(shareDev.getType()),
        m_smbPkt.getBuffer(), pos, true);

    // Determine the filesystem type, for disk shares

    String devType = FileSystem.TypeFAT; // TypeNTFS permits streams, haha

    // Pack the filesystem type

    pos = DataPacker.putString(devType, m_smbPkt.getBuffer(), pos, true, outPkt
        .isUnicode());
    outPkt.setByteCount(pos - outPkt.getByteOffset());

    // Send the response

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * Close a file that has been opened on the server.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procCloseFile(SMBSrvPacket outPkt) throws java.io.IOException,
      SMBSrvException {
    logger.debug(":procCloseFile");
    // Check that the received packet looks like a valid file close request

    if (m_smbPkt.checkPacketIsValid(3, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // TODO implement different closing modes according to FLAGS parameter

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess
          .sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
      return;
    }

    // Get the file id from the request

    int fid = m_smbPkt.getParameter(0);

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File close [" + m_smbPkt.getTreeId() + "] fid=" + fid);

    // Close the file
    try {

      // delete file if it market as delete on close file
      if (netFile.hasDeleteOnClose()) {
        if (netFile instanceof JCRNetworkFile) {

          Node parent = ((JCRNetworkFile) netFile).getNodeRef().getParent();
          ((JCRNetworkFile) netFile).getNodeRef().remove();
          parent.save();

          logger.debug("file [" + netFile.getName() + "] deleted");

        }
      } else if (netFile instanceof JCRNetworkFile) {
        ((JCRNetworkFile) netFile).saveChanges();

        logger.debug("file [" + netFile.getName() + "] save changes");
      }
    } catch (LockException e) {
      // Return an access denied error

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {

      e.printStackTrace();

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }
    // TODO bind closing file with possible adaption layer

    netFile.setClosed(true);

    // Remove the file from the connections list of open files

    conn.removeFile(fid, getSession());

    // Build the close file response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Process a transact2 request. The transact2 can contain many different
   * sub-requests.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procTransact2(SMBSrvPacket outPkt) throws IOException,
      SMBSrvException {
    logger.debug("procTransact2");
    // Check that we received enough parameters for a transact2 request

    if (m_smbPkt.checkPacketIsValid(14, 0) == false) {

      // Not enough parameters for a valid transact2 request

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess
          .sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
      return;
    }

    // Create a transact packet using the received SMB packet

    SMBSrvTransPacket tranPkt = new SMBSrvTransPacket(m_smbPkt.getBuffer());

    // Create a transact buffer to hold the transaction setup, parameter and
    // data blocks

    SrvTransactBuffer transBuf = null;
    int subCmd = tranPkt.getSubFunction();

    if (tranPkt.getTotalParameterCount() == tranPkt.getRxParameterBlockLength() &&
        tranPkt.getTotalDataCount() == tranPkt.getRxDataBlockLength()) {

      // Create a transact buffer using the packet buffer, the entire request is
      // contained in
      // a single
      // packet

      transBuf = new SrvTransactBuffer(tranPkt);
    } else {

      // Create a transact buffer to hold the multiple transact request
      // parameter/data blocks

      transBuf = new SrvTransactBuffer(tranPkt.getSetupCount(), tranPkt
          .getTotalParameterCount(), tranPkt.getTotalDataCount());
      transBuf.setType(tranPkt.getCommand());
      transBuf.setFunction(subCmd);

      // Append the setup, parameter and data blocks to the transaction data

      byte[] buf = tranPkt.getBuffer();

      transBuf.appendSetup(buf, tranPkt.getSetupOffset(), tranPkt
          .getSetupCount() * 2);
      transBuf.appendParameter(buf, tranPkt.getRxParameterBlock(), tranPkt
          .getRxParameterBlockLength());
      transBuf.appendData(buf, tranPkt.getRxDataBlock(), tranPkt
          .getRxDataBlockLength());
    }

    // Set the return data limits for the transaction

    transBuf.setReturnLimits(tranPkt.getMaximumReturnSetupCount(), tranPkt
        .getMaximumReturnParameterCount(), tranPkt.getMaximumReturnDataCount());

    // Check for a multi-packet transaction, for a multi-packet transaction we
    // just acknowledge the receive with an empty response SMB

    if (transBuf.isMultiPacket()) {

      // Save the partial transaction data

      m_sess.setTransaction(transBuf);

      // Send an intermediate acknowedgement response

      m_sess.sendSuccessResponseSMB();
      return;
    }

    // Check if the transaction is on the IPC$ named pipe, the request requires
    // special processing
    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {
      IPCHandler.procTransaction(transBuf, m_sess, outPkt);
      return;
    }

    // DEBUG

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
      logger.debug("Transaction [" + m_smbPkt.getTreeId() + "] tbuf=" +
          transBuf);

    // Process the transaction buffer

    processTransactionBuffer(transBuf, outPkt);
  }

  /**
   * Process a transact2 secondary request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procTransact2Secondary(SMBSrvPacket outPkt)
      throws IOException, SMBSrvException {
    logger.debug("procTransact2Secondary");

    // Check that we received enough parameters for a transact2 request

    if (m_smbPkt.checkPacketIsValid(8, 0) == false) {

      // Not enough parameters for a valid transact2 request

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess
          .sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
      return;
    }

    // Check if there is an active transaction, and it is an NT transaction

    if (m_sess.hasTransaction() == false ||
        (m_sess.getTransaction().isType() == PacketType.Transaction && m_smbPkt
            .getCommand() != PacketType.TransactionSecond) ||
        (m_sess.getTransaction().isType() == PacketType.Transaction2 && m_smbPkt
            .getCommand() != PacketType.Transaction2Second)) {

      // No transaction to continue, or packet does not match the existing
      // transaction, return
      // an error

      m_sess.sendErrorResponseSMB(SMBStatus.SRVNonSpecificError,
          SMBStatus.ErrSrv);
      return;
    }

    // Create an NT transaction using the received packet

    SMBSrvTransPacket tpkt = new SMBSrvTransPacket(m_smbPkt.getBuffer());
    byte[] buf = tpkt.getBuffer();
    SrvTransactBuffer transBuf = m_sess.getTransaction();

    // Append the parameter data to the transaction buffer, if any

    int plen = tpkt.getSecondaryParameterBlockCount();
    if (plen > 0) {

      // Append the data to the parameter buffer

      DataBuffer paramBuf = transBuf.getParameterBuffer();
      paramBuf.appendData(buf, tpkt.getSecondaryParameterBlockOffset(), plen);
    }

    // Append the data block to the transaction buffer, if any

    int dlen = tpkt.getSecondaryDataBlockCount();
    if (dlen > 0) {

      // Append the data to the data buffer

      DataBuffer dataBuf = transBuf.getDataBuffer();
      dataBuf.appendData(buf, tpkt.getSecondaryDataBlockOffset(), dlen);
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
      logger.debug("Transaction Secondary [" + m_smbPkt.getTreeId() +
          "] paramLen=" + plen + ", dataLen=" + dlen);

    // Check if the transaction has been received or there are more sections to
    // be received

    int totParam = tpkt.getTotalParameterCount();
    int totData = tpkt.getTotalDataCount();

    int paramDisp = tpkt.getParameterBlockDisplacement();
    int dataDisp = tpkt.getDataBlockDisplacement();

    if ((paramDisp + plen) == totParam && (dataDisp + dlen) == totData) {

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
        logger.debug("Transaction complete, processing ...");

      // Clear the in progress transaction

      m_sess.setTransaction(null);

      // Check if the transaction is on the IPC$ named pipe, the request
      // requires special
      // processing

      if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {
        IPCHandler.procTransaction(transBuf, m_sess, outPkt);
        return;
      }

      // DEBUG

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
        logger.debug("Transaction second [" + m_smbPkt.getTreeId() + "] tbuf=" +
            transBuf);

      // Process the transaction

      processTransactionBuffer(transBuf, outPkt);
    } else {

      // There are more transaction parameter/data sections to be received,
      // return an
      // intermediate response

      m_sess.sendSuccessResponseSMB();
    }
  }

  /**
   * Process a transaction buffer
   * 
   * @param tbuf
   *          TransactBuffer
   * @param outPkt
   *          SMBSrvPacket
   * @exception IOException
   *              If a network error occurs
   * @exception SMBSrvException
   *              If an SMB error occurs
   */
  private final void processTransactionBuffer(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws IOException, SMBSrvException {
    logger.debug("processTransactionBuffer");

    // Get the transact2 sub-command code and process the request

    switch (tbuf.getFunction()) {

    // Start a file search

    case PacketType.Trans2FindFirst:
      procTrans2FindFirst(tbuf, outPkt);
      break;

    // Continue a file search

    case PacketType.Trans2FindNext:
      procTrans2FindNext(tbuf, outPkt);
      break;

    // Query file system information

    case PacketType.Trans2QueryFileSys:
      procTrans2QueryFileSys(tbuf, outPkt);
      break;

    // Query path

    case PacketType.Trans2QueryPath:
      procTrans2QueryPath(tbuf, outPkt);
      break;

    // Query file information via handle

    case PacketType.Trans2QueryFile:
      procTrans2QueryFile(tbuf, outPkt);
      break;

    // Set file information via handle

    case PacketType.Trans2SetFile:
      procTrans2SetFile(tbuf, outPkt);
      break;

    // Set file information via path

    case PacketType.Trans2SetPath:
      procTrans2SetPath(tbuf, outPkt);
      break;

    // Unknown transact2 command

    default:

      // Return an unrecognized command error

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      break;
    }

  }

  /**
   * Close a search started via the transact2 find first/next command.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected final void procFindClose(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug("procFindClose");
    // Check that the received packet looks like a valid find close request

    if (m_smbPkt.checkPacketIsValid(1, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess
          .sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
      return;
    }

    // Get the search id

    int searchId = m_smbPkt.getParameter(0);

    SearchContext ctx = m_sess.getSearchContext(searchId);

    if (ctx == null) {

      // Invalid search handle

      m_sess.sendSuccessResponseSMB();
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
      logger.debug("Close trans search [" + searchId + "]");

    // Deallocate the search slot, close the search.

    m_sess.deallocateSearchSlot(searchId);

    // Return a success status SMB

    m_sess.sendSuccessResponseSMB();
  }

  /**
   * Process the file lock/unlock request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procLockingAndX(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug("procLockingAndX");
    // TODO not implemented yet
    m_sess.sendSuccessResponseSMB();
  }

  /**
   * Process the logoff request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procLogoffAndX(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procLogoffAndX");
    // Check that the received packet looks like a valid logoff andX request

    if (m_smbPkt.checkPacketIsValid(2, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }
    // Return a success status SMB

    m_sess.sendSuccessResponseSMB();
  }

  /**
   * Process the file open request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procOpenAndX(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {

    logger.debug(":procOpenAndX");
    // Check that the received packet looks like a valid open andX request

    if (m_smbPkt.checkPacketIsValid(15, 1) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is to the IPC$ remote admin named pipe pass the request
    // to the IPC
    // handler. If the device is
    // not a disk type device then return an error.

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

      // Use the IPC$ handler to process the request

      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    } else if (conn.getSharedDevice().getType() != ShareType.DISK) {

      // Return an access denied error

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Extract the open file parameters

    int access = m_smbPkt.getParameter(3);
    int srchAttr = m_smbPkt.getParameter(4);
    int fileAttr = m_smbPkt.getParameter(5);
    int crTime = m_smbPkt.getParameter(6);
    int crDate = m_smbPkt.getParameter(7);
    int openFunc = m_smbPkt.getParameter(8);
    int allocSiz = m_smbPkt.getParameterLong(9);

    // Extract the filename string

    String fileName = m_smbPkt.unpackString(m_smbPkt.isUnicode());
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // logger.debug("\n fileName [" + fileName + "] TEMPLATE");

    // convert name

    if (fileName.equals("")) {
      fileName = "/";
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = path.toString();
    }

    // Check if the file name contains a stream name
    String temp = fileName;
    String stream = null;
    int pos = temp.indexOf(":");
    if (pos == -1) {
      fileName = NameCoder.DecodeName(temp);
    } else {
      fileName = NameCoder.DecodeName(temp.substring(0, pos));
      stream = temp.substring(pos);
    }

    // Create the file open parameters

    SMBDate crDateTime = null;
    if (crTime > 0 && crDate > 0)
      crDateTime = new SMBDate(crDate, crTime);
    logger.debug("openFunction [" + openFunc + "] TEMPORARY");

    FileOpenParams params = new FileOpenParams(fileName, stream, openFunc,
        access, srchAttr, fileAttr, allocSiz, crDateTime.getTime());

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Open AndX [" + m_smbPkt.getTreeId() + "] params=" +
          params);
    logger.debug("is directory " + params.isDirectory() + " TEMPORARY");

    int responseAction = 0x0;
    int fid = -1;
    NetworkFile file = null;

    try {
      boolean isExist = conn.getSession().itemExists(fileName);
      logger.debug("isExist " + isExist + "TEMPORARY");

      if (!isExist) {
        // file/dir not exist
        if (FileAction.createNotExists(openFunc)) {
          // Check if the session has write access to the filesystem

          if (conn.hasWriteAccess() == false) {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied,
                SMBStatus.ErrDos);
            return;
          }

          // Create a new file

          logger.debug("trying to create file TEMPLATE");
          file = JCRDriver.createFile(conn, params);

          // Indicate that the file did not exist and was created

          responseAction = FileAction.FileCreated;

        } else {
          m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound,
              SMBStatus.ErrDos);
          return;
        }
        // if else response action still zero
      } else {
        // file/dir exist
        if ((FileAction.openIfExists(openFunc)) ||
            (FileAction.truncateExistingFile(openFunc))) {
          // Open the requested file
          file = JCRDriver.openFile(conn, params);

          if (FileAction.truncateExistingFile(openFunc))
            responseAction = FileAction.FileTruncated;
          else
            responseAction = FileAction.FileExisted;
        } else {
          m_sess.sendErrorResponseSMB(SMBStatus.DOSFileAlreadyExists,
              SMBStatus.ErrDos);
          return;
        }
      }

      if (file != null) {
        fid = conn.addFile(file, getSession());
      }

    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (LockException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    } catch (TooManyFilesException ex) {
      // Too many files are open on this connection, cannot open any more
      // files.
      m_sess.sendErrorResponseSMB(SMBStatus.DOSTooManyOpenFiles,
          SMBStatus.ErrDos);
      return;
    } catch (Exception e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    logger.debug("resp Action " + FileAction.asString(responseAction) +
        ", fid = " + fid + " TEMPORARY");

    // Build the open file response

    outPkt.setParameterCount(15);

    outPkt.setAndXCommand(0xFF);
    outPkt.setParameter(1, 0); // AndX offset

    outPkt.setParameter(2, fid);

    int attr = (file != null) ? file.getFileAttributes() : 0;
    outPkt.setParameter(3, attr); // file
    // attributes

    SMBDate modDate = null;

    // if (file.hasModifyDate())
    modDate = file != null ? new SMBDate(file.getModifyDate()) : null;

    outPkt.setParameter(4, modDate != null ? modDate.asSMBTime() : 0); // last
    // write
    // time
    outPkt.setParameter(5, modDate != null ? modDate.asSMBDate() : 0); // last
    // write
    // date
    int size = file != null ? file.getFileSizeInt() : 0;
    outPkt.setParameterLong(6, size); // file size
    int acc = file != null ? file.getGrantedAccess() : 0;
    outPkt.setParameter(8, acc);
    outPkt.setParameter(9, OpenAndX.FileTypeDisk);
    outPkt.setParameter(10, 0); // named pipe state
    outPkt.setParameter(11, responseAction);
    outPkt.setParameter(12, 0); // server FID (long)
    outPkt.setParameter(13, 0);
    outPkt.setParameter(14, 0);

    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * Process the file read request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procReadAndX(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procReadAndX");
    // Check that the received packet looks like a valid read andX request

    if (m_smbPkt.checkPacketIsValid(10, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is to the IPC$ remote admin named pipe pass the request
    // to the IPC
    // handler.

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

      // Use the IPC$ handler to process the request

      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    }

    // Extract the read file parameters

    int fid = m_smbPkt.getParameter(2);
    long offset = (long) m_smbPkt.getParameterLong(3); // bottom 32bits of read
    // offset
    offset &= 0xFFFFFFFFL;
    int maxCount = m_smbPkt.getParameter(5);

    // Check for the NT format request that has the top 32bits of the file
    // offset

    if (m_smbPkt.getParameterCount() == 12) {
      long topOff = (long) m_smbPkt.getParameterLong(10);
      offset += topOff << 32;
    }

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
      logger.debug("File Read AndX [" + netFile.getFileId() + "] : Size=" +
          maxCount + " ,Pos=" + offset);

    // Read data from the file

    byte[] buf = outPkt.getBuffer();
    int dataPos = 0;
    int rdlen = 0;

    try {

      // Set the returned parameter count so that the byte offset can be
      // calculated

      outPkt.setParameterCount(12);
      dataPos = outPkt.getByteOffset();
      dataPos = DataPacker.wordAlign(dataPos); // align the data buffer

      // Check if the requested data length will fit into the buffer

      int dataLen = buf.length - dataPos;
      if (dataLen < maxCount)
        maxCount = dataLen;

      // Read from the file

      rdlen = JCRDriver.readFile(m_sess, conn, netFile, buf, dataPos, maxCount,
          offset);
    } catch (AccessDeniedException ex) {

      // User does not have the required access rights or file is not accessible

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (java.io.IOException ex) {

      // Failed to read the file

      m_sess.sendErrorResponseSMB(SMBStatus.HRDReadFault, SMBStatus.ErrHrd);
      return;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Return the data block

    outPkt.setAndXCommand(0xFF); // no chained command
    outPkt.setParameter(1, 0);
    outPkt.setParameter(2, 0); // bytes remaining, for pipes only
    outPkt.setParameter(3, 0); // data compaction mode
    outPkt.setParameter(4, 0); // reserved
    outPkt.setParameter(5, rdlen); // data length
    outPkt.setParameter(6, dataPos - RFCNetBIOSProtocol.HEADER_LEN); // offset
    // to data

    // Clear the reserved parameters

    for (int i = 7; i < 12; i++)
      outPkt.setParameter(i, 0);

    // Set the byte count

    outPkt.setByteCount((dataPos + rdlen) - outPkt.getByteOffset());

    // Check if there is a chained command, or commands

    if (m_smbPkt.hasAndXCommand()) {

      // Process any chained commands, AndX

      int pos = procAndXCommands(outPkt, outPkt.getPacketLength(), netFile);

      // Send the read andX response

      m_sess.sendResponseSMB(outPkt, pos);
    } else {

      // Send the normal read andX response

      m_sess.sendResponseSMB(outPkt);
    }
  }

  /**
   * Rename a file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procRenameFile(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procRenameFile");
    // Check that the received packet looks like a valid rename file request

    if (m_smbPkt.checkPacketIsValid(1, 4) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the Unicode flag

    boolean isUni = m_smbPkt.isUnicode();

    // Read the data block

    m_smbPkt.resetBytePointer();

    // Extract the old file name

    if (m_smbPkt.unpackByte() != DataType.ASCII) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    String oldName = m_smbPkt.unpackString(isUni);
    if (oldName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Extract the new file name

    if (m_smbPkt.unpackByte() != DataType.ASCII) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    String newName = m_smbPkt.unpackString(isUni);
    if (newName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Rename [" + m_smbPkt.getTreeId() + "] old name=" +
          oldName + ", new name=" + newName);

    // Check if the from/to paths are

    if (isValidPath(oldName) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Decode name to jcr mode

    if (oldName.equals("")) {
      // can't rename root node
      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(oldName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      oldName = path.toString();
    }

    // Check if the file name contains a stream name
    String temp = oldName;

    if (temp.indexOf(":") == -1) {
      oldName = NameCoder.DecodeName(temp);
    } else {
      // don't support NT streams
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    if (isValidPath(newName) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    if (newName.equals("")) {
      // can't rename root node
      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(newName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      newName = path.toString();
    }

    // Check if the file name contains a stream name
    temp = newName;
    if (temp.indexOf(":") == -1) {
      newName = NameCoder.DecodeName(temp);
    } else {
      // don't support NT streams
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Access the disk interface and rename the requested file

    try {
      // Rename the requested file
      // check if file for rename exists
      if (conn.getSession().itemExists(oldName)) {

        // checking destination file run with save()

        // Rename/move file
        conn.getSession().move(oldName, newName);
        conn.getSession().save();
      } else {
        // Source file/directory does not exist

        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
            SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
        return;
      }

    } catch (ItemExistsException ex) {

      // Destination file/directory already exists

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision,
          SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException ex) { // useless

      // Not allowed to rename the file/directory

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Build the rename file response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Delete a file.
   * <p>
   * Multiple files may be deleted in response to a single request as
   * SMB_COM_DELETE supports wildcards
   * 
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception IOException
   *              If an network error occurs
   * @exception SMBSrvException
   *              If an SMB error occurs
   */
  protected void procDeleteFile(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procDeleteFile");
    // Check that the received packet looks like a valid file delete request

    if (m_smbPkt.checkPacketIsValid(1, 2) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the Unicode flag

    boolean isUni = m_smbPkt.isUnicode();

    // Read the data block

    m_smbPkt.resetBytePointer();

    // Extract the old file name

    if (m_smbPkt.unpackByte() != DataType.ASCII) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    String fileName = m_smbPkt.unpackString(isUni);
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    if (fileName.equals("")) {
      // root directory can't be delted
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = path.toString();
    }

    // Check if the file name contains a stream name
    String temp = fileName;
    if (temp.indexOf(":") == -1) {
      fileName = NameCoder.DecodeName(temp);
    } else {
      // We don't support NTFS streams
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    }

    // TODO Implement SearchAttributes support

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Delete [" + m_smbPkt.getTreeId() + "] name=" +
          fileName);

    // Access the disk interface and delete the file(s)

    try {
      // Check if file name consist wildcard

      if (WildCard.containsWildcards(fileName)) {
        // TODO this case is unchecked!!! and may be not safe
        // get path before wildcard file name
        int i = fileName.lastIndexOf("/") + 1;
        String wildcardname = fileName.substring(i);
        String path = fileName.substring(0, i);

        Node root = (Node) conn.getSession().getItem(path);

        NodeIterator it = root.getNodes(wildcardname);

        int d = 0; // for debug purposes
        while (it.hasNext()) {
          Node tempref = it.nextNode();
          tempref.remove();
        }
        // save changes
        conn.getSession().save();
        logger.debug(d + " files deleted by [" + wildcardname + "] mask");
      } else {
        // Check if file exists
        if (conn.getSession().itemExists(fileName)) {
          // Delete file
          ((Node) conn.getSession().getItem(fileName)).remove();
          conn.getSession().save();// save change
        } else {
          // file not found
          m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
              SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
          return;
        }
      }

    } catch (AccessDeniedException ex) {

      // Not allowed to delete the file

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Build the delete file response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * Delete a directory.
   * <p>
   * The delete directory message is sent to delete an empty directory. The
   * appropriate Tid and additional pathname are passed. The directory must be
   * empty for it to be deleted.
   * 
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception IOException
   *              If a network error occurs
   * @exception SMBSrvException
   *              If an SMB error occurs
   */
  protected void procDeleteDirectory(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procDeleteDirectory");
    // Check that the received packet looks like a valid delete directory
    // request

    if (m_smbPkt.checkPacketIsValid(0, 2) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the Unicode flag

    boolean isUni = m_smbPkt.isUnicode();

    // Read the data block

    m_smbPkt.resetBytePointer();

    // Extract the old file name

    if (m_smbPkt.unpackByte() != DataType.ASCII) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    String dirName = m_smbPkt.unpackString(isUni);
    if (dirName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    if (dirName.equals("")) {
      // root directory can't be delted
      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(dirName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      dirName = path.toString();
    }

    // Check if the file name contains a stream name
    String temp = dirName;
    if (temp.indexOf(":") == -1) {
      dirName = NameCoder.DecodeName(temp);
    } else {
      // We don't support NTFS streams
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    }
    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Directory Delete [" + m_smbPkt.getTreeId() + "] name=" +
          dirName);

    try {

      // Check that directory exists
      if (conn.getSession().itemExists(dirName)) {
        Node dirNode = (Node) conn.getSession().getItem(dirName);

        // Check if directory is empty
        if (dirNode.getNodes().hasNext()) {
          // Directory not empty

          m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryNotEmpty,
              SMBStatus.ErrDos);
          return;
        } else {
          // FINALY. Delete empty directory
          dirNode.remove();
          conn.getSession().save();
        }

      } else {
        // directory not exists
        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
            SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      }

    } catch (AccessDeniedException ex) {

      // Not allowed to delete the directory

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (LockException ex) {

      // lock conflict

      m_sess.sendErrorResponseSMB(SMBStatus.NTLockConflict,
          SMBStatus.DOSLockConflict, SMBStatus.ErrDos);
      return;
    } catch (java.io.IOException ex) {

      // Failed to delete the directory

      m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid,
          SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Internal JCR errror

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Build the delete directory response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * Process a transact2 file search request.
   * 
   * @param tbuf
   *          Transaction request details
   * @param outPkt
   *          Packet to use for the reply.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected final void procTrans2FindFirst(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2FindFirst");

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(tbuf.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess
          .sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
      return;
    }

    // Get the search parameters

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int srchAttr = paramBuf.getShort(); // Search Attributes
    int maxFiles = paramBuf.getShort(); // Search Count
    int srchFlag = paramBuf.getShort(); // Search Flags
    int infoLevl = paramBuf.getShort(); // Information Level
    paramBuf.skipBytes(4); // here is unusable SearchStorageType

    String srchPath = paramBuf.getString(tbuf.isUnicode()); // search pattern

    // Decode file names to jcr mode
    StringBuilder path = new StringBuilder(srchPath);
    for (int i = 0; i < path.length(); i++) {

      // Convert forward slashes to back slashes

      if (path.charAt(i) == '\\')
        path.setCharAt(i, '/');
    }
    srchPath = NameCoder.DecodeName(path.toString());

    // Check if the search path is valid

    if (srchPath == null || srchPath.length() == 0) {

      // Invalid search request

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    } else if (srchPath.endsWith("/")) {

      // Make the search a wildcard search

      srchPath = srchPath + "*"; // "*.*"
    }

    // Check for the Macintosh information level, if the Macintosh extensions
    // are not enabled return an error

    if (infoLevl == FindInfoPacker.InfoMacHfsInfo &&
        getSession().hasMacintoshExtensions() == false) {

      // Return an error status, Macintosh extensions are not enabled

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
      return;
    }

    // TODO Check if the search path is valid
    // m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
    // SMBStatus.DOSInvalidData, SMBStatus.ErrDos);

    // TODO Check if the search is for an NTFS stream

    // m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
    // SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);

    // Access the shared device disk interface

    SearchContext ctx = null;

    int searchId = -1;
    boolean wildcardSearch = false;

    try {

      // Allocate a search slot for the new search

      searchId = m_sess.allocateSearchSlot();
      if (searchId == -1) {

        // Failed to allocate a slot for the new search

        m_sess.sendErrorResponseSMB(SMBStatus.SRVNoResourcesAvailable,
            SMBStatus.ErrSrv);
        return;
      }

      // Check if this is a wildcard search or single file search

      if (WildCard.containsWildcards(srchPath) ||
          WildCard.containsUnicodeWildcard(srchPath))
        wildcardSearch = true;

      // Check if the search contains Unicode wildcards

      if (tbuf.isUnicode() && WildCard.containsUnicodeWildcard(srchPath)) {

        // Translate the Unicode wildcards to standard DOS wildcards

        srchPath = WildCard.convertUnicodeWildcardToDOS(srchPath);

        // Debug

        if (logger.isDebugEnabled() &&
            m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("Converted Unicode wildcards to:" + srchPath);
      }

      // Start a new search

      ctx = JCRDriver.startSearch(conn, srchPath, srchAttr);
      if (ctx != null) {

        // Store details of the search in the context

        ctx.setTreeId(m_smbPkt.getTreeId());
        ctx.setMaximumFiles(maxFiles);
      } else {

        // Failed to start the search, return a no more files error

        m_sess.sendErrorResponseSMB(SMBStatus.NTNoSuchFile,
            SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
        return;
      }

      // Save the search context

      m_sess.setSearchContext(searchId, ctx);

      // Create the reply transact buffer

      SrvTransactBuffer replyBuf = new SrvTransactBuffer(tbuf);
      DataBuffer dataBuf = replyBuf.getDataBuffer();

      // Determine the maximum return data length

      int maxLen = replyBuf.getReturnDataLimit();

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
        logger.debug("Start trans search [" + searchId + "] - " + srchPath +
            ", attr=0x" + Integer.toHexString(srchAttr) + ", maxFiles=" +
            maxFiles + ", maxLen=" + maxLen + ", infoLevel=" + infoLevl +
            ", flags=0x" + Integer.toHexString(srchFlag));

      // Loop until we have filled the return buffer or there are no more files
      // to return

      int fileCnt = 0;
      int packLen = 0;
      int lastNameOff = 0;

      // Flag to indicate if resume ids should be returned

      boolean resumeIds = false;
      if (infoLevl == FindInfoPacker.InfoStandard &&
          (srchFlag & FindFirstNext.ReturnResumeKey) != 0) {

        // Windows servers only seem to return resume keys for the standard
        // information level

        resumeIds = true;
      }

      // If this is a wildcard search then add the '.' and '..' entries

      if (wildcardSearch == true && ReturnDotFiles == true) {

        // Pack the '.' file information

        if (resumeIds == true) {
          dataBuf.putInt(-1);
          maxLen -= 4;
        }

        lastNameOff = dataBuf.getPosition();
        FileInfo dotInfo = new FileInfo(".", 0, FileAttribute.Directory);
        dotInfo.setFileId(dotInfo.getFileName().hashCode());

        packLen = FindInfoPacker.packInfo(dotInfo, dataBuf, infoLevl, tbuf
            .isUnicode());

        // Update the file count for this packet, update the remaining buffer
        // length

        fileCnt++;
        maxLen -= packLen;

        // Pack the '..' file information

        if (resumeIds == true) {
          dataBuf.putInt(-2);
          maxLen -= 4;
        }

        lastNameOff = dataBuf.getPosition();
        dotInfo.setFileName("..");
        dotInfo.setFileId(dotInfo.getFileName().hashCode());

        packLen = FindInfoPacker.packInfo(dotInfo, dataBuf, infoLevl, tbuf
            .isUnicode());

        // Update the file count for this packet, update the remaining buffer
        // length

        fileCnt++;
        maxLen -= packLen;
      }

      boolean pktDone = false;
      boolean searchDone = false;

      FileInfo info = new FileInfo();

      while (pktDone == false && fileCnt < maxFiles) {

        // Get file information from the search

        if (ctx.nextFileInfo(info) == false) {

          // No more files

          pktDone = true;
          searchDone = true;
        }

        // Check if the file information will fit into the return buffer

        else if (FindInfoPacker.calcInfoSize(info, infoLevl, false, true) <= maxLen) {

          // Pack the resume id, if required

          if (resumeIds == true) {
            dataBuf.putInt(ctx.getResumeId());
            maxLen -= 4;
          }

          // Save the offset to the last file information structure

          lastNameOff = dataBuf.getPosition();

          // Pack the file information

          packLen = FindInfoPacker.packInfo(info, dataBuf, infoLevl, tbuf
              .isUnicode());

          // Update the file count for this packet

          fileCnt++;

          // Recalculate the remaining buffer space

          maxLen -= packLen;
        } else {

          // Set the search restart point

          ctx.rollbackAtOnePosition();

          // No more buffer space

          pktDone = true;
        }
      }

      // Check for a single file search and the file was not found, in this case
      // return an error status

      if (wildcardSearch == false && fileCnt == 0)
        throw new FileNotFoundException(srchPath);

      // Check for a search where the maximum files is set to one, close the
      // search immediately.

      if (maxFiles == 1 && fileCnt == 1)
        searchDone = true;

      // Clear the next structure offset, if applicable

      FindInfoPacker.clearNextOffset(dataBuf, infoLevl, lastNameOff);

      // Pack the parameter block

      paramBuf = replyBuf.getParameterBuffer();

      paramBuf.putShort(searchId);
      paramBuf.putShort(fileCnt);
      paramBuf.putShort(ctx.hasMoreFiles() ? 0 : 1);
      paramBuf.putShort(0);
      paramBuf.putShort(lastNameOff);

      // Send the transaction response

      SMBSrvTransPacket tpkt = new SMBSrvTransPacket(outPkt.getBuffer());
      tpkt.doTransactionResponse(m_sess, replyBuf);

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
        logger.debug("Search [" + searchId + "] Returned " + fileCnt +
            " files, dataLen=" + dataBuf.getLength() + ", moreFiles=" +
            ctx.hasMoreFiles());

      // Check if the search is complete

      if (searchDone == true || ctx.hasMoreFiles() == false) {
        // Debug

        if (logger.isDebugEnabled() &&
            m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("End start search [" + searchId + "] (Search complete)");

        // Release the search context

        m_sess.deallocateSearchSlot(searchId);
      } else if ((srchFlag & FindFirstNext.CloseSearch) != 0) {
        // Debug

        if (logger.isDebugEnabled() &&
            m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("End start search [" + searchId + "] (Close)");

        // Release the search context

        m_sess.deallocateSearchSlot(searchId);
      }
    } catch (FileNotFoundException ex) {

      // Search path does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.NTNoSuchFile,
          SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
    } catch (PathNotFoundException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Requested path does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectPathNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);

    } catch (UnsupportedInfoLevelException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Requested information level is not supported

      m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
    }

  }

  /**
   * Process a transact2 file search continue request.
   * 
   * @param tbuf
   *          Transaction request details
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected final void procTrans2FindNext(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2FindNext");

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(tbuf.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the search parameters

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int searchId = paramBuf.getShort();
    int maxFiles = paramBuf.getShort();
    int infoLevl = paramBuf.getShort();
    paramBuf.getInt();
    int srchFlag = paramBuf.getShort();

    String resumeName = paramBuf.getString(tbuf.isUnicode());

    // Access the shared device disk interface

    SearchContext ctx = null;

    try {
      // Retrieve the search context

      ctx = m_sess.getSearchContext(searchId);
      if (ctx == null) {

        // DEBUG

        if (logger.isDebugEnabled() &&
            m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("Search context null - [" + searchId + "]");

        // Invalid search handle

        m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
        return;
      }

      // Create the reply transaction buffer

      SrvTransactBuffer replyBuf = new SrvTransactBuffer(tbuf);
      DataBuffer dataBuf = replyBuf.getDataBuffer();

      // Determine the maximum return data length

      int maxLen = replyBuf.getReturnDataLimit();

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
        logger.debug("Continue search [" + searchId + "] - " + resumeName +
            ", maxFiles=" + maxFiles + ", maxLen=" + maxLen + ", infoLevel=" +
            infoLevl + ", flags=0x" + Integer.toHexString(srchFlag));

      // Loop until we have filled the return buffer or there are no more files
      // to return

      int fileCnt = 0;
      int packLen = 0;
      int lastNameOff = 0;

      // Flag to indicate if resume ids should be returned

      boolean resumeIds = false;
      if (infoLevl == FindInfoPacker.InfoStandard &&
          (srchFlag & FindFirstNext.ReturnResumeKey) != 0) {

        // Windows servers only seem to return resume keys for the standard
        // information level

        resumeIds = true;
      }

      // Flags to indicate packet full or search complete

      boolean pktDone = false;
      boolean searchDone = false;

      FileInfo info = new FileInfo();

      while (pktDone == false && fileCnt < maxFiles) {

        // Get file information from the search

        if (ctx.nextFileInfo(info) == false) {

          // No more files

          pktDone = true;
          searchDone = true;
        }

        // Check if the file information will fit into the return buffer

        else if (FindInfoPacker.calcInfoSize(info, infoLevl, false, true) <= maxLen) {

          // Pack the resume id, if required

          if (resumeIds == true) {
            dataBuf.putInt(ctx.getResumeId());
            maxLen -= 4;
          }

          // Save the offset to the last file information structure

          lastNameOff = dataBuf.getPosition();

          // Pack the file information

          packLen = FindInfoPacker.packInfo(info, dataBuf, infoLevl, tbuf
              .isUnicode());

          // Update the file count for this packet

          fileCnt++;

          // Recalculate the remaining buffer space

          maxLen -= packLen;
        } else {

          // Set the search restart point

          ctx.rollbackAtOnePosition();

          // No more buffer space

          pktDone = true;
        }
      }

      // Pack the parameter block

      paramBuf = replyBuf.getParameterBuffer();

      paramBuf.putShort(fileCnt);
      paramBuf.putShort(ctx.hasMoreFiles() ? 0 : 1);
      paramBuf.putShort(0);
      paramBuf.putShort(lastNameOff);

      // Send the transaction response

      SMBSrvTransPacket tpkt = new SMBSrvTransPacket(outPkt.getBuffer());
      tpkt.doTransactionResponse(m_sess, replyBuf);

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
        logger.debug("Search [" + searchId + "] Returned " + fileCnt +
            " files, dataLen=" + dataBuf.getLength() + ", moreFiles=" +
            ctx.hasMoreFiles());

      // Check if the search is complete

      if (searchDone == true || ctx.hasMoreFiles() == false) {

        // Debug

        if (logger.isDebugEnabled() &&
            m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("End start search [" + searchId + "] (Search complete)");

        // Release the search context

        m_sess.deallocateSearchSlot(searchId);
      } else if ((srchFlag & FindFirstNext.CloseSearch) != 0) {
        // Debug

        if (logger.isDebugEnabled() &&
            m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("End start search [" + searchId + "] (Close)");

        // Release the search context

        m_sess.deallocateSearchSlot(searchId);
      }
    } catch (FileNotFoundException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Search path does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
    } catch (PathNotFoundException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Requested path does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectPathNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
    } catch (UnsupportedInfoLevelException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Requested information level is not supported

      m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
    }

  }

  /**
   * Process a transact2 file system query request.
   * 
   * @param tbuf
   *          Transaction request details
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected final void procTrans2QueryFileSys(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2QueryFileSys");

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the query file system required information level

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int infoLevl = paramBuf.getShort();

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
      logger.debug("Query File System Info - level = 0x" +
          Integer.toHexString(infoLevl));

    // Access the shared device disk interface

    try {

      // Set the return parameter count, so that the data area position can be
      // calculated.

      outPkt.setParameterCount(10);

      // Pack the disk information into the data area of the transaction reply

      byte[] buf = outPkt.getBuffer();
      int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
      int dataPos = prmPos; // no parameters returned

      // Create a data buffer using the SMB packet. The response should always
      // fit into a single reply packet.

      DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

      // Determine the information level requested

      DiskInfo diskInfo = null;
      VolumeInfo volInfo = null;

      switch (infoLevl) {

      // Standard disk information

      case DiskInfoPacker.InfoStandard: // 1

        // Information format :-
        // ULONG File system identifier, if server is NTServer - 0
        // ULONG Sectors per allocation unit.
        // ULONG Total allocation units.
        // ULONG Total available allocation units.
        // USHORT Number of bytes per sector.

        // Pack the disk information into the return data packet

        // Pack the file system identifier, 0 = NT server

        replyBuf.putZeros(4);

        // Pack the disk unit information
        // FAT32 for 16-32 Gb

        replyBuf.putInt(32);
        replyBuf.putInt(2560000);
        replyBuf.putInt(2304000);
        replyBuf.putShort(512);

        break;

      // Volume label information

      case DiskInfoPacker.InfoVolume: // 2

        // Information format :-
        // ULONG Volume serial number
        // UCHAR Volume label length
        // STRING Volume label

        // Pack the volume serial number

        replyBuf.putZeros(4); // serial number = 0

        // Pack the volume label length and string
        String label = conn.getSharedDevice().getName();
        replyBuf.putByte(label.length());
        replyBuf.putString(label, tbuf.isUnicode());
        break;

      // Full volume information

      case DiskInfoPacker.InfoFsVolume: // 102

        // Pack the volume information

        // Information format :-
        // ULONG Volume creation date/time (NT 64bit time fomat)
        // UINT Volume serial number
        // UINT Volume label length
        // SHORT Reserved
        // STRING Volume label (no null)

        replyBuf.putZeros(8); // Creation DateTime = 0

        replyBuf.putZeros(4); // Serial number = 0

        // TODO check unicode/ascii writing

        int len = conn.getSharedDevice().getName().length();
        if (tbuf.isUnicode())
          len *= 2;
        replyBuf.putInt(len);

        replyBuf.putZeros(2); // reserved 2 bytes
        replyBuf.putString(conn.getSharedDevice().getName(), tbuf.isUnicode(),
            false);

        break;

      // Filesystem size information

      case DiskInfoPacker.InfoFsSize: // 103

        // Pack the disk information into the return data packet

        // Information format :-
        // ULONG Disk size (in units)
        // ULONG Free size (in units)
        // UINT Unit size in blocks
        // UINT Block size in bytes

        replyBuf.putLong(2560000);
        replyBuf.putLong(2304000);
        replyBuf.putInt(32);
        replyBuf.putInt(512);
        break;

      // Filesystem device information

      case DiskInfoPacker.InfoFsDevice: // 104

        // Information format :-
        // UINT Device type look NTIOCtl
        // UINT Characteristics

        replyBuf.putInt(0x0007);// NTIOCtl.DeviceDisk);
        replyBuf.putInt(0x0007); // FILE_VIRTUAL_VOLUME

        break;

      // Filesystem attribute information

      case DiskInfoPacker.InfoFsAttribute: // 105

        String fsType = "NTFS"; // NTFS or FAT
        // Pack the filesystem type

        // Information format :-
        // UINT Attribute flags
        // UINT Maximum filename component length (usually 255)
        // UINT Filesystem type length
        // STRING Filesystem type string

        replyBuf.putInt(FileSystem.UnicodeOnDisk); // TODO put real attributes
        replyBuf.putInt(255);

        if (tbuf.isUnicode())
          replyBuf.putInt(fsType.length() * 2);
        else
          replyBuf.putInt(fsType.length());
        replyBuf.putString(fsType, tbuf.isUnicode(), false);

        break;

      // Mac filesystem information

      case DiskInfoPacker.InfoMacFsInfo:

        // Check if the filesystem supports NTFS streams
        //
        // We should only return a valid response to the Macintosh information
        // level if the
        // filesystem
        // does NOT support NTFS streams. By returning an error status the
        // Thursby DAVE
        // software will treat
        // the filesystem as a WinXP/2K filesystem with full streams support.

        boolean ntfs = false;

        // If the filesystem does not support NTFS streams then send a valid
        // response.

        if (ntfs == false) {

          // Pack the disk information into the return data packet

          DiskInfoPacker
              .packMacFsInformation(diskInfo, volInfo, ntfs, replyBuf);
          // Information format :-
          // LARGE_INTEGER Volume creation time (NT format)
          // LARGE_INTEGER Volume modify time (NT format)
          // LARGE_INTEGER Volume backup time (NT format)
          // ULONG Allocation blocks
          // ULONG Allocation block size (multiple of 512)
          // ULONG Free blocks on the volume
          // UCHAR[32] Finder info
          // LONG Number of files in root directory (zero if unknown)
          // LONG Number of directories in the root directory (zero if unknown)
          // LONG Number of files on the volume (zero if unknown)
          // LONG Number of directories on the volume (zero if unknown)
          // LONG Mac support flags (big endian)

          // Pack the volume creation time

          replyBuf.putZeros(24);

          // Pack the number of allocation blocks, block size and free block
          // count

          replyBuf.putInt((int) 2560000);
          replyBuf.putInt(512 * 32);
          replyBuf.putInt((int) 2304000);

          // Pack the finder information area

          replyBuf.putZeros(32);

          // Pack the file/directory counts

          replyBuf.putInt(0);
          replyBuf.putInt(0);
          replyBuf.putInt(0);
          replyBuf.putInt(0);

          // Pack the Mac support flags

          DataPacker.putIntelInt(ntfs ? 0
              : DiskInfoPacker.MacNoStreamsOrMacSupport, replyBuf.getBuffer(),
              replyBuf.getPosition());
          replyBuf.setPosition(replyBuf.getPosition() + 4);

        }
        break;

      // Filesystem size information, including per user allocation limit

      case DiskInfoPacker.InfoFullFsSize:

        long userLimit = diskInfo.getTotalUnits();

        // Pack the disk information into the return data packet

        // Information format :-
        // ULONG Disk size (in units)
        // ULONG User free size (in units)
        // ULONG Free size (in units)
        // UINT Unit size in blocks
        // UINT Block size in bytes

        replyBuf.putLong(2560000);
        replyBuf.putLong(2560000);
        replyBuf.putLong(2304000);
        replyBuf.putInt(32);
        replyBuf.putInt(512);
        break;
      }

      // Check if any data was packed, if not then the information level is not
      // supported

      if (replyBuf.getPosition() == dataPos) {
        m_sess
            .sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
        return;
      }

      int bytCnt = replyBuf.getPosition() - outPkt.getByteOffset();
      replyBuf.setEndOfBuffer();
      int dataLen = replyBuf.getLength();
      SMBSrvTransPacket.initTransactReply(outPkt, 0, prmPos, dataLen, dataPos);
      outPkt.setByteCount(bytCnt);

      // Send the transact reply

      m_sess.sendResponseSMB(outPkt);
    } catch (Exception ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }
  }

  /**
   * Process a transact2 query path information request.
   * 
   * @param tbuf
   *          Transaction request details
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected final void procTrans2QueryPath(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2QueryPath");

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the query path information level and file/directory name

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int infoLevl = paramBuf.getShort();
    paramBuf.skipBytes(4);

    String path = paramBuf.getString(tbuf.isUnicode());

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
      logger.debug("Query Path - level = 0x" + Integer.toHexString(infoLevl) +
          ", path = " + path);

    // Check if the smb file name is valid

    if (isValidPath(path) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // check if not NTFS stream
    if (path.indexOf(FileOpenParams.StreamSeparator) != -1) {
      // NTFS streams not supported, return an error status

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,// .NTObjectNameInvalid,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    }

    // convert name
    if (path.equals("")) {
      path = "/";
    } else {
      // Convert slashes
      StringBuffer pathBuf = new StringBuffer(path);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (pathBuf.charAt(i) == '\\')
          pathBuf.setCharAt(i, '/');
      }

      path = NameCoder.DecodeName(pathBuf.toString());
    }

    // temp
    logger.debug("after encoding : [" + path + "]");
    // Access the shared device disk interface

    try {

      // Set the return parameter count, so that the data area position can be
      // calculated.

      outPkt.setParameterCount(10);

      // Pack the file information into the data area of the transaction reply

      byte[] buf = outPkt.getBuffer();
      int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
      int dataPos = prmPos + 4;

      // Pack the return parametes, EA error offset

      outPkt.setPosition(prmPos);
      outPkt.packWord(0);

      // Create a data buffer using the SMB packet. The response should always
      // fit into a
      // single
      // reply packet.

      DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

      // Check for the file streams information level

      int dataLen = 0;

      FileInfo fileInfo = null;
      // Get the file information
      Session sess = conn.getSession();

      if (sess.itemExists(path)) {

        fileInfo = JCRDriver.getFileInformation(((Node) conn.getSession()
            .getItem(path)));
      } else {

        // if file/directory not exist send error
        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
            SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
        return;
      }

      // Pack the file information into the return data packet

      dataLen = QueryInfoPacker.packInfo(fileInfo, replyBuf, infoLevl, true);

      // Check if any data was packed, if not then the information level is not
      // supported

      if (dataLen == 0) {
        m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
            SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        return;
      }

      SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, dataLen, dataPos);
      outPkt.setByteCount(replyBuf.getPosition() - outPkt.getByteOffset());

      // Send the transact reply

      m_sess.sendResponseSMB(outPkt);
    } catch (AccessDeniedException ex) {

      // Not allowed to access the file/folder

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
      // } catch (FileNotFoundException ex) {

      // Requested file does not exist

      // m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
      // SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      // return;
    } catch (PathNotFoundException ex) {

      // Requested path does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectPathNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    } catch (UnsupportedInfoLevelException ex) {

      // Requested information level is not supported

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }
  }

  /**
   * Process a transact2 query file information (via handle) request.
   * 
   * @param tbuf
   *          Transaction request details
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              SMB protocol exception
   */
  protected final void procTrans2QueryFile(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2QueryFile");

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the file id and query path information level

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int fid = paramBuf.getShort();
    int infoLevl = paramBuf.getShort();

    // Get the file details via the file id

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
      logger.debug("Query File - level=0x" + Integer.toHexString(infoLevl) +
          ", fid=" + fid + ", stream=" + netFile.getStreamId() + ", name=" +
          netFile.getFullName());

    // Access the shared device disk interface

    try {

      // Set the return parameter count, so that the data area position can be
      // calculated.

      outPkt.setParameterCount(10);

      // Pack the file information into the data area of the transaction reply

      byte[] buf = outPkt.getBuffer();
      int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
      int dataPos = prmPos + 4;

      // Pack the return parametes, EA error offset

      outPkt.setPosition(prmPos);
      outPkt.packWord(0);

      // Create a data buffer using the SMB packet. The response should always
      // fit into a
      // single
      // reply packet.

      DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

      // Check if the virtual filesystem supports streams, and streams are
      // enabled

      // Check for the file streams information level

      int dataLen = 0;

      // Get the file information
      FileInfo fileInfo = null;

      fileInfo = JCRDriver.getFileInformation(((JCRNetworkFile) netFile)
          .getNodeRef());

      if (fileInfo == null) {

        logger.debug(" file info null");
        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
            SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        return;
      }

      // Pack the file information into the return data packet

      dataLen = QueryInfoPacker.packInfo(fileInfo, replyBuf, infoLevl, true);

      // Check if any data was packed, if not then the information level is not
      // supported

      if (dataLen == 0) {
        m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
            SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        return;
      }

      SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, dataLen, dataPos);
      outPkt.setByteCount(replyBuf.getPosition() - outPkt.getByteOffset());

      // Send the transact reply

      m_sess.sendResponseSMB(outPkt);
    } catch (AccessDeniedException ex) {

      // Not allowed to access the file/folder

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (FileNotFoundException ex) {

      // Requested file does not exist
      ex.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (PathNotFoundException ex) {

      // Requested path does not exist
      ex.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectPathNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    } catch (UnsupportedInfoLevelException ex) {

      // Requested information level is not supported

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }
  }

  /**
   * Process a transact2 set file information (via handle) request.
   * 
   * @param tbuf
   *          Transaction request details
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              SMB protocol exception
   */
  protected final void procTrans2SetFile(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2SetFile");

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the file id and information level

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int fid = paramBuf.getShort();
    int infoLevl = paramBuf.getShort();

    // Get the file details via the file id

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
      logger.debug("Set File - level=0x" + Integer.toHexString(infoLevl) +
          ", fid=" + fid + ", name=" + netFile.getFullName());

    // Access the shared device disk interface

    try {

      // Process the set file information request

      DataBuffer dataBuf = tbuf.getDataBuffer();
      FileInfo finfo = null;

      switch (infoLevl) {

      // Set basic file information (dates/attributes)

      case FileInfoLevel.SetBasicInfo:

        // Create the file information template

        int setFlags = 0;
        finfo = new FileInfo(netFile.getFullName(), 0, -1);

        // Set the creation date/time, if specified

        long timeNow = System.currentTimeMillis();

        long nttim = dataBuf.getLong();
        boolean hasSetTime = false;

        if (nttim != 0L) {
          if (nttim != -1L) {
            finfo.setCreationDateTime(NTTime.toJavaDate(nttim));
            setFlags += FileInfo.SetCreationDate;
          }
          hasSetTime = true;
        }

        // Set the last access date/time, if specified

        nttim = dataBuf.getLong();

        if (nttim != 0L) {
          if (nttim != -1L) {
            finfo.setAccessDateTime(NTTime.toJavaDate(nttim));
            setFlags += FileInfo.SetAccessDate;
          } else {
            finfo.setAccessDateTime(timeNow);
            setFlags += FileInfo.SetAccessDate;
          }
          hasSetTime = true;
        }

        // Set the last write date/time, if specified

        nttim = dataBuf.getLong();

        if (nttim > 0L) {
          if (nttim != -1L) {
            finfo.setModifyDateTime(NTTime.toJavaDate(nttim));
            setFlags += FileInfo.SetModifyDate;
          } else {
            finfo.setModifyDateTime(timeNow);
            setFlags += FileInfo.SetModifyDate;
          }
          hasSetTime = true;
        }

        // Set the modify date/time, if specified

        nttim = dataBuf.getLong();

        if (nttim > 0L) {
          if (nttim != -1L) {
            finfo.setChangeDateTime(NTTime.toJavaDate(nttim));
            setFlags += FileInfo.SetChangeDate;
          }
          hasSetTime = true;
        }

        // Set the attributes

        int attr = dataBuf.getInt();
        int unknown = dataBuf.getInt();

        if (hasSetTime == false && unknown == 0) {
          finfo.setFileAttributes(attr);
          setFlags += FileInfo.SetAttributes;
        }

        // Set the file information for the specified file/directory

        finfo.setFileInformationFlags(setFlags);
        JCRDriver
            .setFileInformation(m_sess, conn, netFile.getFullName(), finfo);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
          logger.debug("  Set Basic Info [" + treeId + "] name=" +
              netFile.getFullName() + ", attr=0x" + Integer.toHexString(attr) +
              ", setTime=" + hasSetTime + ", setFlags=0x" +
              Integer.toHexString(setFlags) + ", unknown=" + unknown);
        break;

      // Set end of file position for a file

      case FileInfoLevel.SetEndOfFileInfo:

        // Get the new end of file position

        long eofPos = dataBuf.getLong();

        // Set the new end of file position

        if (netFile.isDirectory())
          throw new AccessDeniedException();
        try{ 
        ((JCRNetworkFile) netFile).truncateFile(eofPos);
        
        }catch(Throwable e){
          e.printStackTrace();
        }
        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
          logger.debug("  Set end of file position fid=" + fid + ", eof=" +
              eofPos);
        break;

      // Set the allocation size for a file

      case FileInfoLevel.SetAllocationInfo:

        // Get the new end of file position

        long allocSize = dataBuf.getLong();

        // Set the new end of file position

        if (netFile.isDirectory())
          throw new AccessDeniedException();

        ((JCRNetworkFile) netFile).truncateFile(allocSize);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
          logger.debug("  Set allocation size fid=" + fid + ", allocSize=" +
              allocSize);
        break;

      // Rename a stream

      case FileInfoLevel.NTFileRenameInfo:

        // Check if the virtual filesystem supports streams, and streams are
        // enabled

        boolean streams = false;

        // If streams are not supported or are not enabled then return an error
        // status

        if (streams == false) {

          // Return a not supported error status

          m_sess.sendErrorResponseSMB(SMBStatus.NTNotSupported,
              SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
          return;
        }

        break;

      // Mark or unmark a file/directory for delete

      case FileInfoLevel.SetDispositionInfo:
      case FileInfoLevel.NTFileDispositionInfo:

        // Get the delete flag

        int flag = dataBuf.getByte();
        boolean delFlag = flag == 1 ? true : false;

        // Call the filesystem driver set file information to see if the file
        // can be marked
        // for
        // delete.

        FileInfo delInfo = new FileInfo();
        delInfo.setDeleteOnClose(delFlag);
        delInfo.setFileInformationFlags(FileInfo.SetDeleteOnClose);

        JCRDriver.setFileInformation(m_sess, conn, netFile.getFullName(),
            delInfo);

        // Mark/unmark the file/directory for deletion

        netFile.setDeleteOnClose(delFlag);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
          logger.debug("  Set file disposition fid=" + fid + ", name=" +
              netFile.getName() + ", delete=" + delFlag);
        break;
      }

      // Set the return parameter count, so that the data area position can be
      // calculated.

      outPkt.setParameterCount(10);

      // Pack the return information into the data area of the transaction reply

      byte[] buf = outPkt.getBuffer();
      int prmPos = outPkt.getByteOffset();

      // Longword align the parameters, return an unknown word parameter
      //
      // Note: Make sure the data offset is on a longword boundary, NT has
      // problems if this is
      // not done

      prmPos = DataPacker.longwordAlign(prmPos);
      DataPacker.putIntelShort(0, buf, prmPos);

      SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, 0, prmPos + 4);
      outPkt.setByteCount((prmPos - outPkt.getByteOffset()) + 4);

      // Send the transact reply

      m_sess.sendResponseSMB(outPkt);

      // Check if there are any file/directory change notify requests active
    } catch (FileNotFoundException ex) {

      // Requested file does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException ex) {

      // Not allowed to change file attributes/settings

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface
      ex.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    } catch (Exception ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }
  }

  /**
   * Process a transact2 set path information request.
   * 
   * @param tbuf
   *          Transaction request details
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              SMB protocol exception
   */
  protected final void procTrans2SetPath(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2SetPath");

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the path and information level

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int infoLevl = paramBuf.getShort();
    paramBuf.skipBytes(4);

    String path = paramBuf.getString(tbuf.isUnicode());

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
      logger.debug("Set Path - path=" + path + ", level=0x" +
          Integer.toHexString(infoLevl));

    // Check if the file name is valid

    if (isValidPath(path) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid,
          SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Access the shared device disk interface

    try {

      // Process the set file information request

      DataBuffer dataBuf = tbuf.getDataBuffer();
      FileInfo finfo = null;

      switch (infoLevl) {

      // Set standard file information (dates/attributes)

      case FileInfoLevel.SetStandard:

        // Create the file information template

        int setFlags = 0;
        finfo = new FileInfo(path, 0, -1);

        // Set the creation date/time, if specified

        int smbDate = dataBuf.getShort();
        int smbTime = dataBuf.getShort();

        boolean hasSetTime = false;

        if (smbDate != 0 && smbTime != 0) {
          finfo.setCreationDateTime(new SMBDate(smbDate, smbTime).getTime());
          setFlags += FileInfo.SetCreationDate;
          hasSetTime = true;
        }

        // Set the last access date/time, if specified

        smbDate = dataBuf.getShort();
        smbTime = dataBuf.getShort();

        if (smbDate != 0 && smbTime != 0) {
          finfo.setAccessDateTime(new SMBDate(smbDate, smbTime).getTime());
          setFlags += FileInfo.SetAccessDate;
          hasSetTime = true;
        }

        // Set the last write date/time, if specified

        smbDate = dataBuf.getShort();
        smbTime = dataBuf.getShort();

        if (smbDate != 0 && smbTime != 0) {
          finfo.setModifyDateTime(new SMBDate(smbDate, smbTime).getTime());
          setFlags += FileInfo.SetModifyDate;
          hasSetTime = true;
        }

        // Set the file size/allocation size

        int fileSize = dataBuf.getInt();
        if (fileSize != 0) {
          finfo.setFileSize(fileSize);
          setFlags += FileInfo.SetFileSize;
        }

        fileSize = dataBuf.getInt();
        if (fileSize != 0) {
          finfo.setAllocationSize(fileSize);
          setFlags += FileInfo.SetAllocationSize;
        }

        // Set the attributes

        int attr = dataBuf.getInt();
        int eaListLen = dataBuf.getInt();

        if (hasSetTime == false && eaListLen == 0) {
          finfo.setFileAttributes(attr);
          setFlags += FileInfo.SetAttributes;
        }

        // Set the file information for the specified file/directory

        finfo.setFileInformationFlags(setFlags);
        JCRDriver.setFileInformation(m_sess, conn, path, finfo);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
          logger.debug("  Set Standard Info [" + treeId + "] name=" + path +
              ", attr=0x" + Integer.toHexString(attr) + ", setTime=" +
              hasSetTime + ", setFlags=0x" + Integer.toHexString(setFlags) +
              ", eaListLen=" + eaListLen);
        break;
      }

      // Set the return parameter count, so that the data area position can be
      // calculated.

      outPkt.setParameterCount(10);

      // Pack the return information into the data area of the transaction reply

      byte[] buf = outPkt.getBuffer();
      int prmPos = outPkt.getByteOffset();

      // Longword align the parameters, return an unknown word parameter
      //
      // Note: Make sure the data offset is on a longword boundary, NT has
      // problems if this is
      // not done

      prmPos = DataPacker.longwordAlign(prmPos);
      DataPacker.putIntelShort(0, buf, prmPos);

      SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, 0, prmPos + 4);
      outPkt.setByteCount((prmPos - outPkt.getByteOffset()) + 4);

      // Send the transact reply

      m_sess.sendResponseSMB(outPkt);

    } catch (FileNotFoundException ex) {

      // Requested file does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException ex) {

      // Not allowed to change file attributes/settings

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    } catch (Exception ex) {
      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }
  }

  /**
   * Process the file write request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procWriteAndX(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procWriteAndX");
    // Check that the received packet looks like a valid write andX request

    if (m_smbPkt.checkPacketIsValid(12, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is to the IPC$ remote admin named pipe pass the request
    // to the IPC
    // handler.

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

      // Use the IPC$ handler to process the request

      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    }

    // Extract the write file parameters

    int fid = m_smbPkt.getParameter(2);
    long offset = (long) (((long) m_smbPkt.getParameterLong(3)) & 0xFFFFFFFFL); // bottom
    // 32bits
    // of file
    // offset
    int dataPos = m_smbPkt.getParameter(11) + RFCNetBIOSProtocol.HEADER_LEN;

    int dataLen = m_smbPkt.getParameter(10);
    int dataLenHigh = 0;

    if (m_smbPkt.getReceivedLength() > 0xFFFF)
      dataLenHigh = m_smbPkt.getParameter(9) & 0x0001;

    if (dataLenHigh > 0)
      dataLen += (dataLenHigh << 16);

    // Check for the NT format request that has the top 32bits of the file
    // offset

    if (m_smbPkt.getParameterCount() == 14) {
      long topOff = (long) (((long) m_smbPkt.getParameterLong(12)) & 0xFFFFFFFFL);
      offset += topOff << 32;
    }

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
      logger.debug("File Write AndX [" + netFile.getFileId() + "] : Size=" +
          dataLen + " ,Pos=" + offset);

    // Write data to the file

    byte[] buf = m_smbPkt.getBuffer();
    int wrtlen = dataLen;

    // Access the disk interface and write to the file

    try {

      // Write to the file

      ((JCRNetworkFile) netFile).updateFile(new ByteArrayInputStream(buf,
          dataPos, dataLen), dataLen, offset);

      logger.debug(dataLen + " writed to binvalue");

    } catch (AccessDeniedException ex) {

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
        logger.debug("File Write Error [" + netFile.getFileId() + "] : " +
            ex.toString());

      // Not allowed to write to the file

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;

    } catch (RepositoryException ex) {
      ex.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;

    } catch (Exception e) {
      e.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;

    }

    // Return the count of bytes actually written

    outPkt.setParameterCount(6);
    outPkt.setAndXCommand(0xFF);
    outPkt.setParameter(1, 0); // AndX offset
    outPkt.setParameter(2, wrtlen);
    outPkt.setParameter(3, 0xFFFF);

    if (dataLenHigh > 0) {
      outPkt.setParameter(4, dataLen >> 16);
      outPkt.setParameter(5, 0);
    } else {
      outPkt.setParameterLong(4, 0);
    }

    outPkt.setByteCount(0);
    outPkt.setParameter(1, outPkt.getLength());

    // Send the write response

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Process the file create/open request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procNTCreateAndX(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procNTCreateAndX");
    // Check that the received packet looks like a valid NT create andX request

    if (m_smbPkt.checkPacketIsValid(24, 1) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is to the IPC$ remote admin named pipe pass the request
    // to the IPC
    // handler. If the device is
    // not a disk type device then return an error.

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

      // Use the IPC$ handler to process the request

      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    } else if (conn.getSharedDevice().getType() != ShareType.DISK) {

      // Return an access denied error

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Extract the NT create andX parameters

    NTParameterPacker prms = new NTParameterPacker(m_smbPkt.getBuffer(),
        SMBSrvPacket.PARAMWORDS + 5);

    int nameLen = prms.unpackWord();

    int flags = prms.unpackInt(); // Create bit set:
    // 0x02 - Request an oplock
    // 0x04 - Request a batch oplock
    // 0x08 - Target of open must be
    // directory

    int rootFID = prms.unpackInt(); // if not null, used as parent directory FID

    int accessMask = prms.unpackInt();

    long allocSize = prms.unpackLong();

    int attrib = prms.unpackInt();

    int shrAccess = prms.unpackInt();
    // Shared Access:
    // Name Value Meaning
    // 0 Prevents the file from being shared.
    // FILE_SHARE_READ 0x00000001 Other open operations can be performed on
    // the file for read access.
    // FILE_SHARE_WRITE 0x00000002 Other open operations can be performed on
    // the file for write access.
    // FILE_SHARE_DELETE 0x00000004 Other open operations can be performed on
    // the file for delete access.

    int createDisp = prms.unpackInt(); // Action performet with file existed or
    // not
    int createOptn = prms.unpackInt(); // Create options
    int impersonLev = prms.unpackInt(); // Security QOS information
    int secFlags = prms.unpackByte();

    // Extract the filename string

    String fileName = DataPacker.getUnicodeString(m_smbPkt.getBuffer(),
        DataPacker.wordAlign(m_smbPkt.getByteOffset()), nameLen / 2);
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    logger.debug(" filename before encoding = " + fileName);

    // Decode name to jcr mode

    if (fileName.equals("")) {
      fileName = "/";
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = path.toString();
    }

    // Check if the file name contains a stream name
    String temp = fileName;
    String stream = null;
    int pos = temp.indexOf(":");
    if (pos == -1) {
      fileName = NameCoder.DecodeName(temp);
    } else {
      fileName = NameCoder.DecodeName(temp.substring(0, pos));
      stream = temp.substring(pos);
    }

    // temporary
    logger.debug(" filename after encoding = " + fileName);
    logger.debug(" create dispositioon = " + FileAction.asString(createDisp));
    // Check if the file name contains a file stream name.

    if (stream != null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,// correct - from
          // snifer
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    }

    // Create the file open parameters to be passed to the disk interface

    FileOpenParams params = new FileOpenParams(fileName, stream, createDisp,
        accessMask, attrib, shrAccess, allocSize, createOptn, rootFID,
        impersonLev, secFlags);

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("NT Create AndX [" + m_smbPkt.getTreeId() + "] params=" +
          params);

    // TODO Check if the file name is valid

    int fid;
    NetworkFile netFile = null;
    int respAction = 0;

    try {

      // Check if the requested file already exists

      if (!conn.getSession().itemExists(fileName)) {

        // Check if the file should be created if it does not exist

        if (createDisp == FileAction.NTCreate ||
            createDisp == FileAction.NTOpenIf ||
            createDisp == FileAction.NTOverwriteIf ||
            createDisp == FileAction.NTSupersede) {

          // Check if the user has the required access permission

          if (conn.hasWriteAccess() == false) {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
                SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
          }

          // Create file/directory
          // TODO check if created file or directore with attributes and
          // createOptn
          netFile = JCRDriver.createFile(conn, params);

          // Check if the delete on close option is set

          if (netFile != null && (createOptn & WinNT.CreateDeleteOnClose) != 0)
            netFile.setDeleteOnClose(true);

          // Indicate that the file did not exist and was created

          respAction = FileAction.FileCreated;
        } else {
          // Return a file not found error
          m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
              SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
          return;
          // }
        }
      } else if (createDisp == FileAction.NTCreate) {

        // Return a file exists error

        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision,
            SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
        return;
      } else {

        // Open the requested file/directory

        netFile = JCRDriver.openFile(conn, params);

        // Check if the file should be truncated

        if (createDisp == FileAction.NTSupersede ||
            createDisp == FileAction.NTOverwriteIf) {

          // Truncate the file
          if (netFile.isDirectory())
            throw new AccessDeniedException();

          ((JCRNetworkFile) netFile).truncateFile(0L);

          // Debug

          if (logger.isDebugEnabled() &&
              m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("  [" + m_smbPkt.getTreeId() + "] name=" + fileName +
                " truncated");
        }

        // Set the file action response

        respAction = FileAction.FileExisted;
      }

      // Add the file to the list of open files for this tree connection

      fid = conn.addFile(netFile, getSession());

    } catch (TooManyFilesException ex) {

      // Too many files are open on this connection, cannot open any more files.

      m_sess.sendErrorResponseSMB(SMBStatus.NTTooManyOpenFiles,
          SMBStatus.DOSTooManyOpenFiles, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException ex) {

      // Return an access denied error

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (FileExistsException ex) {

      // File/directory already exists

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision,
          SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
      return;
    } catch (java.io.IOException ex) {

      // Failed to open the file

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (LockException e) {
      // Return an access denied error

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (javax.jcr.nodetype.ConstraintViolationException e) {
      // can't create file or othe nod as child for parent node so return access
      // denied

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Build the NT create andX response

    outPkt.setParameterCount(34);

    outPkt.setAndXCommand(0xFF);
    outPkt.setParameter(1, 0); // AndX offset

    prms.reset(outPkt.getBuffer(), SMBSrvPacket.PARAMWORDS + 4);

    // Fake the oplock for certain file types

    boolean fakeOpLocks = FakeOpLocks;
    String fname = params.getPath().toUpperCase();

    if (fname.endsWith(".URL")) {

      // Fake the oplock

      fakeOpLocks = true;
    }

    // Check if oplocks should be faked

    if (fakeOpLocks == true) {

      // If an oplock was requested indicate it was granted, for now

      if ((flags & WinNT.RequestBatchOplock) != 0) {

        // Batch oplock granted

        prms.packByte(2);
      } else if ((flags & WinNT.RequestOplock) != 0) {

        // Exclusive oplock granted

        prms.packByte(1);
      } else {

        // No oplock granted

        prms.packByte(0);
      }
    } else
      prms.packByte(0);

    // Pack the file id

    prms.packWord(fid);
    prms.packInt(respAction);

    // Pack the file/directory dates

    if (netFile.hasCreationDate())
      prms.packLong(netFile.getCreationDate());// NTTime.toNTTime(netFile.getCreationDate()));
    else
      prms.packLong(0);

    if (netFile.hasAccessDate())
      prms.packLong(netFile.getAccessDate());// NTTime.toNTTime(netFile.getAccessDate()));
    else
      prms.packLong(0);

    if (netFile.hasModifyDate()) {
      long modDate = netFile.getModifyDate(); // NTTime.toNTTime(netFile.getModifyDate());
      prms.packLong(modDate);
      prms.packLong(modDate);
    } else {
      prms.packLong(0); // Last write time
      prms.packLong(0); // Change time
    }

    prms.packInt(netFile.getFileAttributes());

    // Pack the file size/allocation size

    long fileSize = netFile.getFileSize();
    if (fileSize > 0L)
      fileSize = (fileSize + 512L) & 0xFFFFFFFFFFFFFE00L;

    prms.packLong(fileSize); // Allocation size
    prms.packLong(netFile.getFileSize()); // End of file
    prms.packWord(0); // File type - disk file
    prms.packWord((flags & WinNT.ExtendedResponse) != 0 ? 7 : 0); // Device
    // state
    prms.packByte(netFile.isDirectory() ? 1 : 0);

    prms.packWord(0); // byte count = 0

    // Set the AndX offset

    int endPos = prms.getPosition();
    outPkt.setParameter(1, endPos - RFCNetBIOSProtocol.HEADER_LEN);

    // Check if there is a chained request

    if (m_smbPkt.hasAndXCommand()) {

      // Process the chained requests

      endPos = procAndXCommands(outPkt, endPos, netFile);
    }

    // Send the response packet

    m_sess.sendResponseSMB(outPkt, endPos - RFCNetBIOSProtocol.HEADER_LEN);

  }

  /**
   * Process the cancel request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procNTCancel(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procNTCancel");
    // Check that the received packet looks like a valid NT cancel request

    if (m_smbPkt.checkPacketIsValid(0, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Find the matching notify request and remove it

    // Return a cancelled status

    m_smbPkt.setParameterCount(0);
    m_smbPkt.setByteCount(0);

    // Enable the long error status flag

    if (m_smbPkt.isLongErrorCode() == false)
      m_smbPkt
          .setFlags2(m_smbPkt.getFlags2() + SMBSrvPacket.FLG2_LONGERRORCODE);

    // Set the NT status code

    m_smbPkt.setLongErrorCode(SMBStatus.NTCancelled);

    // Set the Unicode strings flag

    if (m_smbPkt.isUnicode() == false)
      m_smbPkt.setFlags2(m_smbPkt.getFlags2() + SMBSrvPacket.FLG2_UNICODE);

    // Return the error response to the client

    m_sess.sendResponseSMB(m_smbPkt);

    // Nothing to cancel

    // m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
    // SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);

  }

  /**
   * Process an NT transaction
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procNTTransaction(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procNTtransaction");

    // Check that we received enough parameters for a transact2 request

    if (m_smbPkt.checkPacketIsValid(19, 0) == false) {

      // Not enough parameters for a valid transact2 request

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Check if the transaction request is for the IPC$ pipe

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {
      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    }

    // Create an NT transaction using the received packet

    NTTransPacket ntTrans = new NTTransPacket(m_smbPkt.getBuffer());
    int subCmd = ntTrans.getNTFunction();

    // Check for a notfy change request, this needs special processing

    if (subCmd == PacketType.NTTransNotifyChange) {

      // Handle the notify change setup request

      procNTTransactNotifyChange(ntTrans, outPkt);
      return;
    }

    // Create a transact buffer to hold the transaction parameter block and data
    // block

    SrvTransactBuffer transBuf = null;

    if (ntTrans.getTotalParameterCount() == ntTrans.getParameterBlockCount() &&
        ntTrans.getTotalDataCount() == ntTrans.getDataBlockCount()) {

      // Create a transact buffer using the packet buffer, the entire request is
      // contained in
      // a single
      // packet

      transBuf = new SrvTransactBuffer(ntTrans);
    } else {

      // Create a transact buffer to hold the multiple transact request
      // parameter/data blocks

      transBuf = new SrvTransactBuffer(ntTrans.getSetupCount(), ntTrans
          .getTotalParameterCount(), ntTrans.getTotalDataCount());
      transBuf.setType(ntTrans.getCommand());
      transBuf.setFunction(subCmd);

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
        logger.debug("NT Transaction [" + m_smbPkt.getTreeId() + "] transbuf=" +
            transBuf);

      // Append the setup, parameter and data blocks to the transaction data

      byte[] buf = ntTrans.getBuffer();
      int cnt = ntTrans.getSetupCount();

      if (cnt > 0)
        transBuf.appendSetup(buf, ntTrans.getSetupOffset(), cnt * 2);

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
        logger
            .debug("NT Transaction [" + m_smbPkt.getTreeId() + "] pcnt=" +
                ntTrans.getNTParameter(4) + ", offset=" +
                ntTrans.getNTParameter(5));

      cnt = ntTrans.getParameterBlockCount();

      if (cnt > 0)
        transBuf.appendParameter(buf, ntTrans.getParameterBlockOffset(), cnt);

      cnt = ntTrans.getDataBlockCount();
      if (cnt > 0)
        transBuf.appendData(buf, ntTrans.getDataBlockOffset(), cnt);
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
      logger.debug("NT Transaction [" + m_smbPkt.getTreeId() + "] cmd=0x" +
          Integer.toHexString(subCmd) + ", multiPkt=" +
          transBuf.isMultiPacket());

    // Check for a multi-packet transaction, for a multi-packet transaction we
    // just acknowledge
    // the receive with
    // an empty response SMB

    if (transBuf.isMultiPacket()) {

      // Save the partial transaction data

      m_sess.setTransaction(transBuf);

      // Send an intermediate acknowedgement response

      m_sess.sendSuccessResponseSMB();
      return;
    }

    // Process the transaction buffer

    processNTTransactionBuffer(transBuf, ntTrans);
  }

  /**
   * Process an NT transaction secondary packet
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procNTTransactionSecondary(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procNTTransactionSecondary");

    // Check that we received enough parameters for a transact2 request

    if (m_smbPkt.checkPacketIsValid(18, 0) == false) {

      // Not enough parameters for a valid transact2 request

      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Check if the transaction request is for the IPC$ pipe

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {
      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    }

    // Check if there is an active transaction, and it is an NT transaction

    if (m_sess.hasTransaction() == false ||
        m_sess.getTransaction().isType() != PacketType.NTTransact) {

      // No NT transaction to continue, return an error

      m_sess.sendErrorResponseSMB(SMBStatus.SRVNonSpecificError,
          SMBStatus.ErrSrv);
      return;
    }

    // Create an NT transaction using the received packet

    NTTransPacket ntTrans = new NTTransPacket(m_smbPkt.getBuffer());
    byte[] buf = ntTrans.getBuffer();
    SrvTransactBuffer transBuf = m_sess.getTransaction();

    // Append the parameter data to the transaction buffer, if any

    int plen = ntTrans.getParameterBlockCount();
    if (plen > 0) {

      // Append the data to the parameter buffer

      DataBuffer paramBuf = transBuf.getParameterBuffer();
      paramBuf.appendData(buf, ntTrans.getParameterBlockOffset(), plen);
    }

    // Append the data block to the transaction buffer, if any

    int dlen = ntTrans.getDataBlockCount();
    if (dlen > 0) {

      // Append the data to the data buffer

      DataBuffer dataBuf = transBuf.getDataBuffer();
      dataBuf.appendData(buf, ntTrans.getDataBlockOffset(), dlen);
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
      logger.debug("NT Transaction Secondary [" + m_smbPkt.getTreeId() +
          "] paramLen=" + plen + ", dataLen=" + dlen);

    // Check if the transaction has been received or there are more sections to
    // be received

    int totParam = ntTrans.getTotalParameterCount();
    int totData = ntTrans.getTotalDataCount();

    int paramDisp = ntTrans.getParameterBlockDisplacement();
    int dataDisp = ntTrans.getDataBlockDisplacement();

    if ((paramDisp + plen) == totParam && (dataDisp + dlen) == totData) {

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
        logger.debug("NT Transaction complete, processing ...");

      // Clear the in progress transaction

      m_sess.setTransaction(null);

      // Process the transaction

      processNTTransactionBuffer(transBuf, ntTrans);
    }

    // No response is sent for a transaction secondary
  }

  /**
   * Process an NT transaction buffer
   * 
   * @param tbuf
   *          TransactBuffer
   * @param outPkt
   *          NTTransPacket
   * @exception IOException
   *              If a network error occurs
   * @exception SMBSrvException
   *              If an SMB error occurs
   */
  private final void processNTTransactionBuffer(SrvTransactBuffer tbuf,
      NTTransPacket outPkt) throws IOException, SMBSrvException {
    logger.debug(":processNTTransactionBuffer");
    // Process the NT transaction buffer

    switch (tbuf.getFunction()) {

    // Create file/directory

    case PacketType.NTTransCreate:
      procNTTransactCreate(tbuf, outPkt);
      break;

    // I/O control

    case PacketType.NTTransIOCtl:
      procNTTransactIOCtl(tbuf, outPkt);
      break;

    // Query security descriptor

    case PacketType.NTTransQuerySecurityDesc:
      procNTTransactQuerySecurityDesc(tbuf, outPkt);
      break;

    // Set security descriptor

    case PacketType.NTTransSetSecurityDesc:
      procNTTransactSetSecurityDesc(tbuf, outPkt);
      break;

    // Rename file/directory via handle

    case PacketType.NTTransRename:
      procNTTransactRename(tbuf, outPkt);
      break;

    // Get user quota

    case PacketType.NTTransGetUserQuota:

      // Return a not implemented error status

      m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented,
          SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
      break;

    // Set user quota

    case PacketType.NTTransSetUserQuota:

      // Return a not implemented error status

      m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented,
          SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
      break;

    // Unknown NT transaction command

    default:
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      break;
    }
  }

  /**
   * Process an NT create file/directory transaction
   * <p>
   * NT Create With Security Description Or Extended Attributes
   * 
   * @param tbuf
   *          TransactBuffer
   * @param outPkt
   *          NTTransPacket
   * @exception IOException
   * @exception SMBSrvException
   */
  protected final void procNTTransactCreate(SrvTransactBuffer tbuf,
      NTTransPacket outPkt) throws IOException, SMBSrvException {
    logger.debug(":procNTTransactCreate");
    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
      logger.debug("NT TransactCreate");

    // Check that the received packet looks like a valid NT create transaction

    if (tbuf.hasParameterBuffer() && tbuf.getParameterBuffer().getLength() < 52) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is not a disk share then return an error.

    if (conn.getSharedDevice().getType() != ShareType.DISK) {

      // Return an access denied error

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Extract the file create parameters

    DataBuffer tparams = tbuf.getParameterBuffer();

    int flags = tparams.getInt();
    int rootFID = tparams.getInt();
    int accessMask = tparams.getInt();
    long allocSize = tparams.getLong();
    int attrib = tparams.getInt();
    int shrAccess = tparams.getInt();
    int createDisp = tparams.getInt();
    int createOptn = tparams.getInt();
    int sdLen = tparams.getInt();
    int eaLen = tparams.getInt();
    int nameLen = tparams.getInt();
    int impersonLev = tparams.getInt();
    int secFlags = tparams.getByte();

    // Extract the filename string

    tparams.wordAlign();
    String fileName = tparams.getString(nameLen, true);

    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Decode name to jcr mode

    if (fileName.equals("")) {
      fileName = "/";
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = path.toString();
    }

    // Check if the file name contains a stream name
    String temp = fileName;
    String stream = null;
    int pos = temp.indexOf(":");
    if (pos == -1) {
      fileName = NameCoder.DecodeName(temp);
    } else {
      fileName = NameCoder.DecodeName(temp.substring(0, pos));
      stream = temp.substring(pos);
    }

    // Check if the file name contains a file stream name. If the disk interface
    // does not
    // implement the optional NTFS
    // streams interface then return an error status, not supported.

    if (stream != null) {

      // Return a file not found error

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    }

    // Create the file open parameters to be passed to the disk interface

    FileOpenParams params = new FileOpenParams(fileName, stream, createDisp,
        accessMask, attrib, shrAccess, allocSize, createOptn, rootFID,
        impersonLev, secFlags);

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("NT TransactCreate [" + m_smbPkt.getTreeId() + "] params=" +
          params + "  secDescLen=" + sdLen + ", extAttribLen=" + eaLen);

    // Access the disk interface and open/create the requested file

    int fid;
    NetworkFile netFile = null;
    int respAction = 0;

    try {

      // Check if the requested file already exists
      if (!conn.getSession().itemExists(fileName)) { // object not exists

        // Check if the file should be created if it does not exist

        if (createDisp == FileAction.NTCreate ||
            createDisp == FileAction.NTOpenIf ||
            createDisp == FileAction.NTOverwriteIf ||
            createDisp == FileAction.NTSupersede) {

          // Create a new file /directory

          netFile = JCRDriver.createFile(conn, params);

          // Indicate that the file did not exist and was created

          respAction = FileAction.FileCreated;
        } else {

          // Check if the path is a directory

          if (!((Node) conn.getSession().getItem(fileName))
              .isNodeType("nt:file")) {

            // Return an access denied error

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision,
                SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
            return;
          } else {

            // Return a file not found error

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
                SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
          }
        }
      } else if (createDisp == FileAction.NTCreate) {

        // Return a file exists error

        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision,
            SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
        return;
      } else {

        // Open the requested file/directory

        netFile = JCRDriver.openFile(conn, params);

        // Check if the file should be truncated

        if (createDisp == FileAction.NTSupersede ||
            createDisp == FileAction.NTOverwriteIf) {

          // Truncate the file
          if (netFile.isDirectory())
            throw new AccessDeniedException();

          ((JCRNetworkFile) netFile).truncateFile(0L);

          // Debug

          if (logger.isDebugEnabled() &&
              m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("  [" + m_smbPkt.getTreeId() + "] name=" + fileName +
                " truncated");
        }

        // Set the file action response

        respAction = FileAction.FileExisted;
      }

      // Add the file to the list of open files for this tree connection

      fid = conn.addFile(netFile, getSession());
    } catch (TooManyFilesException ex) {

      // Too many files are open on this connection, cannot open any more files.

      m_sess.sendErrorResponseSMB(SMBStatus.NTTooManyOpenFiles,
          SMBStatus.DOSTooManyOpenFiles, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException ex) {

      // Return an access denied error

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (FileExistsException ex) {

      // File/directory already exists

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision,
          SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
      return;
    } catch (PathNotFoundException ex) {

      // Failed to open the file

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound,
          SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {

      // Internal jcr exception
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Build the NT transaction create response

    DataBuffer prms = new DataBuffer(128);

    // If an oplock was requested indicate it was granted, for now

    if ((flags & WinNT.RequestBatchOplock) != 0) {

      // Batch oplock granted

      prms.putByte(2);
    } else if ((flags & WinNT.RequestOplock) != 0) {

      // Exclusive oplock granted

      prms.putByte(1);
    } else {

      // No oplock granted

      prms.putByte(0);
    }
    prms.putByte(0); // alignment

    // Pack the file id

    prms.putShort(fid);
    prms.putInt(respAction);

    // EA error offset

    prms.putInt(0);

    // Pack the file/directory dates

    if (netFile.hasCreationDate())
      prms.putLong(NTTime.toNTTime(netFile.getCreationDate()));
    else
      prms.putLong(0);

    if (netFile.hasModifyDate()) {
      long modDate = NTTime.toNTTime(netFile.getModifyDate());
      prms.putLong(modDate);
      prms.putLong(modDate);
      prms.putLong(modDate);
    } else {
      prms.putLong(0); // Last access time
      prms.putLong(0); // Last write time
      prms.putLong(0); // Change time
    }

    prms.putInt(netFile.getFileAttributes());

    // Pack the file size/allocation size

    prms.putLong(netFile.getFileSize()); // Allocation size
    prms.putLong(netFile.getFileSize()); // End of file
    prms.putShort(0); // File type - disk file
    prms.putShort(0); // Device state
    prms.putByte(netFile.isDirectory() ? 1 : 0);

    // Initialize the transaction response

    outPkt.initTransactReply(prms.getBuffer(), prms.getLength(), null, 0);

    // Send back the response

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * Process an NT I/O control transaction
   * 
   * @param tbuf
   *          TransactBuffer
   * @param outPkt
   *          NTTransPacket
   * @exception IOException
   * @exception SMBSrvException
   */
  protected final void procNTTransactIOCtl(SrvTransactBuffer tbuf,
      NTTransPacket outPkt) throws IOException, SMBSrvException {
    logger.debug(":procNTTransactIOCtl");

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Unpack the request details

    DataBuffer setupBuf = tbuf.getSetupBuffer();

    int ctrlCode = setupBuf.getInt();
    int fid = setupBuf.getShort();
    boolean fsctrl = setupBuf.getByte() == 1 ? true : false;
    int filter = setupBuf.getByte(); // completion filter, a.k.a. IsFlags

    // Debug

    // TODO && m_sess.hasDebug(SMBSrvSession.DBG_TRAN)
    if (logger.isDebugEnabled())
      logger.debug("NT IOCtl code=" + NTIOCtl.asString(ctrlCode) + ", fid=" +
          fid + ", fsctrl=" + fsctrl + ", filter=" + filter);

    try {

      // Pass the request to the IO control interface for processing

      DataBuffer response = IOControlHandler.processIOControl(m_sess, conn,
          ctrlCode, fid, tbuf.getDataBuffer(), fsctrl, filter);

      // Pack the response

      if (response != null) {

        // Pack the response data block

        outPkt.initTransactReply(null, 0, response.getBuffer(), response
            .getLength(), 1);
        outPkt.setSetupParameter(0, response.getLength());
      } else {

        // Pack an empty response data block

        outPkt.initTransactReply(null, 0, null, 0, 1);
        outPkt.setSetupParameter(0, 0);
      }
    } catch (IOControlNotImplementedException ex) {
      // Return a not implemented error status

      m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented,
          SMBStatus.SRVInternalServerError, SMBStatus.ErrSrv);
      return;
    } catch (SMBException ex) {

      // Return the specified SMB status, this should be an NT status code

      m_sess.sendErrorResponseSMB(ex.getErrorCode(),
          SMBStatus.SRVInternalServerError, SMBStatus.ErrSrv);
      return;
    }

    // Send the IOCtl response

    m_sess.sendResponseSMB(outPkt);

    // Send back an error, IOctl not supported

    // m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented,
    // SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);

  }

  /**
   * Process an NT query security descriptor transaction
   * 
   * @param tbuf
   *          TransactBuffer
   * @param outPkt
   *          NTTransPacket
   * @exception IOException
   * @exception SMBSrvException
   */
  protected final void procNTTransactQuerySecurityDesc(SrvTransactBuffer tbuf,
      NTTransPacket outPkt) throws IOException, SMBSrvException {
    logger.debug(":procNTTransactQuerySecurityDesc");
    /*
     * // Get the virtual circuit for the request
     * 
     * VirtualCircuit vc = m_sess.findVirtualCircuit(m_smbPkt.getUserId());
     * 
     * if (vc == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos); return; } // Get the tree
     * connection details
     * 
     * TreeConnection conn = vc.findConnection(tbuf.getTreeId());
     * 
     * if (conn == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos); return; } // Check if the
     * user has the required access permission
     * 
     * if (conn.hasReadAccess() == false) { // User does not have the required
     * access rights
     * 
     * m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
     * SMBStatus.DOSAccessDenied, SMBStatus.ErrDos); return; } // Unpack the
     * request details
     * 
     * DataBuffer paramBuf = tbuf.getParameterBuffer();
     * 
     * int fid = paramBuf.getShort(); int flags = paramBuf.getShort(); // Debug
     * 
     * if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
     * logger.debug("NT QuerySecurityDesc fid=" + fid + ", flags=" + flags); //
     * Get the file details
     * 
     * NetworkFile netFile = conn.findFile(fid);
     * 
     * if (netFile == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos); return; } // Return an
     * empty security descriptor
     * 
     * byte[] paramblk = new byte[4]; DataPacker.putIntelInt(0, paramblk, 0);
     * 
     * outPkt.initTransactReply(paramblk, paramblk.length, null, 0); // Send
     * back the response
     * 
     * m_sess.sendResponseSMB(outPkt);
     */
  }

  /**
   * Process an NT set security descriptor transaction
   * 
   * @param tbuf
   *          TransactBuffer
   * @param outPkt
   *          NTTransPacket
   * @exception IOException
   * @exception SMBSrvException
   */
  protected final void procNTTransactSetSecurityDesc(SrvTransactBuffer tbuf,
      NTTransPacket outPkt) throws IOException, SMBSrvException {
    logger.debug(":procNTTransactSetSecurityDesc");
    /*
     * // Unpack the request details
     * 
     * DataBuffer paramBuf = tbuf.getParameterBuffer(); // Get the virtual
     * circuit for the request
     * 
     * VirtualCircuit vc = m_sess.findVirtualCircuit(m_smbPkt.getUserId());
     * 
     * if (vc == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos); return; } // Get the tree
     * connection details
     * 
     * TreeConnection conn = vc.findConnection(tbuf.getTreeId());
     * 
     * if (conn == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos); return; } // Check if the
     * user has the required access permission
     * 
     * if (conn.hasWriteAccess() == false) { // User does not have the required
     * access rights
     * 
     * m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
     * SMBStatus.DOSAccessDenied, SMBStatus.ErrDos); return; } // Get the file
     * details
     * 
     * int fid = paramBuf.getShort(); paramBuf.skipBytes(2); int flags =
     * paramBuf.getInt(); // Debug
     * 
     * if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
     * logger.debug("NT SetSecurityDesc fid=" + fid + ", flags=" + flags); //
     * Send back an error, security descriptors not supported
     * 
     * m_sess .sendErrorResponseSMB(SMBStatus.SRVNonSpecificError,
     * SMBStatus.ErrSrv);
     */
  }

  /**
   * Process an NT change notification transaction
   * 
   * @param ntpkt
   *          NTTransPacket
   * @param outPkt
   *          SMBSrvPacket
   * @exception IOException
   * @exception SMBSrvException
   */
  protected final void procNTTransactNotifyChange(NTTransPacket ntpkt,
      SMBSrvPacket outPkt) throws IOException, SMBSrvException {
    logger.debug(":procNTTransactNotifyChange");
    /*
     * // Get the virtual circuit for the request
     * 
     * VirtualCircuit vc = m_sess.findVirtualCircuit(m_smbPkt.getUserId());
     * 
     * if (vc == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos); return; } // Get the tree
     * connection details
     * 
     * int treeId = ntpkt.getTreeId(); TreeConnection conn =
     * vc.findConnection(treeId);
     * 
     * if (conn == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos); return; } // Check if the
     * user has the required access permission
     * 
     * if (conn.hasReadAccess() == false) { // User does not have the required
     * access rights
     * 
     * m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
     * SMBStatus.DOSAccessDenied, SMBStatus.ErrDos); return; } // Make sure the
     * tree connection is for a disk device
     * 
     * if (conn.getContext() == null || conn.getContext() instanceof
     * DiskDeviceContext == false) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv); return; } // Check if
     * the device has change notification enabled
     * 
     * DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext(); if
     * (diskCtx.hasChangeHandler() == false) { // Return an error status, share
     * does not have change notification enabled
     * 
     * m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented,
     * SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv); return; } // Unpack the
     * request details
     * 
     * ntpkt.resetSetupPointer();
     * 
     * int filter = ntpkt.unpackInt(); int fid = ntpkt.unpackWord(); boolean
     * watchTree = ntpkt.unpackByte() == 1 ? true : false; int mid =
     * ntpkt.getMultiplexId(); // Get the file details
     * 
     * NetworkFile dir = conn.findFile(fid); if (dir == null) {
     * m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
     * SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv); return; } // Get the
     * maximum notifications to buffer whilst waiting for the request to // be
     * reset after // a notification // has been triggered
     * 
     * int maxQueue = 0; // Debug
     * 
     * if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
     * logger.debug("NT NotifyChange fid=" + fid + ", mid=" + mid + ",
     * filter=0x" + Integer.toHexString(filter) + ", dir=" + dir.getFullName() + ",
     * maxQueue=" + maxQueue); // Check if there is an existing request in the
     * notify list that matches the // new request and // is in a completed //
     * state. If so then the client is resetting the notify request so reuse the //
     * existing // request.
     * 
     * NotifyRequest req = m_sess.findNotifyRequest(dir, filter, watchTree);
     * 
     * if (req != null && req.isCompleted()) { // Reset the existing request
     * with the new multiplex id
     * 
     * req.setMultiplexId(mid); req.setCompleted(false); // Check if there are
     * any buffered notifications for this session
     * 
     * if (req.hasBufferedEvents() || req.hasNotifyEnum()) { // Get the buffered
     * events from the request, clear the list from the // request
     * 
     * NotifyChangeEventList bufList = req.getBufferedEventList();
     * req.clearBufferedEvents(); // Send the buffered events
     * 
     * diskCtx.getChangeHandler().sendBufferedNotifications(req, bufList); //
     * DEBUG
     * 
     * if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY)) {
     * if (bufList == null) logger.debug(" Sent buffered notifications, req=" +
     * req.toString() + ", Enum"); else logger.debug(" Sent buffered
     * notifications, req=" + req.toString() + ", count=" +
     * bufList.numberOfEvents()); } } else { // DEBUG
     * 
     * if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
     * logger.debug(" Reset notify request, " + req.toString()); } } else { //
     * Create a change notification request
     * 
     * req = new NotifyRequest(filter, watchTree, m_sess, dir, mid, ntpkt
     * .getTreeId(), ntpkt.getProcessId(), ntpkt.getUserId(), maxQueue); // Add
     * the request to the pending notify change lists
     * 
     * m_sess.addNotifyRequest(req, diskCtx); // Debug
     * 
     * if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
     * logger.debug(" Added new request, " + req.toString()); } // NOTE: If the
     * change notification request is accepted then no reply is // sent to the
     * client. // A reply will be sent // asynchronously if the change
     * notification is triggered.
     */
  }

  /**
   * Process an NT rename via handle transaction
   * 
   * @param tbuf
   *          TransactBuffer
   * @param outPkt
   *          NTTransPacket
   * @exception IOException
   * @exception SMBSrvException
   */
  protected final void procNTTransactRename(SrvTransactBuffer tbuf,
      NTTransPacket outPkt) throws IOException, SMBSrvException {
    logger.debug(":procNTTransactRename");
    // Unpack the request details

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    // Get the virtual circuit for the request

    // Get the tree connection details

    int treeId = tbuf.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
      logger.debug("NT TransactRename");

    // Send back an error, NT rename not supported

    m_sess
        .sendErrorResponseSMB(SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
  }
}
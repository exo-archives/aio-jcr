/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs.smb.server;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.netbios.RFCNetBIOSProtocol;
import org.exoplatform.services.cifs.server.core.ShareType;
import org.exoplatform.services.cifs.server.core.SharedDevice;
import org.exoplatform.services.cifs.server.filesys.DiskInfo;
import org.exoplatform.services.cifs.server.filesys.FileAccess;
import org.exoplatform.services.cifs.server.filesys.FileAction;
import org.exoplatform.services.cifs.server.filesys.FileInfo;
import org.exoplatform.services.cifs.server.filesys.FileOpenParams;
import org.exoplatform.services.cifs.server.filesys.JCRDriver;
import org.exoplatform.services.cifs.server.filesys.NameCoder;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.cifs.server.filesys.SearchContext;
import org.exoplatform.services.cifs.server.filesys.TooManyConnectionsException;
import org.exoplatform.services.cifs.server.filesys.TooManyFilesException;
import org.exoplatform.services.cifs.server.filesys.TreeConnection;
import org.exoplatform.services.cifs.server.filesys.UnsupportedInfoLevelException;
import org.exoplatform.services.cifs.server.filesys.VolumeInfo;
import org.exoplatform.services.cifs.smb.DataType;
import org.exoplatform.services.cifs.smb.FindFirstNext;
import org.exoplatform.services.cifs.smb.InvalidUNCPathException;
import org.exoplatform.services.cifs.smb.PCShare;
import org.exoplatform.services.cifs.smb.PacketType;
import org.exoplatform.services.cifs.smb.SMBDate;
import org.exoplatform.services.cifs.smb.SMBStatus;
import org.exoplatform.services.cifs.util.DataBuffer;
import org.exoplatform.services.cifs.util.DataPacker;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * LanMan SMB Protocol Handler Class.
 * <p>
 * The LanMan protocol handler processes the additional SMBs that were added to
 * the protocol in the LanMan1 and LanMan2 SMB dialects.
 */
public class LanManProtocolHandler extends CoreProtocolHandler {

  // Debug logging

  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs.smb.server.LanManProtocolHandler");

  // Locking type flags

  protected static final int LockShared = 0x01;

  protected static final int LockOplockRelease = 0x02;

  protected static final int LockChangeType = 0x04;

  protected static final int LockCancel = 0x08;

  protected static final int LockLargeFiles = 0x10;

  /**
   * LanManProtocolHandler constructor.
   */
  public LanManProtocolHandler() {
    super();
  }

  /**
   * LanManProtocolHandler constructor.
   * 
   * @param sess
   *          org.exoplatform.services.CIFS.smbsrv.SMBSrvSession
   */
  public LanManProtocolHandler(SMBSrvSession sess) {
    super(sess);
  }

  /**
   * Return the protocol name
   * 
   * @return String
   */
  public String getName() {
    return "LanMan";
  }

  /**
   * Process the chained SMB commands (AndX).
   * 
   * @return New offset to the end of the reply packet
   * @param outPkt
   *          Reply packet.
   */
  protected final int procAndXCommands(SMBSrvPacket outPkt) {
    logger.debug(":procAndXCommands");
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

    int endOfPkt = outPkt.getByteOffset() + outPkt.getByteCount();
    boolean andxErr = false;

    while (andxCmd != SMBSrvPacket.NO_ANDX_CMD && andxErr == false) {

      // Determine the chained command type

      int prevEndOfPkt = endOfPkt;

      switch (andxCmd) {

      // Tree connect

      case PacketType.TreeConnectAndX:
        endOfPkt = procChainedTreeConnectAndX(andxOff, outPkt, endOfPkt);
        break;
      }

      // Advance to the next chained command block

      andxCmd = m_smbPkt.getAndXParameter(andxOff, 0) & 0x00FF;
      andxOff = m_smbPkt.getAndXParameter(andxOff, 1);

      // Set the next chained command details in the current parameter
      // block

      outPkt.setAndXCommand(prevEndOfPkt, andxCmd);
      outPkt.setAndXParameter(paramBlk, 1, prevEndOfPkt
          - RFCNetBIOSProtocol.HEADER_LEN);

      // Advance the current parameter block

      paramBlk = prevEndOfPkt;

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

    int flags = m_smbPkt.getAndXParameter(cmdOff, 2);
    int pwdLen = m_smbPkt.getAndXParameter(cmdOff, 3);

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getAndXByteOffset(cmdOff);
    int dataLen = m_smbPkt.getAndXByteCount(cmdOff);
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the password string

    String pwd = null;

    if (pwdLen > 0) {
      pwd = new String(buf, dataPos, pwdLen);
      dataPos += pwdLen;
      dataLen -= pwdLen;
    }

    // Extract the requested share name, as a UNC path

    String uncPath = DataPacker.getString(buf, dataPos, dataLen);
    if (uncPath == null) {
      outPkt.setError(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return endOff;
    }

    // Extract the service type string

    dataPos += uncPath.length() + 1; // null terminated
    dataLen -= uncPath.length() + 1; // null terminated

    String service = DataPacker.getString(buf, dataPos, dataLen);
    if (service == null) {
      outPkt.setError(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return endOff;
    }

    // Convert the service type to a shared device type, client may specify
    // '?????' in which
    // case we ignore the error.

    int servType = ShareType.ServiceAsType(service);
    if (servType == ShareType.UNKNOWN && service.compareTo("?????") != 0) {
      outPkt.setError(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return endOff;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
      logger.debug("ANDX Tree Connect AndX - " + uncPath + ", " + service);

    // Parse the requested share name

    PCShare share = null;

    try {
      share = new PCShare(uncPath);
    } catch (org.exoplatform.services.cifs.smb.InvalidUNCPathException ex) {
      outPkt.setError(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return endOff;
    }

    // Map the IPC$ share to the admin pipe type

    if (servType == ShareType.NAMEDPIPE
        && share.getShareName().compareTo("IPC$") == 0)
      servType = ShareType.ADMINPIPE;

    // Find the requested shared device
    SharedDevice shareDev = null;

    try {

      // Get/create the shared device

      shareDev = m_sess.getSMBServer().findShare(share.getShareName(),
          servType, getSession(), true);

    } catch (Exception ex) {

      // Return a general status, bad network name

      outPkt.setError(SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
      return endOff;
    }

    // Check if the share is valid

    if (shareDev == null
        || (servType != ShareType.UNKNOWN && shareDev.getType() != servType)) {
      outPkt.setError(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return endOff;
    }

    // Authenticate the share connect, if the server is using share mode
    // security

    int filePerm = FileAccess.Writeable;

    // Allocate a tree id for the new connection

    try {

      // Allocate the tree id for this connection

      int treeId = m_sess.addConnection(shareDev);
      outPkt.setTreeId(treeId);

      // Set the file permission that this user has been granted for this
      // share

      TreeConnection tree = m_sess.findTreeConnection(treeId);
      tree.setPermission(filePerm);

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
    pos = DataPacker.putString(ShareType.TypeAsService(servType), outBuf, pos,
        true);
    int bytLen = pos - outPkt.getAndXByteOffset(endOff);
    outPkt.setAndXByteCount(endOff, bytLen);

    // Return the new end of packet offset

    return pos;
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
    logger.debug(":procFindClose");
    // Check that the received packet looks like a valid find close request

    if (m_smbPkt.checkPacketIsValid(1, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the search id

    int searchId = m_smbPkt.getParameter(0);

    // Get the search context

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
    logger.debug(":procLockingAndX");
    // Check that the received packet looks like a valid locking andX request

    if (m_smbPkt.checkPacketIsValid(8, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt.getTreeId());

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Extract the file lock/unlock parameters

    int fid = m_smbPkt.getParameter(2);
    int lockType = m_smbPkt.getParameter(3);
    long lockTmo = m_smbPkt.getParameterLong(4);
    int lockCnt = m_smbPkt.getParameter(6);
    int unlockCnt = m_smbPkt.getParameter(7);

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_LOCK))
      logger.debug("File Lock [" + netFile.getFileId() + "] : type=0x"
          + Integer.toHexString(lockType) + ", tmo=" + lockTmo + ", locks="
          + lockCnt + ", unlocks=" + unlockCnt);

    // Return a success status for now

    outPkt.setParameterCount(2);
    outPkt.setAndXCommand(0xFF);
    outPkt.setParameter(1, 0);
    outPkt.setByteCount(0);

    // Send the lock request response

    m_sess.sendResponseSMB(outPkt);
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

    if (m_smbPkt.checkPacketIsValid(15, 1) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
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
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is to the IPC$ remote admin named pipe pass the
    // request to the IPC
    // handler. If the device is
    // not a disk type device then return an error.

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

      // Use the IPC$ handler to process the request

      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    } else if (conn.getSharedDevice().getType() != ShareType.DISK) {

      // Return an access denied error

      // m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied,
      // SMBStatus.ErrDos);
      m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
      // m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights,
      // SMBStatus.ErrSrv);
      return;
    }

    // Extract the open file parameters

    // int flags = m_smbPkt.getParameter(2); //useless
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
      logger.debug("File Open AndX [" + treeId + "] params=" + params);
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
        if ((FileAction.openIfExists(openFunc))
            || (FileAction.truncateExistingFile(openFunc))) {
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

    logger.debug("resp Action " + FileAction.asString(responseAction)
        + ", fid = " + fid + " TEMPORARY");

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
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is to the IPC$ remote admin named pipe pass the
    // request to the IPC
    // handler.

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

      // Use the IPC$ handler to process the request

      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    }

    // Extract the read file parameters

    int fid = m_smbPkt.getParameter(2);
    int offset = m_smbPkt.getParameterLong(3);
    int maxCount = m_smbPkt.getParameter(5);

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
      logger.debug("File Read AndX [" + netFile.getFileId() + "] : Size="
          + maxCount + " ,Pos=" + offset);

    // Read data from the file

    byte[] buf = outPkt.getBuffer();
    int dataPos = 0;
    int rdlen = 0;

    try {

      // Set the returned parameter count so that the byte offset can be
      // calculated

      outPkt.setParameterCount(12);
      dataPos = outPkt.getByteOffset();
      // dataPos = ( dataPos + 3) & 0xFFFFFFFC; // longword align the data

      // Check if the requested data length will fit into the buffer

      int dataLen = buf.length - dataPos;
      if (dataLen < maxCount)
        maxCount = dataLen;

      // Read from the file

      rdlen = JCRDriver.readFile(m_sess, conn, netFile, buf, dataPos, maxCount,
          offset);

    } catch (AccessDeniedException ex) {

      // No access to file, or file is a directory
      //    	
      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
        logger.debug("File Read Error [" + netFile.getFileId() + "] : "
            + ex.toString());

      // Failed to read the file

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (java.io.IOException ex) {

      // Debug

      logger.error("File Read Error [" + netFile.getFileId() + "] : ", ex);

      // Failed to read the file

      m_sess.sendErrorResponseSMB(SMBStatus.HRDReadFault, SMBStatus.ErrHrd);
      return;
    } catch (RepositoryException ex) {

      ex.printStackTrace();
      // Internal repository exception
      // SMBStatus.DOSInvalidData

      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }
    // Return the data block

    outPkt.setAndXCommand(0xFF); // no chained command
    outPkt.setParameter(1, 0);
    outPkt.setParameter(2, 0); // bytes remaining, for pipes only
    outPkt.setParameter(3, 0); // data compaction mode
    outPkt.setParameter(4, 0); // reserved
    outPkt.setParameter(5, rdlen); // data length
    outPkt.setParameter(6, dataPos - RFCNetBIOSProtocol.HEADER_LEN);
    // offset to data

    // Clear the reserved parameters

    for (int i = 7; i < 12; i++)
      outPkt.setParameter(i, 0);

    // Set the byte count

    outPkt.setByteCount((dataPos + rdlen) - outPkt.getByteOffset());

    // Send the read andX response

    m_sess.sendResponseSMB(outPkt);

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

    // Get the tree id from the received packet and validate that it is a
    // valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
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
    if (oldName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // logger.debug("oldName ["+oldName+"]; newName ["+newName+"] TEMPORARY");

    // convert name's

    if (oldName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(oldName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      oldName = NameCoder.DecodeName(path.toString());
    }

    if (newName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(newName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      newName = NameCoder.DecodeName(path.toString());
    }

    String oldNamePath = oldName.substring(0, oldName.lastIndexOf("/") + 1);
    String newNamePath = newName.substring(0, newName.lastIndexOf("/") + 1);

    Session sess = conn.getSession();
    Node n_oldFile;
    Node n_newFile;

    try {
      Node path = (Node) sess.getItem(oldNamePath);
    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    try {
      Node path = (Node) sess.getItem(newNamePath);
    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    try {
      n_oldFile = (Node) sess.getItem(oldName);
    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    try {
      n_newFile = (Node) sess.getItem(newName);
      if (n_newFile != null) {
        m_sess.sendErrorResponseSMB(SMBStatus.DOSFileAlreadyExists,
            SMBStatus.ErrDos);
        return;
      }
    } catch (PathNotFoundException e) {
      // its all rigth the new file name must not exists
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    // in newName abs path must not be any indexes
    // (required by jcr: move command)
    if ((newName.indexOf("[") != -1) || (newName.indexOf("]") != -1)) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    n_newFile = null;
    n_oldFile = null;

    // old and new pathes are checked and valid
    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Rename [" + treeId + "] old name=" + oldName
          + ", new name=" + newName);

    // Access the disk interface and rename the requested file

    try {

      // Rename the requested file
      sess.move(oldName, newName);
      sess.save();

    } catch (ConstraintViolationException e) {
      // TODO check for another error status code
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (ItemExistsException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileAlreadyExists,
          SMBStatus.ErrDos);
      return;
    } catch (VersionException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    // Build the rename file response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Process the SMB session setup request.
   * 
   * @param outPkt
   *          Response SMB packet.
   */
  protected void procSessionSetup(SMBSrvPacket outPkt) throws SMBSrvException,
      IOException, TooManyConnectionsException {
    logger.debug(":procSessionSetup");
    // Extract the client details from the session setup request

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the session details

    int maxBufSize = m_smbPkt.getParameter(2);
    int maxMpx = m_smbPkt.getParameter(3);
    int vcNum = m_smbPkt.getParameter(4);

    // Extract the password string

    byte[] pwd = null;
    int pwdLen = m_smbPkt.getParameter(7);

    if (pwdLen > 0) {
      pwd = new byte[pwdLen];
      for (int i = 0; i < pwdLen; i++)
        pwd[i] = buf[dataPos + i];
      dataPos += pwdLen;
      dataLen -= pwdLen;
    }

    // Extract the user name string

    String user = DataPacker.getString(buf, dataPos, dataLen);
    if (user == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    } else {

      // Update the buffer pointers

      dataLen -= user.length() + 1;
      dataPos += user.length() + 1;
    }

    // Extract the clients primary domain name string

    String domain = "";

    if (dataLen > 0) {

      // Extract the callers domain name

      domain = DataPacker.getString(buf, dataPos, dataLen);
      if (domain == null) {
        m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
        return;
      } else {

        // Update the buffer pointers

        dataLen -= domain.length() + 1;
        dataPos += domain.length() + 1;
      }
    }

    // Extract the clients native operating system

    String clientOS = "";

    if (dataLen > 0) {

      // Extract the callers operating system name

      clientOS = DataPacker.getString(buf, dataPos, dataLen);
      if (clientOS == null) {
        m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
        return;
      }
    }

    // DEBUG

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
      logger.debug("Session setup from user=" + user + ", password=" + pwd
          + ", domain=" + domain + ", os=" + clientOS + ", VC=" + vcNum
          + ", maxBuf=" + maxBufSize + ", maxMpx=" + maxMpx);

    // Store the client maximum buffer size and maximum multiplexed requests
    // count

    m_sess.setClientMaximumBufferSize(maxBufSize);
    m_sess.setClientMaximumMultiplex(maxMpx);

    // Create the client information and store in the session

    // Authenticate the user, if the server is using user mode security
    boolean isGuest = true;

    // Set the guest flag for the client and logged on status

    getSession().setLoggedOn(true);

    // Build the session setup response SMB

    outPkt.setParameterCount(3);
    outPkt.setParameter(0, 0); // No chained response
    outPkt.setParameter(1, 0); // Offset to chained response
    outPkt.setParameter(2, isGuest ? 1 : 0);
    outPkt.setByteCount(0);

    outPkt.setTreeId(0);
    outPkt.setUserId(0);

    // Set the various flags

    // outPkt.setFlags( SMBSrvPacket.FLG_CASELESS);
    int flags = outPkt.getFlags();
    flags &= ~SMBSrvPacket.FLG_CASELESS;
    outPkt.setFlags(flags);
    outPkt.setFlags2(SMBSrvPacket.FLG2_LONGFILENAMES);

    // Pack the OS, dialect and domain name strings.

    int pos = outPkt.getByteOffset();
    buf = outPkt.getBuffer();

    pos = DataPacker.putString("Java", buf, pos, true);
    pos = DataPacker.putString("JLAN Server " + m_sess.getServer().isVersion(),
        buf, pos, true);
    pos = DataPacker.putString(m_sess.getServer().getConfiguration()
        .getDomainName(), buf, pos, true);

    outPkt.setByteCount(pos - outPkt.getByteOffset());

    // Check if there is a chained command, or commands

    if (m_smbPkt.hasAndXCommand() && dataPos < m_smbPkt.getReceivedLength()) {

      // Process any chained commands, AndX

      pos = procAndXCommands(outPkt);
    } else {

      // Indicate that there are no chained replies

      outPkt.setAndXCommand(SMBSrvPacket.NO_ANDX_CMD);
    }

    // Send the negotiate response

    m_sess.sendResponseSMB(outPkt, pos);

    // Update the session state

    m_sess.setState(SMBSrvSessionState.SMBSESSION);

    // Notify listeners that a user has logged onto the session

    m_sess.getSMBServer().sessionLoggedOn(m_sess);

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
    logger.debug(":procTransact2");

    // Check that we received enough parameters for a transact2 request

    if (m_smbPkt.checkPacketIsValid(15, 0) == false) {

      // Not enough parameters for a valid transact2 request

      m_sess
          .sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.NTErr);
      return;
    }

    // Get the tree id from the received packet and validate that it is a
    // valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess
          .sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.NTErr);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Create a transact packet using the received SMB packet

    SMBSrvTransPacket tranPkt = new SMBSrvTransPacket(m_smbPkt.getBuffer());

    // Create a transact buffer to hold the transaction setup, parameter and
    // data blocks

    SrvTransactBuffer transBuf = null;
    int subCmd = tranPkt.getSubFunction();

    if (tranPkt.getTotalParameterCount() == tranPkt.getParameterBlockCount()
        && tranPkt.getTotalDataCount() == tranPkt.getDataBlockCount()) {

      // Create a transact buffer using the packet buffer, the entire
      // request is contained in
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

      // Append the setup, parameter and data blocks to the transaction
      // data

      byte[] buf = tranPkt.getBuffer();

      transBuf.appendSetup(buf, tranPkt.getSetupOffset(), tranPkt
          .getSetupCount() * 2);
      transBuf.appendParameter(buf, tranPkt.getParameterBlockOffset(), tranPkt
          .getParameterBlockCount());
      transBuf.appendData(buf, tranPkt.getDataBlockOffset(), tranPkt
          .getDataBlockCount());
    }

    // Set the return data limits for the transaction

    transBuf.setReturnLimits(tranPkt.getMaximumReturnSetupCount(), tranPkt
        .getMaximumReturnParameterCount(), tranPkt.getMaximumReturnDataCount());

    // Check for a multi-packet transaction, for a multi-packet transaction
    // we just acknowledge
    // the receive with
    // an empty response SMB

    if (transBuf.isMultiPacket()) {

      // Save the partial transaction data

      m_sess.setTransaction(transBuf);

      // Send an intermediate acknowedgement response

      m_sess.sendSuccessResponseSMB();
      return;
    }

    // Check if the transaction is on the IPC$ named pipe, the request
    // requires special
    // processing

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {
      IPCHandler.procTransaction(transBuf, m_sess, outPkt);
      return;
    }

    // DEBUG

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
      logger.debug("Transaction [" + treeId + "] tbuf=" + transBuf);

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
    logger.debug(":procTransact2Secondary");

    // Check that we received enough parameters for a transact2 request

    if (m_smbPkt.checkPacketIsValid(8, 0) == false) {

      // Not enough parameters for a valid transact2 request

      m_sess
          .sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.NTErr);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess
          .sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.NTErr);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Check if there is an active transaction, and it is an NT transaction

    if (m_sess.hasTransaction() == false
        || (m_sess.getTransaction().isType() == PacketType.Transaction && m_smbPkt
            .getCommand() != PacketType.TransactionSecond)
        || (m_sess.getTransaction().isType() == PacketType.Transaction2 && m_smbPkt
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
      logger.debug("Transaction Secondary [" + treeId + "] paramLen=" + plen
          + ", dataLen=" + dlen);

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
        logger.debug("Transaction second [" + treeId + "] tbuf=" + transBuf);

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
    logger.debug(":procTreeConnectAndX");
    // Check that the received packet looks like a valid tree connect
    // request

    if (m_smbPkt.checkPacketIsValid(4, 3) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Extract the parameters

    int flags = m_smbPkt.getParameter(2);
    int pwdLen = m_smbPkt.getParameter(3);

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the password string

    String pwd = null;

    if (pwdLen > 0) {
      pwd = new String(buf, dataPos, pwdLen);
      dataPos += pwdLen;
      dataLen -= pwdLen;
    }

    // Extract the requested share name, as a UNC path

    String uncPath = DataPacker.getString(buf, dataPos, dataLen);
    if (uncPath == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Extract the service type string

    dataPos += uncPath.length() + 1; // null terminated
    dataLen -= uncPath.length() + 1; // null terminated

    String service = DataPacker.getString(buf, dataPos, dataLen);
    if (service == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Convert the service type to a shared device type, client may specify
    // '?????' in which
    // case we ignore the error.

    int servType = ShareType.ServiceAsType(service);
    if (servType == ShareType.UNKNOWN && service.compareTo("?????") != 0) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
      logger.debug("Tree Connect AndX - " + uncPath + ", " + service);

    // Parse the requested share name

    PCShare share = null;

    try {
      share = new PCShare(uncPath);
    } catch (InvalidUNCPathException ex) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Map the IPC$ share to the admin pipe type

    if (servType == ShareType.NAMEDPIPE
        && share.getShareName().compareTo("IPC$") == 0)
      servType = ShareType.ADMINPIPE;

    // Find the requested shared device

    SharedDevice shareDev = null;

    try {

      // Get/create the shared device

      shareDev = m_sess.getSMBServer().findShare(share.getShareName(),
          servType, getSession(), true);
    } catch (Exception ex) {
      // Return a logon failure status

      // m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied,
      // SMBStatus.ErrDos);
      // Return a general status, bad network name

      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidNetworkName,
          SMBStatus.ErrSrv);
      return;
    }

    // Check if the share is valid

    if (shareDev == null
        || (servType != ShareType.UNKNOWN && shareDev.getType() != servType)) {
      m_sess
          .sendErrorResponseSMB(SMBStatus.NETBadNetworkName, SMBStatus.NetErr);
      return;
    }

    // Authenticate the share connection depending upon the security mode
    // the server is running
    // under

    int filePerm = FileAccess.Writeable;

    // Allocate a tree id for the new connection

    int treeId = m_sess.addConnection(shareDev);
    outPkt.setTreeId(treeId);

    // Set the file permission that this user has been granted for this
    // share

    TreeConnection tree = m_sess.findTreeConnection(treeId);
    tree.setPermission(filePerm);

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
      logger.debug("Tree Connect AndX - Allocated Tree Id = " + treeId
          + ", Permission = " + FileAccess.asString(filePerm));

    // Build the tree connect response

    outPkt.setParameterCount(3);
    outPkt.setAndXCommand(0xFF); // no chained reply
    outPkt.setParameter(1, 0);
    outPkt.setParameter(2, 0);

    // Pack the service type

    int pos = outPkt.getByteOffset();
    pos = DataPacker.putString(ShareType.TypeAsService(shareDev.getType()),
        buf, pos, true);
    outPkt.setByteCount(pos - outPkt.getByteOffset());

    // Send the response

    m_sess.sendResponseSMB(outPkt);

    // Inform the driver that a connection has been opened
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
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasWriteAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // If the connection is to the IPC$ remote admin named pipe pass the
    // request to the IPC
    // handler.

    if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE) {

      // Use the IPC$ handler to process the request

      IPCHandler.processIPCRequest(m_sess, outPkt);
      return;
    }

    // Extract the write file parameters

    int fid = m_smbPkt.getParameter(2);
    int offset = m_smbPkt.getParameterLong(3);
    int dataLen = m_smbPkt.getParameter(10);
    int dataPos = m_smbPkt.getParameter(11) + RFCNetBIOSProtocol.HEADER_LEN;

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
      logger.debug("File Write AndX [" + netFile.getFileId() + "] : Size="
          + dataLen + " ,Pos=" + offset);

    // Write data to the file

    byte[] buf = m_smbPkt.getBuffer();
    int wrtlen = 0;

    // Access the disk interface and write to the file

    try {
      // Write to the file

      wrtlen = (int)JCRDriver.writeFile(m_sess, conn, netFile, buf, dataPos,
          dataLen, offset);
    } catch (java.io.IOException ex) {

      // Debug

      logger.error("File Write Error [" + netFile.getFileId() + "] : ", ex);

      // Failed to read the file

      m_sess.sendErrorResponseSMB(SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
      return;
    } catch (Exception e) {
      e.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Return the count of bytes actually written

    outPkt.setParameterCount(6);
    outPkt.setAndXCommand(0xFF);
    outPkt.setParameter(1, 0);
    outPkt.setParameter(2, wrtlen);
    outPkt.setParameter(3, 0); // remaining byte count for pipes only
    outPkt.setParameter(4, 0); // reserved
    outPkt.setParameter(5, 0); // "
    outPkt.setByteCount(0);

    // Send the write response

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * runProtocol method comment.
   */
  public boolean runProtocol() throws java.io.IOException, SMBSrvException,
      TooManyConnectionsException {
    logger.debug(":runProtocol");
    // Check if the SMB packet is initialized

    if (m_smbPkt == null)
      m_smbPkt = m_sess.getReceivePacket();

    // Check if the received packet has a valid SMB signature

    if (m_smbPkt.checkPacketSignature() == false)
      throw new IOException("Invalid SMB signature");

    // Determine if the request has a chained command, if so then we will
    // copy the incoming
    // request so that
    // a chained reply can be built.

    SMBSrvPacket outPkt = m_smbPkt;
    boolean chainedCmd = hasChainedCommand(m_smbPkt);

    if (chainedCmd) {

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_STATE))
        logger.debug("AndX Command = 0x"
            + Integer.toHexString(m_smbPkt.getAndXCommand()));

      // Copy the request packet into a new packet for the reply

      outPkt = new SMBSrvPacket(m_smbPkt);
    }

    // Reset the byte unpack offset

    m_smbPkt.resetBytePointer();

    // Determine the SMB command type

    boolean handledOK = true;

    switch (m_smbPkt.getCommand()) {

    // Session setup

    case PacketType.SessionSetupAndX:
      procSessionSetup(outPkt);
      break;

    // Tree connect

    case PacketType.TreeConnectAndX:
      procTreeConnectAndX(outPkt);
      break;

    // Transaction2

    case PacketType.Transaction2:
    case PacketType.Transaction:
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

    // Read a file

    case PacketType.ReadAndX:
      procReadAndX(outPkt);
      break;

    // Write to a file

    case PacketType.WriteAndX:
      procWriteAndX(outPkt);
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

    // Tree connection (without AndX batching)

    case PacketType.TreeConnect:
      super.runProtocol();
      break;

    // Rename file

    case PacketType.RenameFile:
      procRenameFile(outPkt);
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

        // Check if this is a disk or print connection, if so then send
        // the request to the
        // core protocol handler

        if (conn.getSharedDevice().getType() == ShareType.DISK
            || conn.getSharedDevice().getType() == ShareType.PRINTER) {

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

    // Get the transaction sub-command code and validate

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

    // Unknown transact2 command

    default:

      // Return an unrecognized command error

      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      break;
    }
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

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the search parameters

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int srchAttr = paramBuf.getShort();
    int maxFiles = paramBuf.getShort();
    int srchFlag = paramBuf.getShort();
    int infoLevl = paramBuf.getShort();
    paramBuf.skipBytes(4);

    String srchPath = paramBuf.getString(tbuf.isUnicode());

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
    }

    SearchContext ctx = null;

    int searchId = -1;

    try {

      // Allocate a search slot for the new search

      searchId = m_sess.allocateSearchSlot();
      if (searchId == -1) {

        // Failed to allocate a slot for the new search

        m_sess.sendErrorResponseSMB(SMBStatus.SRVNoResourcesAvailable,
            SMBStatus.ErrSrv);
        return;
      }

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
        logger.debug("Start trans search [" + searchId + "] - " + srchPath
            + ", attr=0x" + Integer.toHexString(srchAttr) + ", maxFiles="
            + maxFiles + ", infoLevel=" + infoLevl + ", flags=0x"
            + Integer.toHexString(srchFlag));

      // Start a new search

      ctx = JCRDriver.startSearch(conn, srchPath, srchAttr);

      if (ctx != null) {

        // Store details of the search in the context

        ctx.setTreeId(treeId);
        ctx.setMaximumFiles(maxFiles);
      } else {

        // Failed to start the search, return a no more files error

        m_sess
            .sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
        return;
      }

      // Save the search context

      m_sess.setSearchContext(searchId, ctx);

      // Create the reply transact buffer

      SrvTransactBuffer replyBuf = new SrvTransactBuffer(tbuf);
      DataBuffer dataBuf = replyBuf.getDataBuffer();

      // Determine the maximum return data length

      int maxLen = replyBuf.getReturnDataLimit();

      // Check if resume keys are required

      boolean resumeReq = (srchFlag & FindFirstNext.ReturnResumeKey) != 0 ? true
          : false;

      // Loop until we have filled the return buffer or there are no more
      // files to return

      int fileCnt = 0;
      int packLen = 0;
      int lastNameOff = 0;

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

          // Pack a dummy resume key, if required

          if (resumeReq) {
            dataBuf.putZeros(4);
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

          // No more buffer space

          pktDone = true;
        }
      }

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
        logger.debug("Search [" + searchId + "] Returned " + fileCnt
            + " files, moreFiles=" + ctx.hasMoreFiles());

      // Check if the search is complete

      if (searchDone == true) {

        // Debug

        if (logger.isDebugEnabled()
            && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("End start search [" + searchId + "] (Search complete)");

        // Release the search context

        m_sess.deallocateSearchSlot(searchId);
      }
    } catch (FileNotFoundException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Search path does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
      // m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound,
      // SMBStatus.ErrDos);
    } catch (UnsupportedInfoLevelException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Requested information level is not supported

      m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
    } catch (Exception e) {
      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Requested server error

      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
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

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the search parameters

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int searchId = paramBuf.getShort();
    int maxFiles = paramBuf.getShort();
    int infoLevl = paramBuf.getShort();
    int reskey = paramBuf.getInt();
    int srchFlag = paramBuf.getShort();

    String resumeName = paramBuf.getString(tbuf.isUnicode());

    // Access the shared device disk interface

    SearchContext ctx = null;

    try {

      // Retrieve the search context

      ctx = m_sess.getSearchContext(searchId);
      if (ctx == null) {

        // DEBUG

        if (logger.isDebugEnabled()
            && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("Search context null - [" + searchId + "]");

        // Invalid search handle

        m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
        return;
      }

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
        logger.debug("Continue search [" + searchId + "] - " + resumeName
            + ", maxFiles=" + maxFiles + ", infoLevel=" + infoLevl
            + ", flags=0x" + Integer.toHexString(srchFlag));

      // Create the reply transaction buffer

      SrvTransactBuffer replyBuf = new SrvTransactBuffer(tbuf);
      DataBuffer dataBuf = replyBuf.getDataBuffer();

      // Determine the maximum return data length

      int maxLen = replyBuf.getReturnDataLimit();

      // Check if resume keys are required

      boolean resumeReq = (srchFlag & FindFirstNext.ReturnResumeKey) != 0 ? true
          : false;

      // Loop until we have filled the return buffer or there are no more
      // files to return

      int fileCnt = 0;
      int packLen = 0;
      int lastNameOff = 0;

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

          // Pack a dummy resume key, if required

          if (resumeReq)
            dataBuf.putZeros(4);

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
        logger.debug("Search [" + searchId + "] Returned " + fileCnt
            + " files, moreFiles=" + ctx.hasMoreFiles());

      // Check if the search is complete

      if (searchDone == true) {

        // Debug

        if (logger.isDebugEnabled()
            && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
          logger.debug("End start search [" + searchId + "] (Search complete)");

        // Release the search context

        m_sess.deallocateSearchSlot(searchId);
      }
    } catch (FileNotFoundException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Search path does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
      // m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound,
      // SMBStatus.ErrDos);
    } catch (UnsupportedInfoLevelException ex) {

      // Deallocate the search

      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);

      // Requested information level is not supported

      m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
    } catch (RepositoryException e) {
      if (searchId != -1)
        m_sess.deallocateSearchSlot(searchId);
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
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
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the query file system required information level

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int infoLevl = paramBuf.getShort();

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
      logger.debug("Query File System Info - level = 0x"
          + Integer.toHexString(infoLevl));

    // Access the shared device disk interface

    try {

      // Set the return parameter count, so that the data area position
      // can be calculated.

      outPkt.setParameterCount(10);

      // Pack the disk information into the data area of the transaction
      // reply

      byte[] buf = outPkt.getBuffer();
      int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
      int dataPos = prmPos; // no parameters returned

      // Create a data buffer using the SMB packet. The response should
      // always fit into a
      // single
      // reply packet.

      DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

      // Determine the information level requested

      DiskInfo diskInfo = null;
      VolumeInfo volInfo = null;

      switch (infoLevl) {

      // Standard disk information

      case DiskInfoPacker.InfoStandard:

        // Get the disk information

        diskInfo = conn.getSharedDevice().getDiskInformation();

        // Pack the disk information into the return data packet

        DiskInfoPacker.packStandardInfo(diskInfo, replyBuf);
        break;

      // Volume label information

      case DiskInfoPacker.InfoVolume:

        // Get the volume label information

        volInfo = conn.getSharedDevice().getVolumeInformation();

        // Pack the volume label information

        DiskInfoPacker.packVolumeInfo(volInfo, replyBuf, tbuf.isUnicode());
        break;

      // Full volume information

      case DiskInfoPacker.InfoFsVolume:

        // Get the volume information

        volInfo = conn.getSharedDevice().getVolumeInformation();

        // Pack the volume information

        DiskInfoPacker.packFsVolumeInformation(volInfo, replyBuf, tbuf
            .isUnicode());
        break;

      // Filesystem size information

      case DiskInfoPacker.InfoFsSize:

        // Get the disk information

        diskInfo = conn.getSharedDevice().getDiskInformation();

        // Pack the disk information into the return data packet

        DiskInfoPacker.packFsSizeInformation(diskInfo, replyBuf);
        break;

      // Filesystem device information

      case DiskInfoPacker.InfoFsDevice:
        DiskInfoPacker.packFsDevice(0, 0, replyBuf);
        break;

      // Filesystem attribute information

      case DiskInfoPacker.InfoFsAttribute:
        DiskInfoPacker.packFsAttribute(0, 255, "JLAN", tbuf.isUnicode(),
            replyBuf);
        break;
      }

      // Check if any data was packed, if not then the information level
      // is not supported

      if (replyBuf.getPosition() == dataPos) {
        m_sess
            .sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
        return;
      }

      int dataLen = replyBuf.getLength();
      SMBSrvTransPacket.initTransactReply(outPkt, 0, prmPos, dataLen, dataPos);
      outPkt.setByteCount(replyBuf.getPosition() - outPkt.getByteOffset());

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
   * 
   * 
   * @TODO stream info is not suported!!!
   */
  protected final void procTrans2QueryPath(SrvTransactBuffer tbuf,
      SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException {
    logger.debug(":procTrans2QueryPath");
    // Get the tree connection details

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(treeId);

    if (conn == null) {
      m_sess
          .sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.NTErr);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the query path information level and file/directory name

    DataBuffer paramBuf = tbuf.getParameterBuffer();

    int infoLevl = paramBuf.getShort();
    paramBuf.skipBytes(4);

    String path = paramBuf.getString(tbuf.isUnicode());

    // convert name

    if (path.equals("")) {
      path = "/";
    } else {
      // Convert slashes
      StringBuffer pathb = new StringBuffer(path);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (pathb.charAt(i) == '\\')
          pathb.setCharAt(i, '/');
      }

      path = NameCoder.DecodeName(path.toString());
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
      logger.debug("Query Path - level = 0x" + Integer.toHexString(infoLevl)
          + ", path = " + path);

    try {

      // Set the return parameter count, so that the data area position
      // can be calculated.

      outPkt.setParameterCount(10);

      // Pack the file information into the data area of the transaction
      // reply

      byte[] buf = outPkt.getBuffer();
      int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
      int dataPos = prmPos; // no parameters returned

      // Create a data buffer using the SMB packet. The response should
      // always fit into a
      // single
      // reply packet.

      DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

      // Get the file information

      Node node = (Node) conn.getSession().getItem(path);

      FileInfo fileInfo = JCRDriver.getFileInformation(node);// .getFileInformation(m_sess,
      // conn, path);

      if (fileInfo == null) {
        m_sess
            .sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.NTErr);
        return;
      }

      // Pack the file information into the return data packet

      int dataLen = QueryInfoPacker
          .packInfo(fileInfo, replyBuf, infoLevl, true);

      // Check if any data was packed, if not then the information level
      // is not supported

      if (dataLen == 0) {
        m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
            SMBStatus.NTErr);
        return;
      }

      SMBSrvTransPacket.initTransactReply(outPkt, 0, prmPos, dataLen, dataPos);
      outPkt.setByteCount(replyBuf.getPosition() - outPkt.getByteOffset());

      // Send the transact reply

      m_sess.sendResponseSMB(outPkt);
    } catch (FileNotFoundException ex) {

      // Requested file does not exist

      m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.NTErr);
      return;
    } catch (UnsupportedInfoLevelException ex) {

      // Requested information level is not supported

      m_sess
          .sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.NTErr);
      return;
    } catch (Exception ex) {

      // Failed to get/initialize the disk interface

      m_sess
          .sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.NTErr);
      return;
    }
  }

}
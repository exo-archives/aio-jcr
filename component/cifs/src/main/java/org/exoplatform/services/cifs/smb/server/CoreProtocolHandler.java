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
package org.exoplatform.services.cifs.smb.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.exoplatform.services.cifs.smb.server.VirtualCircuit;
import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.server.filesys.AccessMode;
import org.exoplatform.services.cifs.server.filesys.FileAction;
import org.exoplatform.services.cifs.server.filesys.FileAttribute;
import org.exoplatform.services.cifs.server.filesys.FileExistsException;
import org.exoplatform.services.cifs.server.filesys.FileInfo;
import org.exoplatform.services.cifs.server.filesys.FileOpenParams;
import org.exoplatform.services.cifs.server.filesys.JCRDriver;
import org.exoplatform.services.cifs.server.filesys.JCRNetworkFile;
import org.exoplatform.services.cifs.server.filesys.NameCoder;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.cifs.server.filesys.TooManyConnectionsException;
import org.exoplatform.services.cifs.server.filesys.TooManyFilesException;
import org.exoplatform.services.cifs.server.filesys.TreeConnection;
import org.exoplatform.services.cifs.smb.Capability;
import org.exoplatform.services.cifs.smb.DataType;
import org.exoplatform.services.cifs.smb.PacketType;
import org.exoplatform.services.cifs.smb.SMBDate;
import org.exoplatform.services.cifs.smb.SMBStatus;
import org.exoplatform.services.cifs.util.DataPacker;
import org.exoplatform.services.jcr.core.ExtendedProperty;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.log.ExoLogger;

/**
 * Core SMB protocol handler class.
 */
class CoreProtocolHandler extends ProtocolHandler {

  // Debug logging

  private static final Log logger = ExoLogger
      .getLogger("org.exoplatform.services.cifs.smb.server.CoreProtocolHandler");

  // Special resume ids for '.' and '..' pseudo directories

  private static final int RESUME_START = 0x00008003;

  private static final int RESUME_DOT = 0x00008002;

  private static final int RESUME_DOTDOT = 0x00008001;

  // Maximum value that can be stored in a parameter word

  private static final int MaxWordValue = 0x0000FFFF;

  // Invalid file name characters

  // private static final String InvalidFileNameChars = "[]:+|<>=;,*?"; //\"/

  // private static final String InvalidFileNameCharsSearch = "[]:+|<>=;,";
  // //\"/
  private static final String InvalidFileNameChars = ":|<>*?"; // \"/

  private static final String InvalidFileNameCharsSearch = ":|<>"; // \"/

  // SMB packet class

  protected SMBSrvPacket m_smbPkt;

  /**
   * Create a new core SMB protocol handler.
   */
  protected CoreProtocolHandler() {
  }

  /**
   * Class constructor
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected CoreProtocolHandler(SMBSrvSession sess) {
    super(sess);
  }

  /**
   * Return the protocol name
   * 
   * @return String
   */
  public String getName() {
    return "Core Protocol";
  }

  /**
   * Check if the specified path exists, and is a directory.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              if an SMB protocol error occurs
   */
  protected void procCheckDirectory(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {

    logger.debug(":procCheckDirectory");

    // Check that the received packet looks like a valid check directory
    // request

    if (m_smbPkt.checkPacketIsValid(0, 2) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a
    // valid connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the directory name

    String dirName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());
    if (dirName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Directory Check [" + treeId + "] name=" + dirName);

    if (dirName.equals("")) {
      dirName = "/";
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(dirName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      dirName = NameCoder.DecodeName(path.toString());
    }

    try {

      // Check that the specified path exists, and it is a directory

      Node node = (Node) conn.getSession().getItem(dirName);

      // TODO check situation when directory and file has same name

      if (node.isNodeType("nt:file")) {
        // path is file
        m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid,
            SMBStatus.ErrDos);
      }

    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid,
          SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }
    // The path exists and is a directory, build the valid path
    // response.

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * Close a file that has been opened on the server. And release all locks.
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

    // Get the tree id from the received packet and validate that it is a
    // valid connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the file id from the request

    int fid = m_smbPkt.getParameter(0);

    // LastWriteTime useless in jcr
    int ftime = m_smbPkt.getParameter(1);
    int fdate = m_smbPkt.getParameter(2);

    NetworkFile netFile = conn.findFile(fid);

    // Save changes
    try {

      ((JCRNetworkFile) netFile).saveChanges();

    } catch (Exception e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File close [" + treeId + "] fid=" + fid);

    // Close the file

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
   * Create a new directory.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procCreateDirectory(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procCreateDirectory");

    // Check that the received packet looks like a valid create directory
    // request

    if (m_smbPkt.checkPacketIsValid(0, 2) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a
    // valid connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the directory name

    String dirName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());
    if (dirName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Directory Create [" + treeId + "] name=" + dirName);

    // convert name

    if (dirName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(dirName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      dirName = NameCoder.DecodeName(path.toString());
    }

    // Access the disk interface and create the new directory
    try {
      if (conn.getSession().itemExists(dirName)) {
        // Failed to create the directory
        // TODO is it correct for jcr?

        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision,
            SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
        return;
      }

      // Directory creation parameters
      JCRDriver.createNode(conn.getSession(), dirName, false);

    } catch (AccessDeniedException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied,
          SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    } catch (LockException e) {
      // it probably never thrown
      e.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    // Build the create directory response
    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Create a new file on the server.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procCreateFile(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procCreateFile");
    // Check that the received packet looks like a valid file create request

    if (m_smbPkt.checkPacketIsValid(3, 2) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the file name

    String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // convert name

    if (fileName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;

    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = NameCoder.DecodeName(path.toString());
    }

    // Get the required file attributes for the new file

    int attr = m_smbPkt.getParameter(0);
    // get time
    int ftime = m_smbPkt.getParameter(1);
    int fdate = m_smbPkt.getParameter(2);

    // Create the file parameters to be passed to the disk interface

    FileOpenParams params = new FileOpenParams(fileName,
        FileAction.CreateNotExist, AccessMode.ReadWrite, attr);

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Create [" + treeId + "] params=" + params);

    // Access the disk interface and create the new file

    int fid;
    NetworkFile netFile = null;

    try {

      // Create the new file

      netFile = JCRDriver.createFile(conn, params);

      // Add the file to the list of open files for this tree connection

      fid = conn.addFile(netFile, getSession());

    } catch (AccessDeniedException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (TooManyFilesException ex) {

      // Too many files are open on this connection, cannot open any more files.

      m_sess.sendErrorResponseSMB(SMBStatus.DOSTooManyOpenFiles,
          SMBStatus.ErrDos);
      return;

    } catch (FileExistsException ex) {

      // File with the requested name already exists

      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileAlreadyExists,
          SMBStatus.ErrDos);
      return;

    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }

    // Build the create file response

    outPkt.setParameterCount(1);
    outPkt.setParameter(0, fid);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Create a temporary file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procCreateTemporaryFile(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procCreateTemporaryFile EMPTY");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Delete a directory.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
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

    // Get the tree id from the received packet and validate that it is a
    // valid connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the directory name

    String dirName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());

    if (dirName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Directory Delete [" + treeId + "] name=" + dirName);

    if (dirName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrSrv);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(dirName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      dirName = NameCoder.DecodeName(path.toString());
    }

    try {

      // Delete file(s)
      Session sess = conn.getSession();
      Node node = (Node) sess.getItem(dirName);
      node.remove();

      sess.save();

    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (LockException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    }
    // Build the delete directory response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Delete a file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
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

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the file name

    String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Delete tid[" + treeId + "] name= [" + fileName + "]");

    if (fileName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrSrv);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = NameCoder.DecodeName(path.toString());
    }

    try {

      // Delete file(s)
      Session sess = conn.getSession();
      Node node = (Node) sess.getItem(fileName);
      node.remove();

      sess.save();

    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (AccessDeniedException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (LockException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException ex) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    }
    // Build the delete file response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Get disk attributes processing.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procDiskAttributes(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procDiskAttributes EMPTY");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Echo packet request.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procEcho(SMBSrvPacket outPkt) throws java.io.IOException,
      SMBSrvException {

    logger.debug("\nCoreProtocolHandler::procEcho");

    // Check that the received packet looks like a valid echo request

    if (m_smbPkt.checkPacketIsValid(1, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the echo count from the request

    int echoCnt = m_smbPkt.getParameter(0);

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_ECHO))
      logger.debug("Echo - Count = " + echoCnt);

    // Loop until all echo packets have been sent

    int echoSeq = 1;

    while (echoCnt > 0) {

      // Set the echo response sequence number

      outPkt.setParameter(0, echoSeq++);

      // Echo the received packet

      m_sess.sendResponseSMB(outPkt);
      echoCnt--;

      // Debug

      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_ECHO))
        logger.debug("Echo Packet, Seq = " + echoSeq);
    }
  }

  /**
   * Flush the specified file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procFlushFile(SMBSrvPacket outPkt) throws java.io.IOException,
      SMBSrvException {
    logger.debug(":procFlushFile");
    // Check that the received packet looks like a valid file flush request

    if (m_smbPkt.checkPacketIsValid(1, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the file id from the request

    int fid = m_smbPkt.getParameter(0);

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Flush [" + netFile.getFileId() + "]");

    // Flush the file

    try {
      if (netFile instanceof JCRNetworkFile) {
        ((JCRNetworkFile) netFile).flush();
      } else {
        // we don't support non-jcr file flush
        m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
        return;
      }
    } catch (java.io.IOException ex) {
      // Debug
      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
        logger.debug("File Flush Error [" + netFile.getFileId() + "] : " +
            ex.toString());

      // Failed to read the file

      m_sess.sendErrorResponseSMB(SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
      return;
    } catch (RepositoryException e) {

      e.printStackTrace();

      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;

    }

    // Send the flush response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Get the file attributes for the specified file.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procGetFileAttributes(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procGetFileAttributes");

    // Check that the received packet looks like a valid query file information
    // request

    if (m_smbPkt.checkPacketIsValid(0, 2) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the file name

    String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Get File Information tid[" + treeId + "] name= [" +
          fileName + "]");

    // Access the disk interface and get the file information

    try {
      // Get the file information for the specified file/directory

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
      String name = NameCoder.DecodeName(fileName);

      Node n = (Node) conn.getSession().getItem(name);

      FileInfo finfo = JCRDriver.getFileInformation(n);
      if (finfo != null) {

        // Check if the share is read-only, if so then force the read-only flag
        // for the file

        if (conn.getSharedDevice().isReadOnly() && finfo.isReadOnly() == false) {

          // Make sure the read-only attribute is set

          finfo.setFileAttributes(finfo.getFileAttributes() +
              FileAttribute.ReadOnly);
        }

        // Return the file information

        outPkt.setParameterCount(10);
        outPkt.setParameter(0, finfo.getFileAttributes());
        if (finfo.getModifyDateTime() != 0L) {
          SMBDate dateTime = new SMBDate(finfo.getModifyDateTime());
          outPkt.setParameter(1, dateTime.asSMBTime());
          outPkt.setParameter(2, dateTime.asSMBDate());
        } else {
          outPkt.setParameter(1, 0);
          outPkt.setParameter(2, 0);
        }
        outPkt.setParameter(3, (int) finfo.getSize() & 0x0000FFFF);
        outPkt.setParameter(4, (int) (finfo.getSize() & 0xFFFF0000) >> 16);

        for (int i = 5; i < 10; i++)
          outPkt.setParameter(i, 0);

        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
        return;
      }
    } catch (PathNotFoundException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError,
          SMBStatus.ErrSrv);
      return;
    }
    // Failed to get the file information

    m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
  }

  /**
   * Get file information.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procGetFileInformation(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procGetFileInformation");
    // Check that the received packet looks like a valid query file information2
    // request

    if (m_smbPkt.checkPacketIsValid(1, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
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
      logger.debug("Get File Information 2 [" + netFile.getFileId() + "]");

    // Access the disk interface and get the file information

    try {

      // Get the file information for the specified file/directory

      FileInfo finfo = JCRDriver.getFileInformation(((JCRNetworkFile) netFile)
          .getNodeRef());
      if (finfo != null) {

        // Check if the share is read-only, if so then force the read-only flag
        // for the file

        if (conn.getSharedDevice().isReadOnly() && finfo.isReadOnly() == false) {

          // Make sure the read-only attribute is set

          finfo.setFileAttributes(finfo.getFileAttributes() +
              FileAttribute.ReadOnly);
        }

        // Initialize the return packet, no data bytes

        outPkt.setParameterCount(11);
        outPkt.setByteCount(0);

        // Return the file information

        // Creation date/time

        SMBDate dateTime = new SMBDate(0);

        if (finfo.getCreationDateTime() != 0L) {
          dateTime.setTime(finfo.getCreationDateTime());
          outPkt.setParameter(0, dateTime.asSMBDate());
          outPkt.setParameter(1, dateTime.asSMBTime());
        } else {
          outPkt.setParameter(0, 0);
          outPkt.setParameter(1, 0);
        }

        // Access date/time

        if (finfo.getAccessDateTime() != 0L) {
          dateTime.setTime(finfo.getAccessDateTime());
          outPkt.setParameter(2, dateTime.asSMBDate());
          outPkt.setParameter(3, dateTime.asSMBTime());
        } else {
          outPkt.setParameter(2, 0);
          outPkt.setParameter(3, 0);
        }

        // Modify date/time

        if (finfo.getModifyDateTime() != 0L) {
          dateTime.setTime(finfo.getModifyDateTime());
          outPkt.setParameter(4, dateTime.asSMBDate());
          outPkt.setParameter(5, dateTime.asSMBTime());
        } else {
          outPkt.setParameter(4, 0);
          outPkt.setParameter(5, 0);
        }

        // File data size

        outPkt.setParameter(6, (int) finfo.getSize() & 0x0000FFFF);
        outPkt.setParameter(7, (int) (finfo.getSize() & 0xFFFF0000) >> 16);

        // File allocation size

        outPkt.setParameter(8, (int) finfo.getSize() & 0x0000FFFF);
        outPkt.setParameter(9, (int) (finfo.getSize() & 0xFFFF0000) >> 16);

        // File attributes

        outPkt.setParameter(10, finfo.getFileAttributes());

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
        return;
      }
    } catch (Exception ex) {

      // Failed to get/initialize the disk interface

      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Failed to get the file information

    m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);

  }

  /**
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procLockFile(SMBSrvPacket outPkt) throws java.io.IOException,
      SMBSrvException {
    logger.debug(":procLockFile EMPTY");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Open a file on the server.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procOpenFile(SMBSrvPacket outPkt) throws IOException,
      SMBSrvException {
    logger.debug(":procOpenFile");
    // Check that the received packet looks like a valid file open request

    if (m_smbPkt.checkPacketIsValid(2, 2) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the file name

    String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // convert name

    if (fileName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = NameCoder.DecodeName(path.toString());
    }

    // Get the required access mode and the file attributes

    int mode = m_smbPkt.getParameter(0);
    int attr = m_smbPkt.getParameter(1);

    // Create the file open parameters to be passed to the disk interface

    FileOpenParams params = new FileOpenParams(fileName, mode,
        AccessMode.ReadWrite, attr);
    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("File Open [" + treeId + "] params=" + params);

    // Access the disk interface and open the requested file

    int fid;
    NetworkFile netFile = null;

    try {

      // Open the requested file

      netFile = JCRDriver.openFile(conn, params);

      // Add the file to the list of open files for this tree connection

      fid = conn.addFile(netFile, getSession());

    }

    catch (TooManyFilesException ex) {

      // Too many files are open on this connection, cannot open any more files.

      m_sess.sendErrorResponseSMB(SMBStatus.DOSTooManyOpenFiles,
          SMBStatus.ErrDos);
      return;
    } catch (Exception e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Build the open file response

    outPkt.setParameterCount(7);

    outPkt.setParameter(0, fid);
    outPkt.setParameter(1, 0); // file attributes

    if (netFile.hasModifyDate()) {
      outPkt.setParameterLong(2, (int) (netFile.getModifyDate() / 1000L));

      // SMBDate smbDate = new SMBDate(netFile.getModifyDate());
      // outPkt.setParameter(2, smbDate.asSMBTime()); // last write time
      // outPkt.setParameter(3, smbDate.asSMBDate()); // last write date
    } else
      outPkt.setParameterLong(2, 0);

    outPkt.setParameterLong(4, netFile.getFileSizeInt()); // file size
    outPkt.setParameter(6, netFile.getGrantedAccess());

    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Process exit, close all open files.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procProcessExit(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procExit");
    // Check that the received packet looks like a valid process exit request

    if (m_smbPkt.checkPacketIsValid(0, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Process Exit - Open files = " + conn.openFileCount());

    // Close all open files

    if (conn.openFileCount() > 0) {

      // Close all files on the connection

      conn.closeConnection(getSession());
    }

    // Build the process exit response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Read from a file that has been opened on the server.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procReadFile(SMBSrvPacket outPkt) throws java.io.IOException,
      SMBSrvException {
    logger.debug(":procReadFile");

    // Check that the received packet looks like a valid file read request

    if (m_smbPkt.checkPacketIsValid(5, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Check if the user has the required access permission

    if (conn.hasReadAccess() == false) {

      // User does not have the required access rights

      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    }

    // Get the file id from the request

    int fid = m_smbPkt.getParameter(0);
    int reqcnt = m_smbPkt.getParameter(1);
    int reqoff = m_smbPkt.getParameter(2) + (m_smbPkt.getParameter(3) << 16);

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
      logger.debug("File Read [" + netFile.getFileId() + "] : Size=" + reqcnt +
          " ,Pos=" + reqoff);

    // Read data from the file

    byte[] buf = outPkt.getBuffer();
    int rdlen = 0;

    try {
      // Check if the required read size will fit into the reply packet

      int dataOff = outPkt.getByteOffset() + 3;
      int availCnt = buf.length - dataOff;
      if (m_sess.hasClientCapability(Capability.LargeRead) == false)
        availCnt = m_sess.getClientMaximumBufferSize() - dataOff;

      if (availCnt < reqcnt) {

        // Limit the file read size

        reqcnt = availCnt;

        // Debug

        if (logger.isDebugEnabled() &&
            m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
          logger.debug("File Read [" + netFile.getFileId() + "] Limited to " +
              availCnt);
      }

      // Read from the file
      rdlen = ((JCRNetworkFile) netFile).read(buf, outPkt.getByteOffset() + 3,
          reqcnt, reqoff);

    } catch (java.io.IOException ex) {
      m_sess.sendErrorResponseSMB(SMBStatus.HRDReadFault, SMBStatus.ErrHrd);
      return;
    } catch (RepositoryException e) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Return the data block

    int bytOff = outPkt.getByteOffset();
    buf[bytOff] = (byte) DataType.DataBlock;
    DataPacker.putIntelShort(rdlen, buf, bytOff + 1);
    outPkt.setByteCount(rdlen + 3); // data type + 16bit length

    outPkt.setParameter(0, rdlen);
    outPkt.setParameter(1, 0);
    outPkt.setParameter(2, 0);
    outPkt.setParameter(3, 0);
    outPkt.setParameter(4, 0);

    // Send the read response

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
    logger.debug(":procRenameFile EMPTY");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Start/continue a directory search operation.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected final void procSearch(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procSearch EMPTY");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Process a search request that is for the volume label.
   * 
   * @param outPkt
   *          SMBSrvPacket
   */
  protected final void procSearchVolumeLabel(SMBSrvPacket outPkt)
      throws IOException, SMBSrvException {
    logger.debug(":procSearchVolumeLabel EMPTY");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Seek to the specified file position within the open file.
   * 
   * @param pkt
   *          SMBSrvPacket
   */
  protected final void procSeekFile(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procSeekFile EMPTY");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Process the SMB session setup request.
   * 
   * @param outPkt
   *          Response SMB packet.
   */

  protected void procSessionSetup(SMBSrvPacket outPkt) throws SMBSrvException,
      IOException, TooManyConnectionsException {
    logger.debug("\nCoreProtocolHandler::procSessionSetup");

    // Build the session setup response SMB

    outPkt.setParameterCount(3);
    outPkt.setParameter(0, 0);
    outPkt.setParameter(1, 0);
    outPkt.setParameter(2, 8192);
    outPkt.setByteCount(0);

    outPkt.setTreeId(0);
    outPkt.setUserId(0);

    // Pack the OS, dialect and domain name strings.

    int pos = outPkt.getByteOffset();
    byte[] buf = outPkt.getBuffer();

    pos = DataPacker.putString("Java", buf, pos, true);
    pos = DataPacker.putString("JLAN Server " + m_sess.getServer().isVersion(),
        buf, pos, true);
    pos = DataPacker.putString(m_sess.getServer().getConfiguration()
        .getDomainName(), buf, pos, true);

    outPkt.setByteCount(pos - outPkt.getByteOffset());

    // Send the negotiate response

    m_sess.sendResponseSMB(outPkt);

    // Update the session state

    m_sess.setState(SMBSrvSessionState.SMBSESSION);
  }

  /**
   * Set the file attributes for a file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procSetFileAttributes(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug(":procSetFileAttributes");
    // Check that the received packet looks like a valid set file attributes
    // request

    if (m_smbPkt.checkPacketIsValid(8, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the data bytes position and length

    int dataPos = m_smbPkt.getByteOffset();
    int dataLen = m_smbPkt.getByteCount();
    byte[] buf = m_smbPkt.getBuffer();

    // Extract the file name

    String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos,
        dataLen, m_smbPkt.isUnicode());
    if (fileName == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // convert name

    if (fileName.equals("")) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
      return;
    } else {
      // Convert slashes
      StringBuffer path = new StringBuffer(fileName);
      for (int i = 0; i < path.length(); i++) {

        // Convert back slashes to forward slashes

        if (path.charAt(i) == '\\')
          path.setCharAt(i, '/');
      }

      fileName = NameCoder.DecodeName(path.toString());
    }

    // Get the file attributes

    int fattr = m_smbPkt.getParameter(0);
    int setFlags = FileInfo.SetAttributes;

    FileInfo finfo = new FileInfo(fileName, 0, fattr);

    int fdate = m_smbPkt.getParameter(1);
    int ftime = m_smbPkt.getParameter(2);

    if (fdate != 0 && ftime != 0) {
      finfo.setModifyDateTime(new SMBDate(fdate, ftime).getTime());
      setFlags += FileInfo.SetModifyDate;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Set File Attributes [" + treeId + "] name=" + fileName +
          ", attr=0x" + Integer.toHexString(fattr) + ", fdate=" + fdate +
          ", ftime=" + ftime);

    // Access the disk interface and set the file attributes

    try {

      // Get the file information for the specified file/directory

      finfo.setFileInformationFlags(setFlags);
      JCRDriver.setFileInformation(m_sess, conn, fileName, finfo);

      // m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData,
      // SMBStatus.ErrDos);
      // return;
    } catch (Exception ex) {
    }

    // Return the set file attributes response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Set file information.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procSetFileInformation(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {

    logger.debug(":procSetFileInformation");
    // Check that the received packet looks like a valid set file information2
    // request
    if (m_smbPkt.checkPacketIsValid(7, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the file id from the request, and get the network file details.

    int fid = m_smbPkt.getParameter(0);
    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Get the creation date/time from the request

    int setFlags = 0;
    FileInfo finfo = new FileInfo(netFile.getName(), 0, 0);

    int fdate = m_smbPkt.getParameter(1);
    int ftime = m_smbPkt.getParameter(2);

    if (fdate != 0 && ftime != 0) {
      finfo.setCreationDateTime(new SMBDate(fdate, ftime).getTime());
      setFlags += FileInfo.SetCreationDate;
    }

    // Get the last access date/time from the request

    fdate = m_smbPkt.getParameter(3);
    ftime = m_smbPkt.getParameter(4);

    if (fdate != 0 && ftime != 0) {
      finfo.setAccessDateTime(new SMBDate(fdate, ftime).getTime());
      setFlags += FileInfo.SetAccessDate;
    }

    // Get the last write date/time from the request

    fdate = m_smbPkt.getParameter(5);
    ftime = m_smbPkt.getParameter(6);

    if (fdate != 0 && ftime != 0) {
      finfo.setModifyDateTime(new SMBDate(fdate, ftime).getTime());
      setFlags += FileInfo.SetModifyDate;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
      logger.debug("Set File Information 2 [" + netFile.getFileId() + "] " +
          finfo.toString());

    // Access the disk interface and set the file information

    try {

      // Get the file information for the specified file/directory

      finfo.setFileInformationFlags(setFlags);
      // TODO Check permissions on the file/folder node

      // Check if the file is being marked for deletion, if so then check if the
      // file is locked

      if (finfo.hasSetFlag(FileInfo.SetDeleteOnClose) &&
          finfo.hasDeleteOnClose()) {
        // Check if the node is locked
      }

    } catch (Exception ex) {
      // Failed to get/initialize the disk interface
      ex.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Return the set file information response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    // Send the response packet

    m_sess.sendResponseSMB(outPkt);
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

  protected void procTreeConnect(SMBSrvPacket outPkt) throws SMBSrvException,
      TooManyConnectionsException, java.io.IOException {
    logger.debug("\nCoreProtocolHandler::procTreeConnect");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Process the SMB tree disconnect request.
   * 
   * @param outPkt
   *          Response SMB packet.
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procTreeDisconnect(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug("procTreeDisconnect");
    // Check that the received packet looks like a valid tree disconnect request

    if (m_smbPkt.checkPacketIsValid(0, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the virtual circuit for the request

    VirtualCircuit vc = m_sess.findVirtualCircuit(m_smbPkt.getUserId());
    if (vc == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter,
          SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a
    // valid
    // connection id.

    int treeId = m_smbPkt.getTreeId();
    TreeConnection conn = vc.findTreeConnection(treeId);

    if (conn == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
      logger.debug("Tree disconnect - " + treeId + ", " + conn.toString());

    // Remove the specified connection from the session

    vc.removeConnection(treeId, m_sess);

    // Build the tree disconnect response

    outPkt.setParameterCount(0);
    outPkt.setByteCount(0);

    m_sess.sendResponseSMB(outPkt);
  }

  /**
   * Unlock a byte range in the specified file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procUnLockFile(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug("\nCoreProtocolHandler::procUnLockDirectory");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Unsupported SMB procesing.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected final void procUnsupported(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {

    // Send an unsupported error response

    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Write to a file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procWriteFile(SMBSrvPacket outPkt) throws java.io.IOException,
      SMBSrvException {
    logger.debug(":procWriteFile");
    // Check that the received packet looks like a valid file write request

    if (m_smbPkt.checkPacketIsValid(5, 0) == false) {
      m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand,
          SMBStatus.ErrSrv);
      return;
    }

    // Get the tree id from the received packet and validate that it is a valid
    // connection id.

    TreeConnection conn = m_sess.findTreeConnection(m_smbPkt);

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

    // Get the file id from the request

    int fid = m_smbPkt.getParameter(0);
    int wrtcnt = m_smbPkt.getParameter(1);
    long wrtoff = (m_smbPkt.getParameter(2) + (m_smbPkt.getParameter(3) << 16)) & 0xFFFFFFFFL;
    int cleft = m_smbPkt.getParameter(4);

    NetworkFile netFile = conn.findFile(fid);

    if (netFile == null) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
      return;
    }

    // Debug

    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
      logger.debug("File Write [" + netFile.getFileId() + "] : Size=" + wrtcnt +
          " ,Pos=" + wrtoff + " , CountLeft=" + cleft);

    // Write data to the file

    byte[] buf = m_smbPkt.getBuffer();
    int pos = m_smbPkt.getByteOffset();
    int wrtlen = 0;

    // Check that the data block is valid

    if (buf[pos] != DataType.DataBlock) {
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    try {

      // Update the buffer position to the start of the data to be written

      pos += 3;

      // Check for a zero length write, this should truncate/extend the file to
      // the write
      // offset position
      if (wrtcnt == 0) {

        ((JCRNetworkFile) netFile).truncateFile(wrtoff);

      } else {

        // Write to the file
        ((JCRNetworkFile) netFile).updateFile(new ByteArrayInputStream(buf,
            pos, wrtcnt), wrtcnt, wrtoff);
        wrtlen=wrtcnt;
        
      }
    } catch (java.io.IOException ex) {

      // Debug
      if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
        logger.debug("File Write Error [" + netFile.getFileId() + "] : " +
            ex.toString());
      // Failed to read the file
      m_sess.sendErrorResponseSMB(SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
      return;
    } catch (Exception ex) {

      // Failed to get/initialize the disk interface
      ex.printStackTrace();
      m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
      return;
    }

    // Return the count of bytes actually written

    outPkt.setParameterCount(1);
    outPkt.setParameter(0, wrtlen);
    outPkt.setByteCount(0);

    // Send the write response

    m_sess.sendResponseSMB(outPkt);

  }

  /**
   * Write to a file then close the file.
   * 
   * @param outPkt
   *          SMBSrvPacket
   * @exception java.io.IOException
   *              If an I/O error occurs
   * @exception SMBSrvException
   *              If an SMB protocol error occurs
   */
  protected void procWriteAndCloseFile(SMBSrvPacket outPkt)
      throws java.io.IOException, SMBSrvException {
    logger.debug("\nCoreProtocolHandler::procWriteAndCloseFile");
    m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
  }

  /**
   * Run the core SMB protocol handler.
   * 
   * @return boolean true if the packet was processed, else false
   */
  public boolean runProtocol() throws java.io.IOException, SMBSrvException,
      TooManyConnectionsException {
    logger.debug(":runProtocol()");
    // Check if the SMB packet is initialized

    if (m_smbPkt == null)
      m_smbPkt = new SMBSrvPacket(m_sess.getBuffer());

    // Determine the SMB command type

    boolean handledOK = true;
    SMBSrvPacket outPkt = m_smbPkt;

    switch (m_smbPkt.getCommand()) {

    // Session setup

    case PacketType.SessionSetupAndX:
      procSessionSetup(outPkt);
      break;

    // Tree connect

    case PacketType.TreeConnect:
      procTreeConnect(outPkt);
      break;

    // Tree disconnect

    case PacketType.TreeDisconnect:
      procTreeDisconnect(outPkt);
      break;

    // Search

    case PacketType.Search:
      procSearch(outPkt);
      break;

    // Get disk attributes

    case PacketType.DiskInformation:
      procDiskAttributes(outPkt);
      break;

    // Get file attributes

    case PacketType.GetFileAttributes:
      procGetFileAttributes(outPkt);
      break;

    // Set file attributes

    case PacketType.SetFileAttributes:
      procSetFileAttributes(outPkt);
      break;

    // Get file information

    case PacketType.QueryInformation2:
      procGetFileInformation(outPkt);
      break;

    // Set file information

    case PacketType.SetInformation2:
      procSetFileInformation(outPkt);
      break;

    // Open a file

    case PacketType.OpenFile:
      procOpenFile(outPkt);
      break;

    // Read from a file

    case PacketType.ReadFile:
      procReadFile(outPkt);
      break;

    // Seek file

    case PacketType.SeekFile:
      procSeekFile(outPkt);
      break;

    // Close a file

    case PacketType.CloseFile:
      procCloseFile(outPkt);
      break;

    // Create a new file

    case PacketType.CreateFile:
    case PacketType.CreateNew:
      procCreateFile(outPkt);
      break;

    // Write to a file

    case PacketType.WriteFile:
      procWriteFile(outPkt);
      break;

    // Write to a file, then close the file

    case PacketType.WriteAndClose:
      procWriteAndCloseFile(outPkt);
      break;

    // Flush file

    case PacketType.FlushFile:
      procFlushFile(outPkt);
      break;

    // Rename a file

    case PacketType.RenameFile:
      procRenameFile(outPkt);
      break;

    // Delete a file

    case PacketType.DeleteFile:
      procDeleteFile(outPkt);
      break;

    // Create a new directory

    case PacketType.CreateDirectory:
      procCreateDirectory(outPkt);
      break;

    // Delete a directory

    case PacketType.DeleteDirectory:
      procDeleteDirectory(outPkt);
      break;

    // Check if a directory exsts

    case PacketType.CheckDirectory:
      procCheckDirectory(outPkt);
      break;

    // Unsupported requests

    case PacketType.IOCtl:
      procUnsupported(outPkt);
      break;

    // Echo request

    case PacketType.Echo:
      procEcho(outPkt);
      break;

    // Process exit request

    case PacketType.ProcessExit:
      procProcessExit(outPkt);
      break;

    // Create temoporary file request

    case PacketType.CreateTemporary:
      procCreateTemporaryFile(outPkt);
      break;

    // Lock file request

    case PacketType.LockFile:
      procLockFile(outPkt);
      break;

    // Unlock file request

    case PacketType.UnLockFile:
      procUnLockFile(outPkt);
      break;

    // Default

    default:

      // Indicate that the protocol handler did not process the SMB
      // request

      handledOK = false;
      break;
    }

    // Return the handled status

    return handledOK;
  }

  /**
   * Check if a path contains any illegal characters, for file/create
   * open/create/rename/get info
   * 
   * @param path
   *          String
   * @return boolean
   */
  protected boolean isValidPath(String path) {
    // Scan the path for invalid path characters

    for (int i = 0; i < InvalidFileNameChars.length(); i++) {
      if (path.indexOf(InvalidFileNameChars.charAt(i)) != -1)
        return false;
    }

    // Path looks valid

    return true;
  }

  protected String unpackUnicode(byte[] byt, int pos, int maxlen) {
    // Check for an empty string

    if (maxlen == 0)
      return "";

    // Search for the trailing null

    int maxpos = pos + (maxlen * 2);
    int endpos = pos;
    char[] chars = new char[maxlen];
    int cpos = 0;
    char curChar;

    do {

      // Get a Unicode character from the buffer

      curChar = (char) (((byt[endpos] & 0xFF) << 8) + (byt[endpos + 1] & 0xFF));

      // Add the character to the array

      chars[cpos++] = curChar;

      // Update the buffer pointer

      endpos += 2;

    } while (curChar != 0 && endpos < maxpos);

    // Check if we reached the end of the buffer

    if (endpos <= maxpos) {
      if (curChar == 0)
        cpos--;
      return new String(chars, 0, cpos);
    }
    return null;

  }

  
  
}
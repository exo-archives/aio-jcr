/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs.smb.server;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cifs.server.SrvSession;
import org.exoplatform.services.cifs.server.filesys.NetworkFile;
import org.exoplatform.services.cifs.server.filesys.TreeConnection;
import org.exoplatform.services.cifs.smb.NTIOCtl;
import org.exoplatform.services.cifs.smb.SMBException;
import org.exoplatform.services.cifs.smb.SMBStatus;
import org.exoplatform.services.cifs.util.DataBuffer;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Sergiy Karpenko
 */

public class IOControlHandler {
  private static Log         logger           = ExoLogger
                                                  .getLogger("org.exoplatform.services.cifs.smb.server.IOControlHandler");

  // Custom I/O control codes

  public static final int    CmdProbe         = NTIOCtl.FsCtlCustom;

  public static final int    CmdFileStatus    = NTIOCtl.FsCtlCustom + 1;

  // Version 1 CmdCheckOut = NTIOCtl.FsCtlCustom + 2
  // Version 1 CmdCheckIn = NTIOCtl.FsCtlCustom + 3
  public static final int    CmdGetActionInfo = NTIOCtl.FsCtlCustom + 4;

  public static final int    CmdRunAction     = NTIOCtl.FsCtlCustom + 5;

  public static final int    CmdGetAuthTicket = NTIOCtl.FsCtlCustom + 6;

  // I/O control request/response signature

  public static final String Signature        = "EXOPLATFORM";

  public static DataBuffer processIOControl(SrvSession sess, TreeConnection conn, int ctrlCode,
      int fid, DataBuffer dataBuf, boolean isFSCtrl, int filter) throws SMBException,
      IOControlNotImplementedException {
    // Validate the file id

    NetworkFile netFile = conn.findFile(fid);
    if (netFile == null || netFile.isDirectory() == false)
      throw new SMBException(SMBStatus.NTErr, SMBStatus.NTInvalidParameter);

    // Split the control code

    int devType = NTIOCtl.getDeviceType(ctrlCode);
    int ioFunc = NTIOCtl.getFunctionCode(ctrlCode);

    // Check for I/O controls that require a success status

    if (devType == NTIOCtl.DeviceFileSystem) {
      // I/O control requests that require a success status
      //
      // Create or get object id

      if (ioFunc == NTIOCtl.FsCtlCreateOrGetObjectId)
        return null;
    }

    // Check if the I/O control looks like a custom I/O control request

    if (devType != NTIOCtl.DeviceFileSystem || dataBuf == null)
      throw new IOControlNotImplementedException();

    // Check if the request has a valid signature for an eXo Platform CIFS
    // server I/O control

    if (dataBuf.getLength() < Signature.length())
      throw new IOControlNotImplementedException("Bad request length");

    String sig = dataBuf.getString(Signature.length(), false);

    if (sig == null || sig.compareTo(Signature) != 0)
      throw new IOControlNotImplementedException("Bad request signature");

    // Get the node for the parent folder, make sure it is a folder

    Node rootNode = null;

    try {
      rootNode = conn.getSession().getRootNode();

    } catch (Exception e) {
      // TODO make correct error processing
      throw new SMBException(SMBStatus.NTErr, SMBStatus.NTAccessDenied);
    }

    // Debug
    if (logger.isDebugEnabled()) {
      logger.debug("IO control func=0x" + Integer.toHexString(ioFunc) + ", fid=" + fid
          + ", buffer=" + dataBuf);
    }

    // Check if the I/O control code is one of our custom codes

    DataBuffer retBuffer = null;

    switch (ioFunc) {

    // Probe to check if this is an eXo CIFS server

    case CmdProbe:

      // Return a buffer with the signature and protocol version

      retBuffer = new DataBuffer(Signature.length());
      retBuffer.putFixedString(Signature, Signature.length());
      retBuffer.putInt(0); // DesktopAction.StsSuccess
      retBuffer.putInt(2); // I/O control interface version id
      break;

    // Get file information for a file within the current folder

    case CmdFileStatus:

      // Process the file status request

      retBuffer = procIOFileStatus(sess, conn, dataBuf, rootNode);
      break;

    // Get action information for the specified executable path

    case CmdGetActionInfo:

      // Process the get action information request

      retBuffer = procGetActionInfo(sess, conn, dataBuf, rootNode, netFile);
      break;

    // Run the named action

    case CmdRunAction:

      // Process the run action request

      retBuffer = procRunAction(sess, conn, dataBuf, rootNode, netFile);
      break;

    // Return the authentication ticket

    /*
     * case CmdGetAuthTicket: // Process the get auth ticket request retBuffer =
     * procGetAuthTicket(sess, conn, dataBuf, rootNode, netFile); break;
     */
    // Unknown I/O control code
    default:
      throw new IOControlNotImplementedException();
    }

    // Return the reply buffer, may be null

    return retBuffer;
  }

  /**
   * Process the file status I/O request
   * 
   * @param sess Server session
   * @param tree Tree connection
   * @param reqBuf Request buffer
   * @param folderNode NodeRef of parent folder
   * @return DataBuffer
   * @throws SMBException
   */
  private final static DataBuffer procIOFileStatus(SrvSession sess, TreeConnection tree,
      DataBuffer reqBuf, Node folderNode) throws SMBException {
    // Get the file name from the request

    String fName = reqBuf.getString(true);

    if (logger.isDebugEnabled())
      logger.debug("  File status, fname=" + fName);

    // Create a response buffer

    DataBuffer respBuf = new DataBuffer(256);
    respBuf.putFixedString(Signature, Signature.length());

    // Get the node for the file/folder

    Node childNode = null;

    try {
      childNode = folderNode.getNode(fName);
    } catch (Exception ex) {
      // Return an error response

      respBuf.putInt(2); // DesktopAction.StsFileNotFound
      return respBuf;
    }

    try {

      // Check if this is a file or folder node
      if (!childNode.isNodeType("nt:file")) {
        // Only return the status and node type for folders

        respBuf.putInt(0);// DesktopAction.StsSuccess
        respBuf.putInt(1); // type - folder
      } else {
        // Indicate that this is a file node

        respBuf.putInt(0); // DesktopAction.StsSuccess
        respBuf.putInt(0); // type - file

        // TODO Check if this file is a working copy

        // Not a working copy

        respBuf.putInt(0); // false

        // TODO Check the lock status of the file

        // Pack the lock type, and owner if there is a lock on the file
        String strLockType = null;

        if (strLockType == null)
          respBuf.putInt(0); // Lock None
        else {

          if (strLockType == "READONLY")

            respBuf.putInt(strLockType.equalsIgnoreCase("READONLY") ? 1 : 0);
          respBuf.putString(/* lock owner name */"", true, true);
        }

        // Get the content data details for the file

        if (false) {
          long size = 0L;

          Node contentNode = childNode.getNode("jcr:content");
          Property dataProp = contentNode.getProperty("jcr:data");

          if (dataProp != null) {
            size = dataProp.getLength();
          }

          // Pack the content length and mime-type

          respBuf.putInt(1); // has contant data details
          respBuf.putLong(size); // Content data size
          respBuf.putString(/* mimetype name */"", true, true); // mimetype name
        } else {
          // File does not have any content

          respBuf.putInt(0);
        }
      }
    } catch (Exception e) {
      // TODO check!
      throw new SMBException(SMBStatus.SRVInternalServerError, SMBStatus.ErrSrv);

    }

    // Return the response

    return respBuf;
  }

  /**
   * Process the get action information request
   * 
   * @param sess Server session
   * @param tree Tree connection
   * @param reqBuf Request buffer
   * @param folderNode NodeRef of parent folder
   * @param netFile NetworkFile for the folder
   * @return DataBuffer
   */
  private final static DataBuffer procGetActionInfo(SrvSession sess, TreeConnection tree,
      DataBuffer reqBuf, Node folderNode, NetworkFile netFile) {
    // Get the executable file name from the request

    String exeName = reqBuf.getString(true);

    if (logger.isDebugEnabled())
      logger.debug("  Get action info, exe=" + exeName);

    // Create a response buffer

    DataBuffer respBuf = new DataBuffer(256);
    respBuf.putFixedString(Signature, Signature.length());

    // Get the desktop actions list

    if (true) { // there is no desctop actions
      respBuf.putInt(6);// StsNoSuchAction
      return respBuf;
    }

    // TODO Return the desktop action details

    /*
     * respBuf.putInt(DesktopAction.StsSuccess);
     * respBuf.putString(deskAction.getName(), true);
     * respBuf.putInt(deskAction.getAttributes());
     * respBuf.putInt(deskAction.getPreProcessActions()); String confirmStr =
     * deskAction.getConfirmationString(); respBuf.putString(confirmStr != null ?
     * confirmStr : "", true);
     */

    // Return the response
    return respBuf;
  }

  /**
   * Process the run action request
   * 
   * @param sess Server session
   * @param tree Tree connection
   * @param reqBuf Request buffer
   * @param folderNode NodeRef of parent folder
   * @param netFile NetworkFile for the folder
   * @return DataBuffer
   */
  private final static DataBuffer procRunAction(SrvSession sess, TreeConnection tree,
      DataBuffer reqBuf, Node folderNode, NetworkFile netFile) {
    // Get the name of the action to run

    String actionName = reqBuf.getString(true);

    if (logger.isDebugEnabled())
      logger.debug("  Run action, name=" + actionName);

    // Create a response buffer

    DataBuffer respBuf = new DataBuffer(256);
    respBuf.putFixedString(Signature, Signature.length());

    // Find the action handler

    if (true) {
      respBuf.putInt(6);// NoSuchActions
      respBuf.putString("", true);
      return respBuf;
    }

    // Return the response

    return respBuf;
  }

  /**
   * Process the get authentication ticket request
   * 
   * @param sess Server session
   * @param tree Tree connection
   * @param reqBuf Request buffer
   * @param folderNode NodeRef of parent folder
   * @param netFile NetworkFile for the folder
   * @return DataBuffer
   */
  private final static DataBuffer procGetAuthTicket(SrvSession sess, TreeConnection tree,
      DataBuffer reqBuf, Node folderNode, NetworkFile netFile) {
    // DEBUG

    if (logger.isDebugEnabled())
      logger.debug("  Get Auth Ticket");

    // Create a response buffer

    DataBuffer respBuf = new DataBuffer(256);
    respBuf.putFixedString(Signature, Signature.length());

    // Get an authentication ticket for the client, or validate the existing
    // ticket. The ticket can be used when
    // generating URLs for the client-side application so that the user does not
    // have to re-authenticate
    /*
     * getTicketForClient(sess); // Pack the response ClientInfo cInfo =
     * sess.getClientInformation(); if (cInfo != null &&
     * cInfo.getAuthenticationTicket() != null) {
     * respBuf.putInt(DesktopAction.StsAuthTicket);
     * respBuf.putString(cInfo.getAuthenticationTicket(), true); } else {
     * respBuf.putInt(DesktopAction.StsError); respBuf.putString("Client
     * information invalid", true); }
     */
    // Return the response
    return respBuf;
  }
}

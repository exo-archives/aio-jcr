/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.exoplatform.services.cifs.smb.server;

import java.io.IOException;

// import org.exoplatform.services.CIFS.server.filesys.DiskDeviceContext;
// import org.exoplatform.services.CIFS.server.filesys.DiskInterface;
// import org.exoplatform.services.CIFS.server.filesys.DiskSizeInterface;
// import org.exoplatform.services.CIFS.server.filesys.DiskVolumeInterface;
// import org.exoplatform.services.CIFS.server.filesys.SrvDiskInfo;
// import org.exoplatform.services.CIFS.server.filesys.VolumeInfo;
import org.exoplatform.services.cifs.server.filesys.TooManyConnectionsException;
import org.exoplatform.services.cifs.smb.PacketType;

/**
 * Protocol handler abstract base class.
 * <p>
 * The protocol handler class is the base of all SMB protocol/dialect handler
 * classes.
 */
abstract class ProtocolHandler {

  // Server session that this protocol handler is associated with.

  protected SMBSrvSession m_sess;

  /**
   * Create a protocol handler for the specified session.
   */
  protected ProtocolHandler() {
  }

  /**
   * Create a protocol handler for the specified session.
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected ProtocolHandler(SMBSrvSession sess) {
    m_sess = sess;
  }

  /**
   * Return the protocol handler name.
   * 
   * @return java.lang.String
   */
  public abstract String getName();

  /**
   * Run the SMB protocol handler for this server session.
   * 
   * @exception java.io.IOException
   * @exception SMBSrvException
   */
  public abstract boolean runProtocol() throws IOException, SMBSrvException,
      TooManyConnectionsException;

  /**
   * Get the server session that this protocol handler is associated with.
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected final SMBSrvSession getSession() {
    return m_sess;
  }

  /**
   * Set the server session that this protocol handler is associated with.
   * 
   * @param sess
   *          SMBSrvSession
   */
  protected final void setSession(SMBSrvSession sess) {
    m_sess = sess;
  }

  /**
   * Determine if the request is a chained (AndX) type command and there is a
   * chained command in this request.
   * 
   * @param pkt
   *          SMBSrvPacket
   * @return true if there is a chained request to be handled, else false.
   */
  protected final boolean hasChainedCommand(SMBSrvPacket pkt) {

    // Determine if the command code is an AndX command

    int cmd = pkt.getCommand();

    if (cmd == PacketType.SessionSetupAndX || cmd == PacketType.TreeConnectAndX
        || cmd == PacketType.OpenAndX || cmd == PacketType.WriteAndX
        || cmd == PacketType.ReadAndX || cmd == PacketType.LogoffAndX
        || cmd == PacketType.LockingAndX || cmd == PacketType.NTCreateAndX) {

      // Check if there is a chained command

      return pkt.hasAndXCommand();
    }

    // Not a chained type command

    return false;
  }

}
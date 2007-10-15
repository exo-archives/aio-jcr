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
package org.exoplatform.services.cifs.server.filesys;

import javax.jcr.Session;

import org.exoplatform.services.cifs.server.SrvSession;
import org.exoplatform.services.cifs.server.core.SharedDevice;

/**
 * The tree connection class holds the details of a single SMB tree connection.
 * A tree connection is a connection to a shared device.
 */
public class TreeConnection {

  // Maximum number of open files allowed per connection.

  public static final int MAXFILES = 8192;

  // Number of initial file slots to allocate. Number of allocated slots will
  // be doubled
  // when required until MAXFILES is reached.

  public static final int INITIALFILES = 32;

  // Shared device that the connection is associated with

  private SharedDevice m_shareDev;

  // session to jcr repository workspace, may be null for not disk shares
  private Session m_jcr_sess;

  // List of open files on this connection. Count of open file slots used.

  private NetworkFile[] m_files;

  private int m_fileCount;

  // Access permission that the user has been granted

  private int m_permission;

  /**
   * Construct a tree connection using the specified shared device name.
   * 
   * @param shrDev
   *          SharedDevice
   */
  public TreeConnection(SharedDevice shrDev) {
    m_shareDev = shrDev;
  }

  /**
   * Set jcr-session opened for shared device disk type (which represent
   * workspace).
   * 
   * @param javax.jcr.Session
   *          sess
   */
  public void setSession(Session sess) {
    m_jcr_sess = sess;
  }

  /**
   * Return jcr-session opened for shared device disk type (which represent
   * workspace).
   * 
   * @return javax.jcr.Session
   */
  public Session getSession() {
    return m_jcr_sess;
  }

  /**
   * Check if shared device has jcr-session assotiated with him
   * 
   * @return boolean
   */
  public boolean hasSession() {
    return false;
  }

  /**
   * Add a network file to the list of open files for this connection.
   * 
   * @param file
   *          NetworkFile
   * @param sess
   *          SrvSession
   * @return int
   */
  public final int addFile(NetworkFile file, SrvSession sess)
      throws TooManyFilesException {

    // Check if the file array has been allocated

    if (m_files == null)
      m_files = new NetworkFile[INITIALFILES];

    // Find a free slot for the network file

    int idx = 0;

    while (idx < m_files.length && m_files[idx] != null)
      idx++;

    // Check if we found a free slot

    if (idx == m_files.length) {

      // The file array needs to be extended, check if we reached the
      // limit.

      if (m_files.length >= MAXFILES)
        throw new TooManyFilesException();

      // Extend the file array

      NetworkFile[] newFiles = new NetworkFile[m_files.length * 2];
      System.arraycopy(m_files, 0, newFiles, 0, m_files.length);
      m_files = newFiles;
    }

    // Store the network file, update the open file count and return the
    // index

    m_files[idx] = file;
    m_fileCount++;

    return idx;
  }

  /**
   * Close the tree connection, release resources.
   * 
   * @param sess
   *          SrvSession
   */
  public final void closeConnection(SrvSession sess) {

    // Make sure all files are closed

    if (openFileCount() > 0) {

      // Close all open files

      for (int idx = 0; idx < m_files.length; idx++) {

        // Check if the file is active

        if (m_files[idx] != null)
          removeFile(idx, sess);
      }
    }

    // Decrement the active connection count for the shared device
    m_shareDev.decrementConnectionCount();

  }

  /**
   * Return the specified network file.
   * 
   * @return NetworkFile
   */
  public final NetworkFile findFile(int fid) {

    // Check if the file id and file array are valid

    if (m_files == null || fid >= m_files.length)
      return null;

    // Get the required file details

    return m_files[fid];
  }

  /**
   * Return the length of the file table
   * 
   * @return int
   */
  public final int getFileTableLength() {
    if (m_files == null)
      return 0;
    return m_files.length;
  }

  /**
   * Return the share access permissions that the user has been granted.
   * 
   * @return int
   */
  public final int getPermission() {
    return m_permission;
  }

  /**
   * Deterimine if the access permission for the shared device allows read
   * access
   * 
   * @return boolean
   */
  public final boolean hasReadAccess() {
    if (m_permission == FileAccess.ReadOnly
        || m_permission == FileAccess.Writeable)
      return true;
    return false;
  }

  /**
   * Determine if the access permission for the shared device allows write
   * access
   * 
   * @return boolean
   */
  public final boolean hasWriteAccess() {
    if (m_permission == FileAccess.Writeable)
      return true;
    return false;
  }

  /**
   * Check if the user has been granted the required access permission for this
   * share.
   * 
   * @param perm
   *          int
   * @return boolean
   */
  public final boolean hasPermission(int perm) {
    if (m_permission >= perm)
      return true;
    return false;
  }

  /**
   * Return the count of open files on this tree connection.
   * 
   * @return int
   */
  public final int openFileCount() {
    return m_fileCount;
  }

  /**
   * Remove all files from the tree connection.
   */
  public final void removeAllFiles() {

    // Check if the file array has been allocated

    if (m_files == null)
      return;

    // Clear the file list

    for (int idx = 0; idx < m_files.length;idx++){
      //m_files
      m_files[idx] = null;
    }
      ;
    m_fileCount = 0;
  }

  /**
   * Remove a network file from the list of open files for this connection.
   * 
   * @param idx
   *          int
   * @param sess
   *          SrvSession
   */
  public final void removeFile(int idx, SrvSession sess) {

    // Range check the file index

    if (m_files == null || idx >= m_files.length)
      return;

    // Remove the file and update the open file count.

    m_files[idx] = null;
    m_fileCount--;
  }

  /**
   * Set the access permission for this share that the user has been granted.
   * 
   * @param perm
   *          int
   */
  public final void setPermission(int perm) {
    m_permission = perm;
  }

  /**
   * Return the tree connection as a string.
   * 
   * @return java.lang.String
   */
  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("[");
    str.append(m_shareDev.getName());
    str.append(",");
    str.append(m_fileCount);
    str.append(":");
    str.append(FileAccess.asString(m_permission));
    str.append("]");
    return str.toString();
  }

  public SharedDevice getSharedDevice() {
    return m_shareDev;
  }
}
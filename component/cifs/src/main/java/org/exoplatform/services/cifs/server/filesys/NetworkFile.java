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
package org.exoplatform.services.cifs.server.filesys;

/**
 * <p>
 * The network file represents a file or directory on a filesystem. The server
 * keeps track of the open files on a per session basis.
 * <p>
 * This class may be extended as required by your own disk driver class.
 */
public abstract class NetworkFile {

  // Granted file access types

  public static final int READONLY      = 0;

  public static final int WRITEONLY     = 1;

  public static final int READWRITE     = 2;

  // File status flags

  public static final int IOPending     = 0x0001;

  public static final int DeleteOnClose = 0x0002;

  // File identifier and parent directory identifier

  protected int           m_fid;

  protected int           m_dirId;

  // Unique file identifier

  protected long          m_uniqueId;

  // File/directory name

  protected String        m_name;

  // Stream name and id

  protected String        m_streamName;

  protected int           m_streamId;

  // Full name, relative to the share

  protected String        m_fullName;

  // File attributes

  protected int           m_attrib;

  // File size

  protected long          m_fileSize    = 0;

  // File creation/modify/last access date/time

  protected long          m_createDate;

  protected long          m_modifyDate;

  protected long          m_accessDate;

  // Granted file access type

  protected int           m_grantedAccess;

  // Flag to indicate that the file has been closed

  protected boolean       m_closed      = true;

  // File status flags

  private int             m_flags;

  /**
   * Create a network file object with the specified file identifier.
   * 
   * @param fid int
   */
  public NetworkFile(int fid) {
    m_fid = fid;
  }

  /**
   * Create a network file with the specified file and parent directory ids
   * 
   * @param fid int
   * @param did int
   */
  public NetworkFile(int fid, int did) {
    m_fid = fid;
    m_dirId = did;
  }

  /**
   * Create a network file with the specified file id, stream id and parent
   * directory id
   * 
   * @param fid int
   * @param stid int
   * @param did int
   */
  public NetworkFile(int fid, int stid, int did) {
    m_fid = fid;
    m_streamId = stid;
    m_dirId = did;
  }

  /**
   * Create a network file object with the specified file/directory name.
   * 
   * @param name File name string.
   */
  public NetworkFile(String name) {
    m_name = name;
  }

  public NetworkFile() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Return the parent directory identifier
   * 
   * @return int
   */
  public final int getDirectoryId() {
    return m_dirId;
  }

  /**
   * Return the file attributes.
   * 
   * @return int
   */
  public final int getFileAttributes() {
    return m_attrib;
  }

  /**
   * Return the file identifier.
   * 
   * @return int
   */
  public final int getFileId() {
    return m_fid;
  }

  /**
   * Get the file size, in bytes.
   * 
   * @return long
   */
  public final long getFileSize() {
    return m_fileSize;
  }

  /**
   * Get the file size, in bytes.
   * 
   * @return int
   */
  public final int getFileSizeInt() {
    return (int) (m_fileSize & 0x0FFFFFFFFL);
  }

  /**
   * Return the full name, relative to the share.
   * 
   * @return java.lang.String
   */
  public final String getFullName() {
    return m_fullName;
  }

  /**
   * Return the full name including the stream name, relative to the share.
   * 
   * @return java.lang.String
   */
  public final String getFullNameStream() {
    if (isStream())
      return m_fullName + m_streamName;
    else
      return m_fullName;
  }

  /**
   * Return the granted file access mode.
   */
  public final int getGrantedAccess() {
    return m_grantedAccess;
  }

  /**
   * Return the file/directory name.
   * 
   * @return java.lang.String
   */
  public String getName() {
    return m_name;
  }

  /**
   * Return the stream id, zero indicates the main file stream
   * 
   * @return int
   */
  public final int getStreamId() {
    return m_streamId;
  }

  /**
   * Return the stream name, if this is a stream
   * 
   * @return String
   */
  public final String getStreamName() {
    return m_streamName;
  }

  /**
   * Return the unique file identifier
   * 
   * @return long
   */
  public final long getUniqueId() {
    return m_uniqueId;
  }

  /**
   * Determine if the file has been closed.
   * 
   * @return boolean
   */
  public final boolean isClosed() {
    return m_closed;
  }

  /**
   * Return the directory file attribute status.
   * 
   * @return true if the file is a directory, else false.
   */

  public final boolean isDirectory() {
    return (m_attrib & FileAttribute.Directory) != 0 ? true : false;
  }

  /**
   * Return the hidden file attribute status.
   * 
   * @return true if the file is hidden, else false.
   */

  public final boolean isHidden() {
    return (m_attrib & FileAttribute.Hidden) != 0 ? true : false;
  }

  /**
   * Return the read-only file attribute status.
   * 
   * @return true if the file is read-only, else false.
   */

  public final boolean isReadOnly() {
    return (m_attrib & FileAttribute.ReadOnly) != 0 ? true : false;
  }

  /**
   * Return the system file attribute status.
   * 
   * @return true if the file is a system file, else false.
   */

  public final boolean isSystem() {
    return (m_attrib & FileAttribute.System) != 0 ? true : false;
  }

  /**
   * Return the archived attribute status
   * 
   * @return boolean
   */
  public final boolean isArchived() {
    return (m_attrib & FileAttribute.Archive) != 0 ? true : false;
  }

  /**
   * Check if this is a stream file
   * 
   * @return boolean
   */
  public final boolean isStream() {
    return m_streamName != null ? true : false;
  }

  /**
   * Check for NT attributes
   * 
   * @param attr int
   * @return boolean
   */
  public final boolean hasNTAttribute(int attr) {
    return (m_attrib & attr) == attr ? true : false;
  }

  /**
   * Determine if the file access date/time is valid
   * 
   * @return boolean
   */
  public final boolean hasAccessDate() {
    return m_accessDate != 0L ? true : false;
  }

  /**
   * Return the file access date/time
   * 
   * @return long
   */
  public final long getAccessDate() {
    return m_accessDate;
  }

  /**
   * Determine if the file creation date/time is valid
   * 
   * @return boolean
   */
  public final boolean hasCreationDate() {
    return m_createDate != 0L ? true : false;
  }

  /**
   * Return the file creation date/time
   * 
   * @return long
   */
  public final long getCreationDate() {
    return m_createDate;
  }

  /**
   * Check if the delete on close flag has been set for this file
   * 
   * @return boolean
   */
  public final boolean hasDeleteOnClose() {
    return (m_flags & DeleteOnClose) != 0 ? true : false;
  }

  /**
   * Check if the file has an I/O request pending
   * 
   * @return boolean
   */
  public final boolean hasIOPending() {
    return (m_flags & IOPending) != 0 ? true : false;
  }

  /**
   * Determine if the file modification date/time is valid
   * 
   * @return boolean
   */
  public boolean hasModifyDate() {
    return m_modifyDate != 0L ? true : false;
  }

  /**
   * Return the file modify date/time
   * 
   * @return long
   */
  public final long getModifyDate() {
    return m_modifyDate;
  }

  /**
   * Set the file attributes, as specified by the SMBFileAttribute class.
   * 
   * @param attrib int
   */
  public final void setAttributes(int attrib) {
    m_attrib = attrib;
  }

  /**
   * Set, or clear, the delete on close flag
   * 
   * @param del boolean
   */
  public final void setDeleteOnClose(boolean del) {
    setStatusFlag(DeleteOnClose, del);
  }

  /**
   * Set the parent directory identifier
   * 
   * @param dirId int
   */
  public final void setDirectoryId(int dirId) {
    m_dirId = dirId;
  }

  /**
   * Set the file identifier.
   * 
   * @param fid int
   */
  public final void setFileId(int fid) {
    m_fid = fid;
  }

  /**
   * Set the file size.
   * 
   * @param siz long
   */
  public final void setFileSize(long siz) {
    m_fileSize = siz;
  }

  /**
   * Set the file size.
   * 
   * @param siz int
   */
  public final void setFileSize(int siz) {
    m_fileSize = (long) siz;
  }

  /**
   * Set the full file name, relative to the share.
   * 
   * @param name java.lang.String
   */
  public final void setFullName(String name) {
    m_fullName = name;
  }

  /**
   * Set the granted file access mode.
   * 
   * @param mode int
   */
  public final void setGrantedAccess(int mode) {
    m_grantedAccess = mode;
  }

  /**
   * Set the file name.
   * 
   * @param name String
   */
  public final void setName(String name) {
    m_name = name;
  }

  /**
   * set/clear the I/O pending flag
   * 
   * @param pending boolean
   */
  public final void setIOPending(boolean pending) {
    setStatusFlag(IOPending, pending);
  }

  /**
   * Set the stream id
   * 
   * @param id int
   */
  public final void setStreamId(int id) {
    m_streamId = id;
  }

  /**
   * Set the stream name
   * 
   * @param name String
   */
  public final void setStreamName(String name) {
    m_streamName = name;
  }

  /**
   * Set the file closed state.
   * 
   * @param b boolean
   */
  public final synchronized void setClosed(boolean b) {
    m_closed = b;
  }

  /**
   * Set the file access date/time
   * 
   * @param dattim long
   */
  public final void setAccessDate(long dattim) {
    m_accessDate = dattim;
  }

  /**
   * Set the file creation date/time
   * 
   * @param dattim long
   */
  public final void setCreationDate(long dattim) {
    m_createDate = dattim;
  }

  /**
   * Set the file modification date/time
   * 
   * @param dattim long
   */
  public final void setModifyDate(long dattim) {
    m_modifyDate = dattim;
  }

  /**
   * Set/clear a file status flag
   * 
   * @param flag int
   * @param sts boolean
   */
  protected final synchronized void setStatusFlag(int flag, boolean sts) {
    boolean state = (m_flags & flag) != 0;
    if (sts == true && state == false)
      m_flags += flag;
    else if (sts == false && state == true)
      m_flags -= flag;
  }

  /**
   * Set the unique file identifier
   * 
   * @param id long
   */
  protected final void setUniqueId(long id) {
    m_uniqueId = id;
  }

  /**
   * Set the unique id using the file and directory id
   * 
   * @param fid int
   * @param did int
   */
  protected final void setUniqueId(int fid, int did) {
    long ldid = (long) did;
    long lfid = (long) fid;
    m_uniqueId = (ldid << 32) + lfid;
  }

  /**
   * Set the unique id using the full path string
   * 
   * @param path String
   */
  protected final void setUniqueId(String path) {
    m_uniqueId = (long) path.toUpperCase().hashCode();
  }

}
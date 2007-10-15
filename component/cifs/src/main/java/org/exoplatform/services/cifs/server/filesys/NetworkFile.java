package org.exoplatform.services.cifs.server.filesys;

import java.util.Date;

public abstract class NetworkFile {
  // Granted file access types

  public static final int READONLY = 0;

  public static final int WRITEONLY = 1;

  public static final int READWRITE = 2;

  // File status flags

  public static final int IOPending = 0x0001;

  public static final int DeleteOnClose = 0x0002;

  // File identifier and parent directory identifier

  protected int m_fid;

  protected int m_dirId;

  // Unique file identifier

  protected long m_uniqueId;

  // File/directory name

  protected String m_name;

  // Stream name and id

  protected String m_streamName;

  protected int m_streamId;

  // Full name, relative to the share

  protected String m_fullName;

  // File attributes

  protected int m_attrib;

  // File size

  protected long m_fileSize=0;

  // File creation/modify/last access date/time

  protected long m_createDate;

  protected long m_modifyDate;

  protected long m_accessDate;

  // Granted file access type

  protected int m_grantedAccess;

  // Flag to indicate that the file has been closed

  protected boolean m_closed = true;

  // File status flags

  private int m_flags;

  public NetworkFile() {
  }

  public NetworkFile(int fid) {
    m_fid = fid;
  }

  /**
   * Create a network file with the specified file and parent directory ids
   * 
   * @param fid
   *          int
   * @param did
   *          int
   */
  public NetworkFile(int fid, int did) {
    m_fid = fid;
    m_dirId = did;
  }

  /**
   * Create a network file with the specified file id, stream id and parent
   * directory id
   * 
   * @param fid
   *          int
   * @param stid
   *          int
   * @param did
   *          int
   */
  public NetworkFile(int fid, int stid, int did) {
    m_fid = fid;
    m_streamId = stid;
    m_dirId = did;
  }

  /**
   * Create a network file object with the specified file/directory name.
   * 
   * @param name
   *          File name string.
   */
  public NetworkFile(String name) {
    m_name = name;
  }

  public void setGrantedAccess(int i) {
    m_grantedAccess = i;
  }

  public String getName() {
    return m_name;
  }

  public int getFileId() {
    return m_fid;
  }

  public int getFileAttributes() {
    return m_attrib;
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
   * @param attr
   *          int
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

  public int getGrantedAccess() {

    return m_grantedAccess;
  }

  /**
   * Set the file attributes, as specified by the SMBFileAttribute class.
   * 
   * @param attrib
   *          int
   */
  public final void setAttributes(int attrib) {
    m_attrib = attrib;
  }

  /**
   * Set the file size.
   * 
   * @param siz
   *          long
   */
  public final void setFileSize(long siz) {
    m_fileSize = siz;
  }

  /**
   * Set the file size.
   * 
   * @param siz
   *          int
   */
  public final void setFileSize(int siz) {
    m_fileSize = (long) siz;
  }

  /**
   * Set the file access date/time
   * 
   * @param dattim
   *          long
   */
  public final void setAccessDate(long dattim) {
    m_accessDate = dattim;
  }

  /**
   * Set the file creation date/time
   * 
   * @param dattim
   *          long
   */
  public final void setCreationDate(long dattim) {
    m_createDate = dattim;
  }

  /**
   * Set the file modification date/time
   * 
   * @param dattim
   *          long
   */
  public final void setModifyDate(long dattim) {
    m_modifyDate = dattim;
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
   * Set the file closed state.
   * 
   * @param b
   *          boolean
   */
  public final synchronized void setClosed(boolean b) {
    m_closed = b;
  }

  abstract public int writeFile(byte[] buf, int dataPos, int dataLen, int offset)
      throws Exception;

  // abstract public int readFile(byte[] buf, int maxCount, int dataPos, int
  // offset)throws Exception;

  /**
   * Return the file information as a string.
   * 
   * @return File information string.
   */
  public String toString() {
    StringBuffer str = new StringBuffer();

    // Append the file/directory name

    if (m_name != null) {
      str.append(m_name);
    }

    // Append the stream name
    str.append(" - stream[");

    if (m_name != null) {
      str.append(m_streamName);
    }

    str.append("] fullname[");
    if (m_fullName != null) {
      str.append(m_fullName);
    }

    // Append the attribute states

    str.append("] atributes[");

    str.append(m_attrib + " ");

    if (isReadOnly())
      str.append("R");
    else
      str.append("-");
    if (isHidden())
      str.append("H");
    else
      str.append("-");
    if (isSystem())
      str.append("S");
    else
      str.append("-");
    if (isDirectory())
      str.append("D");
    else
      str.append("F");

    // Append the file size, in bytes

    str.append("] size[");
    str.append(m_fileSize);

    // Append the file write date/time, if available
    str.append("] modifyDate[");

    if (m_modifyDate != 0L) {
      str.append(" - ");
      str.append(new Date(m_modifyDate));
    }

    // Append the file status
    str.append("] fileStatus[");
    str.append(m_flags);
    str.append("]");
    // Return the file information string

    return str.toString();
  }
  
  public void remove(){
    
  }
}

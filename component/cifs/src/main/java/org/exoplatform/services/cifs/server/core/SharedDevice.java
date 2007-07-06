/**
 * 
 */
package org.exoplatform.services.cifs.server.core;

import org.exoplatform.services.cifs.server.filesys.DiskInfo;
import org.exoplatform.services.cifs.server.filesys.VolumeInfo;

/**
 * This class is a wrapper of share device abstraction (in JCR it means
 * workspaces)
 * 
 * @author Karpenko
 * 
 */
public class SharedDevice {
  // Share attribute types

  public static final int Admin = 0x0001;

  public static final int Hidden = 0x0002;

  public static final int ReadOnly = 0x0004;

  public static final int Temporary = 0x0008;

  // Shared device name
  private String m_name;

  // Shared device type look ShareType
  private int m_type;

  // Shared device comments
  private String m_comments;

  // Shared device attributes
  private int m_attrib;

  // Current and maximum connections to this shared device

  private int m_maxUses = -1; // unlimited

  private int m_curUses = 0;

  private DiskInfo m_diskinfo;

  private VolumeInfo m_volinfo;

  // Empty class constructor
  public SharedDevice() {

  }

  public SharedDevice(String name, int type) {
    m_name = name;
    m_type = type;
  }

  public void setName(String s) {
    m_name = s;
  }

  public void setType(int type) {
    m_type = type;
  }

  public void setComments(String com) {
    m_comments = com;
  }

  public void setAttributes(int i) {
    m_attrib = i;
  }

  public String getName() {
    return m_name;
  }

  public int getType() {
    return m_type;
  }

  public String getComments() {
    return m_comments;
  }

  public int getAttributes() {
    return m_attrib;
  }

  /**
   * Determine if this is an admin share.
   * 
   * @return boolean
   */
  public final boolean isAdmin() {
    return (m_attrib & Admin) == 0 ? false : true;
  }

  /**
   * Determine if this is a hidden share.
   * 
   * @return boolean
   */
  public final boolean isHidden() {
    return (m_attrib & Hidden) == 0 ? false : true;
  }

  /**
   * Determine if the share is read-only.
   * 
   * @return boolean
   */
  public final boolean isReadOnly() {
    return (m_attrib & ReadOnly) == 0 ? false : true;
  }

  /**
   * Determine if the share is a temporary share
   * 
   * @return boolean
   */
  public final boolean isTemporary() {
    return (m_attrib & Temporary) == 0 ? false : true;
  }

  /**
   * Increment the connection count for the share
   */
  public synchronized void incrementConnectionCount() {
    m_curUses++;
  }

  /**
   * Decrement the connection count for the share
   */
  public synchronized void decrementConnectionCount() {
    m_curUses--;
  }

  /**
   * Returns a String that represents the value of this object.
   * 
   * @return a string representation of the receiver
   */
  public String toString() {

    // Build a string that represents this shared device

    StringBuffer str = new StringBuffer();
    str.append("[");
    str.append(getName());
    str.append(",");
    str.append(ShareType.TypeAsString(getType()));
    str.append(",");

    if (isAdmin())
      str.append(",Admin");

    if (isHidden())
      str.append(",Hidden");

    if (isReadOnly())
      str.append(",ReadOnly");

    if (isTemporary())
      str.append(",Temp");

    str.append("]");

    return str.toString();
  }

  public DiskInfo getDiskInformation() {

    return m_diskinfo;
  }

  public VolumeInfo getVolumeInformation() {

    return m_volinfo;
  }

  public void setDiskInfo(DiskInfo di) {
    m_diskinfo = di;
  }

  public void setVolumeInfo(VolumeInfo vi) {
    m_volinfo = vi;
  }
}

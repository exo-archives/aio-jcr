package org.exoplatform.services.cifs.server.core;

import org.exoplatform.services.cifs.server.filesys.DiskInfo;
import org.exoplatform.services.cifs.server.filesys.VolumeInfo;

/**
 * This class is a wrapper of share device abstraction (in JCR it means
 * workspace).
 * 
 * @author Karpenko Sergey
 */
public class SharedDevice {
  // Share attribute types
  public static final int ADMIN     = 0x0001;

  public static final int HIDDEN    = 0x0002;

  public static final int READONLY  = 0x0004;

  public static final int TEMPORARY = 0x0008;

  /**
   * Shared device name.
   */
  private String          name;

  /**
   * Shared device type look ShareType.
   */
  private int             type;

  /**
   * Shared device comments.
   */
  private String          comments;

  /**
   * Shared device attributes.
   */
  private int             attrib;

  /**
   * Current and maximum connections to this shared device.
   */
  private int             maxUses   = -1;    // unlimited

  private int             curUses   = 0;

  private DiskInfo        diskinfo;

  private VolumeInfo      volinfo;

  /**
   * Empty class constructor.
   */
  public SharedDevice() {

  }

  /**
   * Class constructor.
   * 
   * @param name
   * @param type
   */
  public SharedDevice(String name, int type) {
    this.name = name;
    this.type = type;
  }

  public void setName(String s) {
    name = s;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setComments(String com) {
    comments = com;
  }

  public void setAttributes(int i) {
    attrib = i;
  }

  public String getName() {
    return name;
  }

  public int getType() {
    return type;
  }

  public String getComments() {
    return comments;
  }

  public int getAttributes() {
    return attrib;
  }

  /**
   * Determine if this is an admin share.
   * 
   * @return boolean
   */
  public final boolean isAdmin() {
    return (attrib & ADMIN) == 0 ? false : true;
  }

  /**
   * Determine if this is a hidden share.
   * 
   * @return boolean
   */
  public final boolean isHidden() {
    return (attrib & HIDDEN) == 0 ? false : true;
  }

  /**
   * Determine if the share is read-only.
   * 
   * @return boolean
   */
  public final boolean isReadOnly() {
    return (attrib & READONLY) == 0 ? false : true;
  }

  /**
   * Determine if the share is a temporary share
   * 
   * @return boolean
   */
  public final boolean isTemporary() {
    return (attrib & TEMPORARY) == 0 ? false : true;
  }

  /**
   * Increment the connection count for the share
   */
  public synchronized void incrementConnectionCount() {
    curUses++;
  }

  /**
   * Decrement the connection count for the share
   */
  public synchronized void decrementConnectionCount() {
    curUses--;
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

    return diskinfo;
  }

  public VolumeInfo getVolumeInformation() {

    return volinfo;
  }

  public void setDiskInfo(DiskInfo di) {
    diskinfo = di;
  }

  public void setVolumeInfo(VolumeInfo vi) {
    volinfo = vi;
  }
}

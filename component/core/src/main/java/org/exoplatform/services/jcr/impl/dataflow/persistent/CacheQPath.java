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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.Serializable;
import java.util.UUID;

import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS
 * 15.06.07
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
class CacheQPath implements Serializable {

  //private static final String BASE = String.valueOf(UUID.randomUUID().hashCode()) + "-";
  
//  static {
//    UUID rnd = UUID.randomUUID();
//    
//    byte[] rb = new byte[16];
//    rb[0] = (byte) (rnd.getMostSignificantBits() >>> 56 & 0x0f); 
//    rb[1] = (byte) (rnd.getMostSignificantBits() >>> 48 & 0x0f);
//    rb[2] = (byte) (rnd.getMostSignificantBits() >>> 40 & 0x0f);
//    rb[3] = (byte) (rnd.getMostSignificantBits() >>> 32 & 0xff);
//    rb[4] = (byte) (rnd.getMostSignificantBits() >>> 24 & 0xff);
//    rb[5] = (byte) (rnd.getMostSignificantBits() >>> 16 & 0xff);
//    rb[6] = (byte) (rnd.getMostSignificantBits() >>> 8 & 0xff);
//    rb[7] = (byte) (rnd.getMostSignificantBits() & 0xff);
//    
//    rb[8] = (byte) (rnd.getLeastSignificantBits() >>> 56 & 0x0f); 
//    rb[9] = (byte) (rnd.getLeastSignificantBits() >>> 48 & 0xff);
//    rb[10] = (byte) (rnd.getLeastSignificantBits() >>> 40 & 0xff);
//    rb[11] = (byte) (rnd.getLeastSignificantBits() >>> 32 & 0xff);
//    rb[12] = (byte) (rnd.getLeastSignificantBits() >>> 24 & 0xff);
//    rb[13] = (byte) (rnd.getLeastSignificantBits() >>> 16 & 0xff);
//    rb[14] = (byte) (rnd.getLeastSignificantBits() >>> 8 & 0xff);
//    rb[15] = (byte) (rnd.getLeastSignificantBits() & 0xff);
//    
//    //BASE = new String(rb);
//  }
  
  private final String parentId;
  private final QPath path;
  //private final int hashCode;
  private final String key;
  
  /**
   * For CPath will be stored in cache C
   */
  CacheQPath(String parentId, QPath path) {
    this.parentId = parentId;
    this.path = path;
    this.key = key(this.parentId, this.path.getEntries());
    
//    int hk = 31 + (this.parentId != null ? this.parentId.hashCode() : 1);
//    this.hashCode = hk * 31 + this.path.getEntries()[this.path.getEntries().length - 1].hashCode();
    //this.hashCode = key(this.parentId, this.path.getEntries()).hashCode();
  }
  
  /**
   * For CPath will be searched in cache C
   */
  CacheQPath(String parentId, QPathEntry name) {
    this.parentId = parentId;
    this.path = null;
    this.key = key(this.parentId, name);
    
//    int hk = 31 + (this.parentId != null ? this.parentId.hashCode() : 1);
//    this.hashCode = hk * 31 + name.hashCode();
    
//    this.hashCode = key(this.parentId, name).hashCode();
  }
    
  protected String key(String parentId, QPathEntry[] pathEntries) {
    return key(parentId, pathEntries[pathEntries.length - 1]);
  }
  
  protected String key(String parentId, QPathEntry name) {
    StringBuilder sk = new StringBuilder();
    //sk.append(BASE); for strong hash code, skip it when equals uses String.equals 
    sk.append(parentId != null ? parentId : Constants.ROOT_PARENT_UUID);
    sk.append(name.getAsString(true));
    return sk.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    //return obj.equals(key); // [PN] 16.10.07
    if (key.hashCode() == obj.hashCode() && obj instanceof CacheQPath)
      return key.equals(((CacheQPath) obj).key);
    return false;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public String toString() {
    return (this.parentId != null ? this.parentId : Constants.ROOT_PARENT_UUID) + 
      (path != null ? path.getEntries()[path.getEntries().length - 1] : "null") + ", " +
      key;
  }

  protected String getParentId() {
    return parentId;
  }

  protected QPath getQPath() {
    return path;
  }
}

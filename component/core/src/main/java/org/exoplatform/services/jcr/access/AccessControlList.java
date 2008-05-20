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
package org.exoplatform.services.jcr.access;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.<br/> Access Control List
 * 
 * @author Gennady Azarenkov
 * @version $Id: AccessControlList.java 12851 2007-02-16 14:48:31Z ksm $
 */

public class AccessControlList implements Externalizable {
  private Log                            log       = ExoLogger.getLogger("jcr.AccessControlList");

  public static final String             DELIMITER = ";";

  private String                         owner;

  private final List<AccessControlEntry> accessList;

  public AccessControlList() {
    this(SystemIdentity.SYSTEM);
  }

  /**
   * default ACL owned by ownerName
   * 
   * @param ownerName
   */
  AccessControlList(String ownerName) {
    this.owner = ownerName;
    this.accessList = new ArrayList<AccessControlEntry>();
    for (String str : PermissionType.ALL) {
      accessList.add(new AccessControlEntry(SystemIdentity.ANY, str));
    }
  }

  /**
   * Create ACL from owner name and collection of permission entries
   * 
   * @param owner
   * @param permissions - permission entries
   */
  public AccessControlList(String owner, List<AccessControlEntry> accessList) {
    this.owner = owner;
    this.accessList = accessList;
  }

  public boolean hasPermissions() {
    return accessList != null;
  }

  public boolean hasOwner() {
    return owner != null;
  }

  public void addPermissions(String rawData) throws RepositoryException {
    StringTokenizer listTokenizer = new StringTokenizer(rawData, AccessControlList.DELIMITER);
    if (listTokenizer.countTokens() < 1)
      throw new RepositoryException("AccessControlList " + rawData
          + " is empty or have a bad format");

    while (listTokenizer.hasMoreTokens()) {
      String entry = listTokenizer.nextToken();
      StringTokenizer entryTokenizer = new StringTokenizer(entry, AccessControlEntry.DELIMITER);
      if (entryTokenizer.countTokens() != 2)
        throw new RepositoryException("AccessControlEntry " + entry
            + " is empty or have a bad format");
      accessList.add(new AccessControlEntry(entryTokenizer.nextToken(), entryTokenizer.nextToken()));
    }
  }

  public void addPermissions(String identity, String[] perm) {
    for (String p : perm) {
      accessList.add(new AccessControlEntry(identity, p));
    }
  }

  public void removePermissions(String identity) {
    for (Iterator<AccessControlEntry> iter = accessList.iterator(); iter.hasNext();) {
      AccessControlEntry a = iter.next();
      if (a.getIdentity().equals(identity))
        iter.remove();
    }
  }

  public void removePermissions(String identity, String permission) {
    for (Iterator<AccessControlEntry> iter = accessList.iterator(); iter.hasNext();) {
      AccessControlEntry a = iter.next();
      if (a.getIdentity().equals(identity) && a.getPermission().equals(permission))
        iter.remove();
    }
  }

  /**
   * @return Returns the owner.
   */
  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  // Create safe copy of list <AccessControlEntry>
  public List<AccessControlEntry> getPermissionEntries() {
    List<AccessControlEntry> list = new ArrayList<AccessControlEntry>();
    for (AccessControlEntry entry : accessList) {
      list.add(new AccessControlEntry(entry.getIdentity(), entry.getPermission()));
    }
    return list;
  }

  public List<String> getPermissions(String identity) {
    List<String> permissions = new ArrayList<String>();
    for (AccessControlEntry entry : accessList) {
      if (entry.getIdentity().equals(identity))
        permissions.add(entry.getPermission());
    }
    return permissions;
  }

  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj instanceof AccessControlList) {
      AccessControlList another = (AccessControlList) obj;
      return dump().equals(another.dump());
    }
    return false;
  }

  public String dump() {
    String res = "OWNER: " + owner + "\n";
    for (AccessControlEntry a : accessList) {
      res += a.getAsString() + "\n";
    }
    return res;
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    // reading owner
    byte[] buf;
    int ownLength = in.readInt();
    if (ownLength != 0) {
      buf = new byte[ownLength];
      in.read(buf);
      this.owner = new String(buf, "UTF-8");
    } else {
      this.owner = null;
    }
    accessList.clear();
    // reading access control entrys size
    int listSize = in.readInt();
    for (int i = 0; i < listSize; i++) {
      // reading access control entrys identity
      buf = new byte[in.readInt()];
      in.read(buf);
      String ident = new String(buf, "UTF-8");
      // reading permission
      buf = new byte[in.readInt()];
      in.read(buf);
      String perm = new String(buf, "UTF-8");

      accessList.add(new AccessControlEntry(ident, perm));
    }
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    // Writing owner
    if (owner != null) {
      out.writeInt(owner.getBytes().length);
      out.write(owner.getBytes());
    } else {
      out.writeInt(0);
    }

    // writing access control entrys size
    out.writeInt(accessList.size());

    for (AccessControlEntry entry : accessList) {
      // writing access control entrys identity
      out.writeInt(entry.getIdentity().getBytes().length);
      out.write(entry.getIdentity().getBytes());
      // writing permission
      out.writeInt(entry.getPermission().getBytes().length);
      out.write(entry.getPermission().getBytes());
    }
  }

  /**
   * @return size of access list
   */
  public int size() {
    return accessList != null ? accessList.size() : 0;
  }

  List<AccessControlEntry> getPermissionsList() {
    return accessList;
  }
}
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
/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: AccessControlEntry.java 12843 2007-02-16 09:11:18Z peterit $
 */
public class AccessControlEntry {


  private String identity;

  private String permission;
  
  public static final String DELIMITER = " "; 
  public AccessControlEntry(String identity, String permission) {
    this.identity = identity;
    this.permission = permission;
  }

  public String getIdentity() {
    return identity;
  }

  public String getPermission() {
    return permission;
  }

  public String getAsString() {
    return identity + AccessControlEntry.DELIMITER + permission;
  }
  
  public boolean equals(Object obj) {
    if(obj == this)
      return true;
    if(obj instanceof AccessControlEntry) {
      AccessControlEntry another = (AccessControlEntry) obj;
      return getAsString().equals(another.getAsString());
    }
    return false;
  }

  @Override
  public String toString() {
    return super.toString() + " (" + getAsString() + ")";
  }
  
}

/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.access;
/**
 * Created by The eXo Platform SARL        .
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
  public static AccessControlEntry parse(String permission) {
    
    return null;
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

}

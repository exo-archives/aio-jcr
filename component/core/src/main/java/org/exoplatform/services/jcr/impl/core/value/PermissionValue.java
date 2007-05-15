/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * 
 * @version $Id: PermissionValue.java 13463 2007-03-16 09:17:29Z geaz $
 */

public class PermissionValue extends BaseValue {

  private static final int TYPE = ExtendedPropertyType.PERMISSION;
  
  private String identity;

  private String permission;

  public PermissionValue(TransientValueData data) throws IOException {
    super(TYPE, data);
    
    try {
      //String p = new String(BLOBUtil.readValue(data));
      String[] persArray = parse(new String(data.getAsByteArray()));
      this.identity = persArray[0];
      this.permission = persArray[1];
    } catch (IOException e) {
      throw new RuntimeException("FATAL ERROR IOException occured: " + e.getMessage(), e);
    }
  }

  public PermissionValue(String identity, String permission) throws IOException {    
    // [PN] 18.05.06 toString(identity, permission) instead identity + " " + permission
    super(TYPE, new TransientValueData(asString(identity, permission))); // identity + " " + permission
    // [PN] 08.02.06
    if (identity != null && identity.indexOf(" ") != -1)
      throw new RuntimeException("Identity should not contain ' '");
    if(permission != null && !permission.equals(PermissionType.READ) &&
       !permission.equals(PermissionType.ADD_NODE) &&
       !permission.equals(PermissionType.REMOVE) &&
       !permission.equals(PermissionType.SET_PROPERTY))
      throw new RuntimeException("Permission should be one of defined in PermissionType. Have "+permission);
    this.identity = identity;
    this.permission = permission;
  }
  
  static public PermissionValue parseValue(String pstring) throws IOException {
    String[] persArray = parse(pstring);
    return new PermissionValue(persArray[0], persArray[1]);
  }
  
  static public String[] parse(String pstring) {
    StringTokenizer parser = new StringTokenizer(pstring, AccessControlEntry.DELIMITER);
    String identityString = parser.nextToken();
    String permissionString = parser.nextToken();
    
    String[] persArray = new String[2]; 
    
    if (identityString != null) {
      persArray[0] = identityString;
    } else {
      persArray[0] = SystemIdentity.ANY;
    }
    if (permissionString != null) {
      persArray[1] = permissionString;
    } else {
      persArray[1] = PermissionType.READ;
    }
    return persArray;
  }

  protected String getInternalString() throws ValueFormatException {
    // [PN] 19.05.06
    return asString(identity, permission);
  }
  
  static protected String asString(String identity, String permission) {
    if (identity != null || permission != null) //SystemIdentity.ANY, PermissionType.ALL
      return (identity != null ? identity : SystemIdentity.ANY) + AccessControlEntry.DELIMITER 
        + (permission != null ? permission : PermissionType.READ);
    else 
      return "";
  }

  /**
   * @return Returns the identity.
   */
  public String getIdentity() {
    return identity;
  }

  /**
   * @return Returns the permission
   */
  public String getPermission() {
    return permission;
  }
  
//  public AccessControlEntry getACE() {
//    return permission;
//  }
  
  
}
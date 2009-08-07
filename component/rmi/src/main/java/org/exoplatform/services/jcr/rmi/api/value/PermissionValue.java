/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exoplatform.services.jcr.rmi.api.value;

import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id$
 */
public class PermissionValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  private static final long serialVersionUID = 1475495561074957852L;

  private String            identity;

  private String            permission;

  public PermissionValue(String identity, String permission) {
    if (identity != null && identity.indexOf(" ") != -1)
      throw new RuntimeException("Identity should not contain ' '");
    if (permission != null && !permission.equals(PermissionType.READ)
        && !permission.equals(PermissionType.ADD_NODE) && !permission.equals(PermissionType.REMOVE)
        && !permission.equals(PermissionType.SET_PROPERTY))
      throw new RuntimeException("Permission should be one of defined in PermissionType. Have "
          + permission);
    this.identity = identity;
    this.permission = permission;
  }

  static public PermissionValue parseValue(String pstring) {
    String[] persArray = parse(pstring);
    return new PermissionValue(persArray[0], persArray[1]);
  }

  static public String[] parse(String pstring) {
    StringTokenizer parser = new StringTokenizer(pstring, AccessControlList.DELIMITER);
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

  public long getLength() {
    return getString().length();
  }

  public String getString() {
    return asString(identity, permission);
  }

  public int getType() {
    // TODO Auto-generated method stub
    return ExtendedPropertyType.PERMISSION;
  }

  static protected String asString(String identity, String permission) {
    if (identity != null || permission != null) // SystemIdentity.ANY, PermissionType.ALL
      return (identity != null ? identity : SystemIdentity.ANY) + AccessControlList.DELIMITER
          + (permission != null ? permission : PermissionType.READ);
    else
      return "";
  }
}

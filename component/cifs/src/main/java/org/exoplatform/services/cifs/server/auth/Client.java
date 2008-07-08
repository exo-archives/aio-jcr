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
package org.exoplatform.services.cifs.server.auth;

/**
 * This class contain info about authenticated user.
 * <p>
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 */

public class Client {

  public static final String GUEST_NAME         = "__anonim";

  // Logon types

  public final static int    LOGON_NORMAL       = 0;

  public final static int    LOGON_GUEST        = 1;

  public final static int    LOGON_NULL_SESSION = 2;

  private String             username;

  private String             plainpassword;

  private int                logonType;

  public Client(String name, String pass) {
    username = name;
    plainpassword = pass;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return plainpassword;
  }

  public boolean isGuest() {
    return (logonType == LOGON_GUEST);
  }

  public void setGuest(boolean isguest) {
    setLogonType(isguest == true ? LOGON_GUEST : LOGON_NORMAL);
  }

  public final void setLogonType(int logonType) {
    this.logonType = logonType;
  }

  public boolean isNullSession() {
    return logonType == LOGON_NULL_SESSION;
  }

}

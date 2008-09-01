/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.external;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class MemberInfo {
  private final String ipAddress;

  private final int    port;

  private final String login;

  private final String password;
  
  private final int    priority;

  public MemberInfo(String ipAddress, int port, String login, String password, int priority) {
    this.ipAddress = ipAddress;
    this.port = port;
    this.login = login;
    this.password = password;
    this.priority = priority;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public int getPort() {
    return port;
  }

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }
  
  public int getPriority() {
    return priority;
  }
  
  public boolean equals (MemberInfo memberInfo) {
    return (ipAddress.equals(memberInfo.getIpAddress()) &&
            port == memberInfo.getPort() &&
            priority == memberInfo.getPriority());
  }
}

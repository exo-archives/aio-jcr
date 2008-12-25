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
package org.exoplatform.services.jcr.ext.replication.async.transport;

import org.jgroups.Address;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 25.12.2008
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: Member.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class Member {
  
  private final Address address;
  
  private int priority = -1;
  
  /**
   * Member  constructor.
   *
   * @param address
   *          address of memmber
   */
  public Member(Address address) {
    this.address = address;
  }
  
  public Member(Address address, int priority) {
    this.address = address;
    this.priority = priority;
  }

  /**
   * Get address of member.
   *
   * @return Address
   *           return address of member 
   */
  public Address getAddress() {
    return address;
  }
}

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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jgroups.Address;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 25.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: Member.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class Member implements Externalizable{

  private Address address;

  private int     priority;

  public Member(){
  }
  
  /**
   * Member constructor.
   * 
   * @param address
   *          address of member
   */
  public Member(Address address) {
    this.address = address;
    this.priority = -1;
  }

  public Member(Address address, int priority) {
    this.address = address;
    this.priority = priority;
  }

  /**
   * Get member name in format "priority. address". <br/>For information purpose only. Use address
   * and priority in comparisons.
   * 
   * @return String
   * 
   * @see {@link Member.getAddress()}
   */
  public String getName() {
    return this.priority + ". " + this.address;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Member)
      return this.address.equals(((Member) obj).address)
          && this.priority == ((Member) obj).priority;
    else
      return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return this.toString() + " [" + getName() + "]";
  }

  /**
   * Get address of member.
   * 
   * @return Address return address of member
   */
  public Address getAddress() {
    return address;
  }

  /**
   * @return the priority
   */
  public int getPriority() {
    return priority;
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    priority = in.readInt();
    address = (Address)in.readObject();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(this.priority);
    out.writeObject(address);
  }

}

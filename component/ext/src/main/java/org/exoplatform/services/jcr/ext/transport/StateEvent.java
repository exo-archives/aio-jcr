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
package org.exoplatform.services.jcr.ext.transport;

import java.util.List;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 25.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: StateEvent.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class StateEvent {

  private final List<MemberAddress> members;

  private final MemberAddress       localMember;

  /**
   * @return the members
   */
  public List<MemberAddress> getMembers() {
    return members;
  }

  public StateEvent(MemberAddress localMember, List<MemberAddress> members) {
    this.members = members;
    this.localMember = localMember;
  }

  public MemberAddress getLocalMember() {
    return localMember;
  }

  public boolean isCoordinator() {
    return members.get(0).getAddress().equals(localMember.getAddress());
  }
}

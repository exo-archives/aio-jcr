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
package org.exoplatform.services.jcr.ext.organization;

import org.exoplatform.services.organization.Membership;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MembershipImpl implements Membership {

  /**
   * The group id
   */
  private String groupId;

  /**
   * The membership type id
   */
  private String membershipType;

  /**
   * The user name
   */
  private String userName;

  MembershipImpl() {
  }

  MembershipImpl(String userName, String groupId, String membershipType) {
    this.userName = userName;
    this.groupId = groupId;
    this.membershipType = membershipType;
  }

  /**
   * @return The group id
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @deprecated This method is not used
   * @return The id of the membership
   */
  public String getId() {
    return null;
  }

  /**
   * @return The membership type id
   */
  public String getMembershipType() {
    return membershipType;
  }

  /**
   * @return The user name which belong to this membership
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Set membership type
   * 
   * @param type
   *          The new membership type
   */
  public void setMembershipType(String type) {
    membershipType = type;
  }

}

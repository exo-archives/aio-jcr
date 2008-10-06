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
 * @version $Id: MembershipImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class MembershipImpl implements Membership {

  private String groupId;

  private String membershipType;

  private String userName;

  MembershipImpl() {
  }

  MembershipImpl(String groupId, String membershipType) {
    this.groupId = groupId;
    this.membershipType = membershipType;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.organization.Membership#getGroupId()
   */
  public String getGroupId() {
    return groupId;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.organization.Membership#getId()
   */
  public String getId() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.organization.Membership#getMembershipType()
   */
  public String getMembershipType() {
    return membershipType;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.organization.Membership#getUserName()
   */
  public String getUserName() {
    return userName;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.organization.Membership#setMembershipType(java
   * .lang.String)
   */
  public void setMembershipType(String type) {
    membershipType = type;
  }

}

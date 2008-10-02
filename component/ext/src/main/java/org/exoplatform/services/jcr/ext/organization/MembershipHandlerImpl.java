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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * 
 * TODO seems nodetypes and/or existing interfaces of API don't relate one to other.
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: MembershipHandler.java 111 2008-11-11 11:11:11Z peterit $
 */
public class MembershipHandlerImpl implements
    org.exoplatform.services.organization.MembershipHandler {

  protected final JCROrganizationServiceImpl    service;

  protected final List<MembershipEventListener> listeners = new ArrayList<MembershipEventListener>();

  MembershipHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#addMembershipEventListener(org.exoplatform.services.organization.MembershipEventListener)
   */
  public void addMembershipEventListener(MembershipEventListener listener) {
    listeners.add(listener);
  }

  public void removeMembershipEventListener(MembershipEventListener listener) {
    listeners.remove(listener);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#createMembership(org.exoplatform.services.organization.Membership, boolean)
   */
  public void createMembership(Membership m, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#createMembershipInstance()
   */
  public Membership createMembershipInstance() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#findMembership(java.lang.String)
   */
  public Membership findMembership(String id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#findMembershipByUserGroupAndType(java.lang.String, java.lang.String, java.lang.String)
   */
  public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#findMembershipsByGroup(org.exoplatform.services.organization.Group)
   */
  public Collection findMembershipsByGroup(Group group) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#findMembershipsByUser(java.lang.String)
   */
  public Collection findMembershipsByUser(String userName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#findMembershipsByUserAndGroup(java.lang.String, java.lang.String)
   */
  public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#linkMembership(org.exoplatform.services.organization.User, org.exoplatform.services.organization.Group, org.exoplatform.services.organization.MembershipType, boolean)
   */
  public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#removeMembership(java.lang.String, boolean)
   */
  public Membership removeMembership(String id, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.organization.MembershipHandler#removeMembershipByUser(java.lang.String, boolean)
   */
  public Collection removeMembershipByUser(String username, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}

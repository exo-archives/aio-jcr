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
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 24.07.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: GroupHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class GroupHandlerImpl implements GroupHandler {

  protected final JCROrganizationServiceImpl service;
  
  protected final List<GroupEventListener> listeners  = new ArrayList<GroupEventListener>();
  
  GroupHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }
  
  public void addChild(Group parent, Group child, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub

  }

  public void addGroupEventListener(GroupEventListener listener) {
    listeners.add(listener);
  }
  
  public void removeGroupEventListener(GroupEventListener listener) {
    listeners.remove(listener);
  }

  public void createGroup(Group group, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub

  }

  public Group createGroupInstance() {
    // TODO Auto-generated method stub
    return null;
  }

  public Group findGroupById(String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection findGroupByMembership(String userName, String membershipType) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection findGroups(Group parent) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection findGroupsOfUser(String user) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getAllGroups() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Group removeGroup(Group group, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void saveGroup(Group group, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub

  }

}

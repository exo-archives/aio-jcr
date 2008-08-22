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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 24.07.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: MembershipTypeHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class MembershipTypeHandlerImpl implements MembershipTypeHandler {

  protected final JCROrganizationServiceImpl service;
  
  MembershipTypeHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }
  
  /**
   * // TODO check if method used.
   */
  public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    
    Date now = Calendar.getInstance().getTime() ;
    mt.setCreatedDate(now) ;
    mt.setModifiedDate(now) ;
    
    return mt;
  }

  public MembershipType createMembershipTypeInstance() {
    return new MembershipTypeImpl();
  }

  public MembershipType findMembershipType(String name) throws Exception {
    
    Session session = service.getStorageSession();
    try {
      Node mtNode = (Node) session.getItem(service.getStoragePath() +"/exo:membershipTypes/" + name);
      
      MembershipType mt = new MembershipTypeImpl();
      mt.setName(mtNode.getName());
      mt.setDescription(mtNode.getProperty("exo:description").getString());
      
      // TODO fix noetype
  //    mt.setCreatedDate(d);
  //    mt.setModifiedDate(d);
  //    mt.setOwner(s);
      
      return null;
    } finally {
      session.logout();
    }
  }

  public Collection findMembershipTypes() throws Exception {
      Session session = service.getStorageSession();
      try {
        Node storageNode = (Node) session.getItem(service.getStoragePath());
        
        List<MembershipType> types = new ArrayList<MembershipType>();
        
        for (NodeIterator nodes = storageNode.getNodes("exo:membershipTypes"); nodes.hasNext();) {
          Node mtNode = nodes.nextNode();
          MembershipType mt = new MembershipTypeImpl();
          mt.setName(mtNode.getName());
          mt.setDescription(mtNode.getProperty("exo:description").getString());
          // TODO fix noetype
          //    mt.setCreatedDate(d);
          //    mt.setModifiedDate(d);
          //    mt.setOwner(s);
          
          types.add(mt);
        }

        return types;
      } finally {
        session.logout();
      }
  }

  public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO update memebership
    return null;
  }

}

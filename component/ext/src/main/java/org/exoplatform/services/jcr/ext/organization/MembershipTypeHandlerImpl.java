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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: MembershipTypeHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class MembershipTypeHandlerImpl implements MembershipTypeHandler {

  public static final String                 STORAGE_EXO_DESCRIPTION      = "exo:description";

  public static final String                 STORAGE_EXO_MEMBERSHIP_TYPES = "/exo:membershipTypes";

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl service;

  MembershipTypeHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * @see org.exoplatform.services.organization.MembershipTypeHandler#
   *      createMembershipTypeInstance()
   */
  public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO: implement broadcast
    if (mt.getName().length() == 0) {
      throw new OrganizationServiceException("Name of membership type can not be empty.");
    }

    Session session = service.getStorageSession();
    try {
      Node mtNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_MEMBERSHIP_TYPES);
      mtNode = mtNode.addNode(mt.getName());
      mtNode.setProperty(STORAGE_EXO_DESCRIPTION, mt.getDescription());
      session.save();
      return mt;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipTypeHandler# createMembershipTypeInstance
   *      ()
   */
  public MembershipType createMembershipTypeInstance() {
    return new MembershipTypeImpl();
  }

  /**
   * @see org.exoplatform.services.organization.MembershipTypeHandler#findMembershipType
   *      (java.lang.String)
   */
  public MembershipType findMembershipType(String name) throws Exception {
    MembershipType mt = null;
    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath()
          + STORAGE_EXO_MEMBERSHIP_TYPES);

      for (NodeIterator nodes = storageNode.getNodes(name); nodes.hasNext();) {
        if (mt != null) {
          throw new OrganizationServiceException("More than one membership type " + name
              + " is found");
        }
        Node mtNode = nodes.nextNode();

        mt = new MembershipTypeImpl();
        mt.setName(mtNode.getName());
        mt.setDescription(mtNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
      }
      return mt;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipTypeHandler# findMembershipTypes ()
   */
  public Collection findMembershipTypes() throws Exception {
    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath()
          + STORAGE_EXO_MEMBERSHIP_TYPES);

      List<MembershipType> types = new ArrayList<MembershipType>();

      for (NodeIterator nodes = storageNode.getNodes(); nodes.hasNext();) {
        Node mtNode = nodes.nextNode();
        MembershipType mt = new MembershipTypeImpl();
        mt.setName(mtNode.getName());
        mt.setDescription(mtNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());

        types.add(mt);
      }

      return types;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipTypeHandler# removeMembershipType
   *      (java.lang.String, boolean)
   */
  public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception {
    // TODO: broadcast
    Session session = service.getStorageSession();
    MembershipType mt = null;
    try {
      Node mtNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_MEMBERSHIP_TYPES
          + "/" + name);

      mt = new MembershipTypeImpl();
      mt.setName(name);
      mt.setDescription(mtNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
      mtNode.remove();
      session.save();
      return mt;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipTypeHandler# saveMembershipType
   *      (org.exoplatform.services.organization.MembershipType, boolean)
   */
  public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO broadcast
    Session session = service.getStorageSession();
    try {
      Node mtNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_MEMBERSHIP_TYPES
          + "/" + mt.getName());
      mtNode.setProperty(STORAGE_EXO_DESCRIPTION, mt.getDescription());
      session.save();
      return mt;
    } finally {
      session.logout();
    }
  }

}

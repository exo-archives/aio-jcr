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

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 6 Жов 2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
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
   * {@inheritDoc}
   */
  public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO implement broadcast
    if (mt.getName().length() == 0) {
      throw new OrganizationServiceException("The name of membership type can not be empty.");
    }

    Session session = service.getStorageSession();
    try {
      Node storagePath = (Node) session.getItem(service.getStoragePath()
          + STORAGE_EXO_MEMBERSHIP_TYPES);
      try {
        Node mtNode = storagePath.addNode(mt.getName());
        mtNode.setProperty(STORAGE_EXO_DESCRIPTION, mt.getDescription());
        session.save();
        return new MembershipTypeImpl(mt.getName(), mt.getDescription(), mtNode.getUUID());
      } catch (ItemExistsException e) {
        throw new OrganizationServiceException("The membership type " + mt.getName() + " is exist.");
      }
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public MembershipType createMembershipTypeInstance() {
    return new MembershipTypeImpl();
  }

  /**
   * {@inheritDoc}
   */
  public MembershipType findMembershipType(String name) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node mtNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_MEMBERSHIP_TYPES
          + "/" + name);
      MembershipType mt = new MembershipTypeImpl(mtNode.getUUID());
      mt.setName(mtNode.getName());
      mt.setDescription(mtNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
      return mt;
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("The membership type with name " + name
          + " is absent.");
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
        MembershipType mt = new MembershipTypeImpl(mtNode.getUUID());
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
   * {@inheritDoc}
   */
  public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      Node mtNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_MEMBERSHIP_TYPES
          + "/" + name);

      MembershipType mt = new MembershipTypeImpl();
      mt.setName(name);
      mt.setDescription(mtNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
      mtNode.remove();
      session.save();
      return mt;
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("The membership type with name " + name
          + " is absent.");
    } finally {
      session.logout();
    }
  }

  public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      MembershipTypeImpl mtImpl = (MembershipTypeImpl) mt;
      if (mtImpl.getUUId() == null) {
        throw new OrganizationServiceException("");
      }

      try {
        Node mNode = session.getNodeByUUID(mtImpl.getUUId());
        String path = mNode.getPath();
        int pos = path.lastIndexOf('/');
        String prevName = path.substring(pos + 1);
        String srcPath = path.substring(0, pos);
        String destPath = srcPath + "/" + mt.getName();

        try {
          session.move(srcPath, destPath);
          try {
            Node nmtNode = (Node) session.getItem(destPath);
            nmtNode.setProperty(STORAGE_EXO_DESCRIPTION, mt.getDescription());
            MembershipType nmt = new MembershipTypeImpl(nmtNode.getUUID());
            nmt.setDescription(mt.getDescription());
            session.save();
            return nmt;
          } finally {
          }

        } catch (PathNotFoundException e) {
          throw new OrganizationServiceException("The membership type " + prevName
              + " is absent and can not be save.");
        } catch (ItemExistsException e) {
          throw new OrganizationServiceException("Can not save membership type " + prevName
              + " because new membership type " + mt.getName() + " already is exist.");
        }
      } catch (ItemNotFoundException e) {
        throw new OrganizationServiceException("Can not find membership type for save changes.");
      }
    } finally {
      session.logout();
    }
  }
}

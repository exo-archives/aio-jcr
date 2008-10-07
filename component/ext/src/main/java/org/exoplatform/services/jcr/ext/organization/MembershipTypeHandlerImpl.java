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
import java.util.Date;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MembershipTypeHandlerImpl extends CommonHandler implements MembershipTypeHandler {

  public static final String                 STORAGE_EXO_DESCRIPTION      = "exo:description";

  public static final String                 STORAGE_EXO_MEMBERSHIP_TYPES = "/exo:membershipTypes";

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl service;

  /**
   * MembershipTypeHandlerImpl constructor.
   * 
   * @param service
   *          The initialization data
   */
  MembershipTypeHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * Use this method to persist a new membership type. The developer usually should call the method
   * createMembershipTypeInstance, to create a new MembershipType, set the membership type data and
   * call this method to persist the membership type.
   * 
   * @param mt
   *          The new membership type that the developer want to persist
   * @param broadcast
   *          Broadcast the event if the broadcast value is 'true'
   * @return Return the MembershiptType object (the same as passed without update).
   * @throws Exception
   *           An exception is thrown if the method cannot access the database or a listener fail to
   *           handle the event
   */
  public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO implement broadcast
    checkMandatoryProperties(mt);

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
        throw new OrganizationServiceException("The membership type " + mt.getName() + " is exist.",
                                               e);
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
   * {@inheritDoc}
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

  /**
   * {@inheritDoc}
   */
  public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO implement broadcast
    checkMandatoryProperties(mt);

    Session session = service.getStorageSession();
    try {
      MembershipTypeImpl mtImpl = (MembershipTypeImpl) mt;
      if (mtImpl.getUUId() == null) {
        throw new OrganizationServiceException("Can not find membership type for save changes because UUId is null.");
      }

      try {
        Node mNode = session.getNodeByUUID(mtImpl.getUUId());
        String srcPath = mNode.getPath();
        int pos = srcPath.lastIndexOf('/');
        String prevName = srcPath.substring(pos + 1);
        String destPath = srcPath.substring(0, pos) + "/" + mt.getName();

        try {
          session.move(srcPath, destPath);
          try {
            Node nmtNode = (Node) session.getItem(destPath);
            nmtNode.setProperty(STORAGE_EXO_DESCRIPTION, mt.getDescription());
            session.save();
            return new MembershipTypeImpl(mt.getName(), mt.getDescription(), nmtNode.getUUID());
          } finally {
          }
        } catch (PathNotFoundException e) {
          throw new OrganizationServiceException("The membership type " + prevName
              + " is absent and can not be save.");
        } catch (ItemExistsException e) {
          throw new OrganizationServiceException("Can not save membership type " + prevName
              + " because new membership type " + mt.getName() + " is exist.");
        }
      } catch (ItemNotFoundException e) {
        throw new OrganizationServiceException("Can not find membership type for save changes by UUId.");
      }
    } finally {
      session.logout();
    }
  }

  @Override
  void checkMandatoryProperties(Object nodeType) throws Exception {
    MembershipType mt = (MembershipType) nodeType;
    if (mt.getName() == null) {
      throw new OrganizationServiceException("The name of membership type can not be empty.");
    }
  }

  @Override
  Date readDateProperty(Node node, String prop) throws Exception {
    try {
      return node.getProperty(prop).getDate().getTime();
    } catch (PathNotFoundException e) {
      return null;
    } catch (RepositoryException e) {
      throw new OrganizationServiceException("Can not get access to the database", e);
    }
  }

  @Override
  String readStringProperty(Node node, String prop) throws Exception {
    try {
      return node.getProperty(prop).getString();
    } catch (PathNotFoundException e) {
      return null;
    } catch (RepositoryException e) {
      throw new OrganizationServiceException("Can not get access to the database", e);
    }
  }

}

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
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MembershipTypeHandlerImpl extends CommonHandler implements MembershipTypeHandler {

  public static final String                 EXO_DESCRIPTION      = "exo:description";

  public static final String                 STORAGE_EXO_MEMBERSHIP_TYPES = "exo:membershipTypes";

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
   * {@inheritDoc}
   */
  public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception {
    // TODO implement broadcast
    checkMandatoryProperties(mt);

    Session session = service.getStorageSession();
    try {
      Node storagePath = (Node) session.getItem(service.getStoragePath() + "/"
          + STORAGE_EXO_MEMBERSHIP_TYPES);
      try {
        Node mtNode = storagePath.addNode(mt.getName());
        writeObjectToNode(mt, mtNode);
        session.save();
        return (MembershipType) readObjectFromNode(mtNode);
      } catch (ItemExistsException e) {
        throw new OrganizationServiceException("The membership type " + mt.getName() + " is exist.",
                                               e);
      }
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not create membership type", e);
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
      Node mtNode = (Node) session.getItem(service.getStoragePath() + "/"
          + STORAGE_EXO_MEMBERSHIP_TYPES + "/" + name);
      return (MembershipType) readObjectFromNode(mtNode);
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("The membership type " + name + " is absent.");
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership type", e);
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
      List<MembershipType> types = new ArrayList<MembershipType>();

      Node storageNode = (Node) session.getItem(service.getStoragePath() + "/"
          + STORAGE_EXO_MEMBERSHIP_TYPES);
      for (NodeIterator nodes = storageNode.getNodes(); nodes.hasNext();) {
        Node mtNode = nodes.nextNode();
        types.add((MembershipType) readObjectFromNode(mtNode));
      }

      return types;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership types", e);
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
      Node mtNode = (Node) session.getItem(service.getStoragePath() + "/"
          + STORAGE_EXO_MEMBERSHIP_TYPES + "/" + name);

      MembershipType mt = (MembershipType) readObjectFromNode(mtNode);
      mtNode.remove();
      session.save();
      return mt;
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("The membership type " + name + " is absent.");
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove membership type", e);
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
          if (!prevName.equals(mt.getName())) {
            session.move(srcPath, destPath);
          }
          try {
            Node nmtNode = (Node) session.getItem(destPath);
            writeObjectToNode(mt, nmtNode);
            session.save();
            return (MembershipType) readObjectFromNode(nmtNode);
          } catch (PathNotFoundException e) {
            throw new OrganizationServiceException("The membership type " + mt.getName()
                + " is absent and can not be save", e);
          }
        } catch (PathNotFoundException e) {
          throw new OrganizationServiceException("The membership type " + prevName
              + " is absent and can not be save", e);
        } catch (ItemExistsException e) {
          throw new OrganizationServiceException("Can not save membership type " + prevName
              + " because new membership type " + mt.getName() + " is exist", e);
        }

      } catch (ItemNotFoundException e) {
        throw new OrganizationServiceException("Can not find membership type for save changes by UUId",
                                               e);
      }
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save membership type", e);
    } finally {
      session.logout();
    }
  }

  private void checkMandatoryProperties(Object obj) throws Exception {
    MembershipType mt = (MembershipType) obj;
    if (mt.getName() == null || mt.getName().length() == 0) {
      throw new OrganizationServiceException("The name of membership type can not be null or empty.");
    }
  }

  private Object readObjectFromNode(Node node) throws Exception {
    try {
      MembershipType mt = new MembershipTypeImpl(node.getUUID());
      mt.setName(node.getName());
      mt.setDescription(readStringProperty(node, EXO_DESCRIPTION));
      return mt;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read membership type properties", e);
    }
  }

  private void writeObjectToNode(Object obj, Node node) throws Exception {
    MembershipType mt = (MembershipType) obj;
    try {
      node.setProperty(EXO_DESCRIPTION, mt.getDescription());
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write membership type properties", e);
    }
  }

}

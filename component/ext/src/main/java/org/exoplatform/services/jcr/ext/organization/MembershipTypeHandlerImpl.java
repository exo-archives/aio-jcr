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
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MembershipTypeHandlerImpl extends CommonHandler implements MembershipTypeHandler {

  public static final String                 EXO_DESCRIPTION              = "exo:description";

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
      Node mtNode = storagePath.addNode(mt.getName());
      writeObjectToNode(mt, mtNode);
      session.save();
      return readObjectFromNode(mtNode);

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
      return readObjectFromNode(mtNode);

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
        types.add(readObjectFromNode(mtNode));
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
      MembershipType mt = readObjectFromNode(mtNode);
      mtNode.remove();
      session.save();
      return mt;

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

      Node mNode = session.getNodeByUUID(mtImpl.getUUId());
      String srcPath = mNode.getPath();
      int pos = srcPath.lastIndexOf('/');
      String prevName = srcPath.substring(pos + 1);
      String destPath = srcPath.substring(0, pos) + "/" + mt.getName();

      if (!prevName.equals(mt.getName())) {
        session.move(srcPath, destPath);
      }

      Node nmtNode = (Node) session.getItem(destPath);
      writeObjectToNode(mt, nmtNode);
      session.save();
      return readObjectFromNode(nmtNode);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save membership type", e);
    } finally {
      session.logout();
    }
  }

  /**
   * Check that all mandatory properties of the membership type have a value.
   * 
   * @param mt
   *          The membership type to check
   * @throws Exception
   *           If one of properties is null or is empty.
   */
  private void checkMandatoryProperties(MembershipType mt) throws Exception {
    if (mt.getName() == null || mt.getName().length() == 0) {
      throw new OrganizationServiceException("The name of membership type can not be null or empty.");
    }
  }

  /**
   * Read membership type properties from the node in the storage.
   * 
   * @param node
   *          The node to read from
   * @return The membership type
   * @throws Exception
   *           An exception is thrown if method can not get access to the database
   */
  private MembershipType readObjectFromNode(Node node) throws Exception {
    try {
      MembershipType mt = new MembershipTypeImpl(node.getUUID());
      mt.setName(node.getName());
      mt.setDescription(readStringProperty(node, EXO_DESCRIPTION));
      return mt;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read membership type properties", e);
    }
  }

  /**
   * Write membership type properties to the node
   * 
   * @param mt
   *          The membership type
   * @param node
   *          The node in the storage
   * @throws Exception
   *           An exception is thrown if method can not get access to the database
   */
  private void writeObjectToNode(MembershipType mt, Node node) throws Exception {
    try {
      node.setProperty(EXO_DESCRIPTION, mt.getDescription());
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write membership type properties", e);
    }
  }

}

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
package org.exoplatform.services.jcr.impl.dataflow;

/**
 * Created by The eXo Platform SAS.
 * <br>
 * Newly added Node's data (used for mock inmemory repository as well). 
 * Besides NodeData's methods includes child items adders
 *
 * @author Gennady Azarenkov
 * @version $Id$
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemDataVisitor;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.MutableNodeData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.util.IdGenerator;

public class TransientNodeData extends TransientItemData implements Comparable, MutableNodeData,
    Externalizable {

  private static final long   serialVersionUID = -8675118546441306180L;

  protected AccessControlList acl;

  protected InternalQName     primaryTypeName;

  protected InternalQName[]   mixinTypeNames;

  protected int               orderNum;

  public TransientNodeData(QPath path,
                           String identifier,
                           int version,
                           InternalQName primaryTypeName,
                           InternalQName[] mixinTypeNames,
                           int orderNum,
                           String parentIdentifier,
                           AccessControlList acl) {
    super(path, identifier, version, parentIdentifier);
    this.primaryTypeName = primaryTypeName;
    this.mixinTypeNames = mixinTypeNames;
    this.orderNum = orderNum;
    this.acl = acl;
  }

  // --------------- ItemData ------------
  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#isNode()
   */
  public boolean isNode() {
    return true;
  }

  // ---------------- NodeData -------------

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getOrderNumber()
   */
  public int getOrderNumber() {
    return orderNum;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getPrimaryTypeName()
   */
  public InternalQName getPrimaryTypeName() {
    return primaryTypeName;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getMixinTypeNames()
   */
  public InternalQName[] getMixinTypeNames() {
    return mixinTypeNames;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getACL()
   */
  public AccessControlList getACL() {
    return acl;
  }

  // ---------------- MutableNodeData

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.MutableNodeData#setOrderNumber(int)
   */
  public void setOrderNumber(int orderNum) {
    this.orderNum = orderNum;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.datamodel.MutableNodeData#setMixinTypeNames(org.exoplatform.services
   * .jcr.datamodel.InternalQName[])
   */
  public void setMixinTypeNames(InternalQName[] mixinTypeNames) {
    this.mixinTypeNames = mixinTypeNames;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.MutableNodeData#setId(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.datamodel.MutableNodeData#setACL(org.exoplatform.services.jcr.
   * access.AccessControlList)
   */
  public void setACL(AccessControlList acl) {
    this.acl = acl;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.datamodel.ItemData#accept(org.exoplatform.services.jcr.dataflow
   * .ItemDataVisitor)
   */
  public void accept(ItemDataVisitor visitor) throws RepositoryException {
    visitor.visit(this);
  }

  /**
   * Factory method
   * 
   * @param parent
   * @param name
   * @param primaryTypeName
   * @return
   */
  public static TransientNodeData createNodeData(NodeData parent,
                                                 InternalQName name,
                                                 InternalQName primaryTypeName) {
    TransientNodeData nodeData = null;
    QPath path = QPath.makeChildPath(parent.getQPath(), name);
    nodeData = new TransientNodeData(path,
                                     IdGenerator.generate(),
                                     -1,
                                     primaryTypeName,
                                     new InternalQName[0],
                                     0,
                                     parent.getIdentifier(),
                                     parent.getACL());
    return nodeData;
  }

  /**
   * Factory method
   * 
   * @param parent
   * @param name
   * @param primaryTypeName
   * @return
   */
  public static TransientNodeData createNodeData(NodeData parent,
                                                 InternalQName name,
                                                 InternalQName primaryTypeName,
                                                 int index) {
    TransientNodeData nodeData = null;
    QPath path = QPath.makeChildPath(parent.getQPath(), name, index);
    nodeData = new TransientNodeData(path,
                                     IdGenerator.generate(),
                                     -1,
                                     primaryTypeName,
                                     new InternalQName[0],
                                     0,
                                     parent.getIdentifier(),
                                     parent.getACL());
    return nodeData;
  }

  /**
   * Factory method
   * 
   * @param parent
   * @param name
   * @param primaryTypeName
   * @return
   */
  public static TransientNodeData createNodeData(NodeData parent,
                                                 InternalQName name,
                                                 InternalQName primaryTypeName,
                                                 String identifier) {
    TransientNodeData nodeData = null;
    QPath path = QPath.makeChildPath(parent.getQPath(), name);
    nodeData = new TransientNodeData(path,
                                     identifier,
                                     -1,
                                     primaryTypeName,
                                     new InternalQName[0],
                                     0,
                                     parent.getIdentifier(),
                                     parent.getACL());
    return nodeData;
  }

  // ------------- Comparable /////

  public int compareTo(Object obj) {
    return ((NodeData) obj).getOrderNumber() - orderNum;
  }

  // Need for Externalizable
  // ------------------ [ BEGIN ] ------------------
  public TransientNodeData() {
    super();
    this.acl = new AccessControlList();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);

    out.writeInt(orderNum);

    out.writeInt(primaryTypeName.getAsString().getBytes().length);
    out.write(primaryTypeName.getAsString().getBytes());

    out.writeInt(mixinTypeNames.length);
    for (int i = 0; i < mixinTypeNames.length; i++) {
      out.writeInt(mixinTypeNames[i].getAsString().getBytes().length);
      out.write(mixinTypeNames[i].getAsString().getBytes());
    }

    acl.writeExternal(out);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);

    orderNum = in.readInt();

    byte[] buf;

    try {
      buf = new byte[in.readInt()];
      in.readFully(buf);
      String sQName = new String(buf, Constants.DEFAULT_ENCODING);
      primaryTypeName = InternalQName.parse(sQName);
    } catch (final IllegalNameException e) {
      throw new IOException(e.getMessage()) {
        private static final long serialVersionUID = 3489809179234435267L;

        @Override
        public Throwable getCause() {
          return e;
        }
      };
    }

    int count = in.readInt();

    mixinTypeNames = new InternalQName[count];

    for (int i = 0; i < count; i++) {
      try {
        buf = new byte[in.readInt()];
        in.readFully(buf);
        String sQName = new String(buf, Constants.DEFAULT_ENCODING);
        mixinTypeNames[i] = InternalQName.parse(sQName);
      } catch (final IllegalNameException e) {
        throw new IOException(e.getMessage()) {
          private static final long serialVersionUID = 3489809179234435268L; // eclipse gen

          @Override
          public Throwable getCause() {
            return e;
          }
        };
      }
    }

    acl.readExternal(in);
  }

  // ------------------ [ END ] ------------------

  // ------------ Cloneable ------------------

  @Override
  public TransientNodeData clone() {
    TransientNodeData dataCopy = new TransientNodeData(getQPath(),
                                                       getIdentifier(),
                                                       getPersistedVersion(),
                                                       getPrimaryTypeName(),
                                                       getMixinTypeNames(),
                                                       getOrderNumber(),
                                                       getParentIdentifier() != null
                                                           ? getParentIdentifier()
                                                           : null,
                                                       getACL());

    return dataCopy;
  }

  public TransientNodeData cloneAsSibling(int index) throws PathNotFoundException,
                                                    IllegalPathException {

    QPath siblingPath = QPath.makeChildPath(getQPath().makeParentPath(),
                                            getQPath().getName(),
                                            index);

    TransientNodeData dataCopy = new TransientNodeData(siblingPath,
                                                       getIdentifier(),
                                                       getPersistedVersion(),
                                                       getPrimaryTypeName(),
                                                       getMixinTypeNames(),
                                                       getOrderNumber(),
                                                       getParentIdentifier() != null
                                                           ? getParentIdentifier()
                                                           : null,
                                                       getACL());

    return dataCopy;
  }

  // -----------------------------------------
}

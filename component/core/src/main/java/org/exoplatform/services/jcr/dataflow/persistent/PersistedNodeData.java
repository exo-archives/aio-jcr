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
package org.exoplatform.services.jcr.dataflow.persistent;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemDataVisitor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
/**
 * Created by The eXo Platform SAS.</br>
 * 
 * Immutable NodeData from persistense storage
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: PersistedNodeData.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class PersistedNodeData extends PersistedItemData implements NodeData {
  
  protected final int orderNumber;
  protected final InternalQName primaryTypeName;
  protected final InternalQName[] mixinTypeNames;
  protected AccessControlList acl;
  protected final boolean isPrivilagable; 
  protected final boolean isOwnamble;
  
  public PersistedNodeData(String id, QPath qpath, String parentId, int version,
      int orderNumber, InternalQName primaryTypeName, InternalQName[] mixinTypeNames,
      AccessControlList acl, boolean isPrivilagable,boolean isOwnamble) {
    super(id, qpath, parentId, version);
    this.primaryTypeName = primaryTypeName;
    this.mixinTypeNames = mixinTypeNames;
    this.orderNumber = orderNumber;
    this.acl = acl;
    this.isPrivilagable = isPrivilagable;
    this.isOwnamble = isOwnamble;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getOrderNumber()
   */
  public int getOrderNumber() {
    return orderNumber;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getPrimaryTypeName()
   */
  public InternalQName getPrimaryTypeName() {
    return primaryTypeName;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getMixinTypeNames()
   */
  public InternalQName[] getMixinTypeNames() {
    return mixinTypeNames;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getACL()
   */
  public AccessControlList getACL() {
    return acl;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#setACL(org.exoplatform.services.jcr.access.AccessControlList)
   */
  public void setACL(AccessControlList acl) {
    this.acl = acl;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#accept(org.exoplatform.services.jcr.dataflow.ItemDataVisitor)
   */
  public void accept(ItemDataVisitor visitor) throws RepositoryException {
    visitor.visit(this);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#isNode()
   */
  public boolean isNode() {
    return true;
  }

  public boolean isPrivilagable() {
    return isPrivilagable;
  }

  public boolean isOwnamble() {
    return isOwnamble;
  }

}

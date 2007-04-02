/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow.persistent;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemDataVisitor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
/**
 * Created by The eXo Platform SARL        .</br>
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
  
  public PersistedNodeData(String id, QPath qpath, String parentId, int version,
      int orderNumber, InternalQName primaryTypeName, InternalQName[] mixinTypeNames,
      AccessControlList acl) {
    super(id, qpath, parentId, version);
    this.primaryTypeName = primaryTypeName;
    this.mixinTypeNames = mixinTypeNames;
    this.orderNumber = orderNumber;
    this.acl = acl;
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
  
//  /* (non-Javadoc)
//   * @see org.exoplatform.services.jcr.datamodel.NodeData#getOrderDirection()
//   */
//  public int getOrderDirection() {
//    return 0;
//  }
  
//  public boolean isOrdered() {
//    // For persisted node data - not ordered always 
//    return getOrderDirection() != 0;
//  }
  
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

}

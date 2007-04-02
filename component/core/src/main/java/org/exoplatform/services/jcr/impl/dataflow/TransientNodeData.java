/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.dataflow;

/**
 * Created by The eXo Platform SARL        .
 * <br>
 * Newly added Node's data (used for mock inmemory repository as well). 
 * Besides NodeData's methods includes child items adders
 *
 * @author Gennady Azarenkov
 * @version $Id: TransientNodeData.java 13421 2007-03-15 10:46:47Z geaz $
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
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.MutableNodeData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.TraverseableNodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.util.UUIDGenerator;

public class TransientNodeData extends TransientItemData implements Comparable,
    MutableNodeData, Externalizable  {

  private InternalQName primaryTypeName;

  private InternalQName[] mixinTypeNames;

  private int orderNum;

  private AccessControlList acl;
  
  public TransientNodeData(QPath path, String uuid, int version,
      InternalQName primaryTypeName, InternalQName[] mixinTypeNames,
      int orderNum, String parentUUID, AccessControlList acl) {
    super(path, uuid, version, parentUUID);
    this.primaryTypeName = primaryTypeName;
    this.mixinTypeNames = mixinTypeNames;
    this.orderNum = orderNum;    
    this.acl = acl;
  }

  
  // --------------- ItemData ------------
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#isNode()
   */
  public boolean isNode() {
    return true;
  }
  
  //---------------- NodeData -------------
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.NodeData#getOrderNumber()
   */
  public int getOrderNumber() {
    return orderNum;
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
  //---------------- MutableNodeData 

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.MutableNodeData#setOrderNumber(int)
   */
  public void setOrderNumber(int orderNum) {
    this.orderNum = orderNum;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.MutableNodeData#setMixinTypeNames(org.exoplatform.services.jcr.datamodel.InternalQName[])
   */
  public void setMixinTypeNames(InternalQName[] mixinTypeNames) {
    this.mixinTypeNames = mixinTypeNames;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.MutableNodeData#setUUID(java.lang.String)
   */
  public void setUUID(String uuid) {
    UUID = uuid;
    //this.hashCode = UUID.hashCode();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.MutableNodeData#setACL(org.exoplatform.services.jcr.access.AccessControlList)
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

  
  /**
   * Factory method
   * 
   * @param parent
   * @param name
   * @param primaryTypeName
   * @return
   */
  public static TransientNodeData createNodeData(NodeData parent,
      InternalQName name, InternalQName primaryTypeName) {
    TransientNodeData nodeData = null;
    QPath path = QPath.makeChildPath(parent.getQPath(), name);
    nodeData = new TransientNodeData(path,
        UUIDGenerator.generate(), -1, primaryTypeName, 
        new InternalQName[0], 0, parent.getUUID(), parent.getACL());
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
      InternalQName name, InternalQName primaryTypeName, int index) {
    TransientNodeData nodeData = null;
    QPath path = QPath.makeChildPath(parent.getQPath(), name, index);
    nodeData = new TransientNodeData(path,
        UUIDGenerator.generate(), -1, primaryTypeName, 
        new InternalQName[0], 0, parent.getUUID(), parent.getACL());
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
      InternalQName name, InternalQName primaryTypeName, String uuid) {
    TransientNodeData nodeData = null;
    QPath path = QPath.makeChildPath(parent.getQPath(), name);
    nodeData = new TransientNodeData(path,
        uuid, -1, primaryTypeName, 
        new InternalQName[0], 0, parent.getUUID(), parent.getACL());
    return nodeData;
  }
  
  // ------------- Comparable  /////

  public int compareTo(Object obj) {
    return ((NodeData) obj).getOrderNumber() - orderNum;
  }
  
//Need for Externalizable
//------------------ [ BEGIN ] ------------------
  public TransientNodeData() {
    super();
  }
  
  public void writeExternal(ObjectOutput out) throws IOException {
//    System.out.println("-->TransientNodeData --> writeExternal(ObjectOutput out)");
    
    super.writeExternal(out);
    
    out.writeInt(orderNum);
    
    out.writeInt(primaryTypeName.getAsString().getBytes().length);
    out.write(primaryTypeName.getAsString().getBytes());
    
    out.writeInt(mixinTypeNames.length);
    for(int i = 0; i < mixinTypeNames.length; i++){
      out.writeInt(mixinTypeNames[i].getAsString().getBytes().length);
      out.write(mixinTypeNames[i].getAsString().getBytes());
    }
    
    out.writeObject(acl);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//    System.out.println("-->TransientNodeData --> readExternal(ObjectInput in)");
    
    super.readExternal(in);
     
    orderNum = in.readInt();
    
    byte[] buf;
    
    try {
      buf = new byte[in.readInt()];
      in.read(buf);
      String sQName = new String(buf, Constants.DEFAULT_ENCODING);
      primaryTypeName = InternalQName.parse(sQName);
    } catch (IllegalNameException e) {
      // TODO throw exception 
      e.printStackTrace();
    }
    
    int count = in.readInt();
    
    mixinTypeNames = new InternalQName[count];
    
    for (int i = 0; i < count; i++) {
      try {
        buf = new byte[in.readInt()];
        in.read(buf);
        String sQName = new String(buf, Constants.DEFAULT_ENCODING);
        mixinTypeNames[i] = InternalQName.parse(sQName);
      } catch (IllegalNameException e) {
        // TODO throw exception
        e.printStackTrace();
      }
    }
    
    acl = (AccessControlList)in.readObject();
  }
  //------------------ [  END  ] ------------------
  
  // ------------ Cloneable ------------------
  
  @Override
  public TransientNodeData clone() {
    TransientNodeData dataCopy = new TransientNodeData(
        getQPath(), 
        getUUID(), 
        getPersistedVersion(),
        getPrimaryTypeName(), 
        getMixinTypeNames(),
        getOrderNumber(),
        getParentUUID() != null ? getParentUUID() : null,
        getACL());
        
    return dataCopy;
  }
  
  public TransientNodeData cloneAsSibling(int index) throws PathNotFoundException, IllegalPathException {
    
    QPath siblingPath = qpath.makeChildPath(
        getQPath().makeParentPath(),
        getQPath().getName(),
        index);
    
    TransientNodeData dataCopy = new TransientNodeData(
        siblingPath, 
        getUUID(), 
        getPersistedVersion(),
        getPrimaryTypeName(), 
        getMixinTypeNames(),
        getOrderNumber(),
        getParentUUID() != null ? getParentUUID() : null,
        getACL());
      
    return dataCopy;
  }
  
  // -----------------------------------------
}


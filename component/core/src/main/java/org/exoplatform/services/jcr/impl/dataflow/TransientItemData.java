/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.dataflow;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.MutableItemData;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: TransientItemData.java 12841 2007-02-16 08:58:38Z peterit $
 */
public abstract class TransientItemData implements MutableItemData, Externalizable {

  protected InternalQPath QPath;

  protected String UUID;

  protected String parentUUID;

  protected int persistedVersion;
  
  //protected int hashCode;
  

  /**
   * @param path QPath
   * @param uuid id
   * @param version persisted version
   * @param parentUUID parentId
   */
  TransientItemData(InternalQPath path, String uuid, int version, String parentUUID) {
    this.parentUUID = parentUUID != null ? parentUUID.intern() : null;
    this.UUID = uuid.intern();
    this.QPath = path;
    this.persistedVersion = version;
    //this.hashCode = initHashCode();
  }
  
  protected int initHashCode() {
    final int prime = 31;
    int hcode = prime * UUID.hashCode(); // [PN] 10.02.07 by uuid only
    //hcode = prime * hcode + QPath.hashCode();    
    //hcode = prime * hcode + (parentUUID != null ? parentUUID.hashCode() : 0);
    //hcode = prime * hcode + persistedVersion;
    return hcode; 
  }
  
//  @Override
//  public int hashCode() {
//    return hashCode;
//  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getQPath()
   */
  public InternalQPath getQPath() {
    return QPath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getUUID()
   */
  public String getUUID() {
    return UUID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getPersistedVersion()
   */
  public int getPersistedVersion() {
    return persistedVersion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getParentUUID()
   */
  public String getParentUUID() {
    return parentUUID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.MutableItemData#increasePersistedVersion()
   */
  public void increasePersistedVersion() {
    this.persistedVersion++;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    
    if (obj == null)
      return false;

    if (obj instanceof ItemData) {
      //return this.hashCode == obj.hashCode();
      //return getUUID().equals(((ItemData) obj).getUUID());
      return getUUID().hashCode() == ((ItemData) obj).getUUID().hashCode();
    } 

    return false;
  }
  
//  @Override
//  public int hashCode() {
//    return hashCode;
//  }

  /**
   * @return Qname - shortcut for getQPath().getName();
   */
  public InternalQName getQName() {
    return QPath.getName();
  }

//  serializable --------------
  TransientItemData() {
  }

  public void writeExternal(ObjectOutput out) throws IOException {
//    System.out.println("-->TransientItemData--> writeExternal(ObjectOutput out)");

    out.writeInt(QPath.getAsString().getBytes().length);
    out.write(QPath.getAsString().getBytes());

    out.writeInt(UUID.getBytes().length);
    out.write(UUID.getBytes());

    out.writeInt(parentUUID.getBytes().length);
    out.write(parentUUID.getBytes());

    out.writeInt(persistedVersion);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//    System.out.println("-->TransientItemData--> readExternal(ObjectInput in)");
    
    byte[] buf;

    try {
      buf = new byte[in.readInt()];
      in.read(buf);
      String sQPath = new String(buf, Constants.DEFAULT_ENCODING);
      QPath = InternalQPath.parse(sQPath);
    } catch (IllegalPathException e) {
      e.printStackTrace();
    }

    buf = new byte[in.readInt()/*UUIDGenerator.UUID_LENGTH*/];
    in.read(buf);
    UUID = new String(buf , Constants.DEFAULT_ENCODING).intern();

    buf = new byte[in.readInt()];
    in.read(buf);
    parentUUID = new String(buf, Constants.DEFAULT_ENCODING).intern();

    persistedVersion = in.readInt();
    
    //hashCode = initHashCode();
  }

}
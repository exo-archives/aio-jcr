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
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.MutableItemData;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */
public abstract class TransientItemData implements MutableItemData, Externalizable {

  protected QPath qpath;

  protected String identifier;

  protected String parentIdentifier;

  protected int persistedVersion;
  
  //protected int hashCode;
  

  /**
   * @param path QPath
   * @param identifier id
   * @param version persisted version
   * @param parentIdentifier parentId
   */
  TransientItemData(QPath path, String identifier, int version, String parentIdentifier) {
    this.parentIdentifier = parentIdentifier != null ? parentIdentifier.intern() : null;
    this.identifier = identifier.intern();
    this.qpath = path;
    this.persistedVersion = version;
    //this.hashCode = initHashCode();
  }
  
  protected int initHashCode() {
    final int prime = 31;
    int hcode = prime * identifier.hashCode(); // [PN] 10.02.07 by identifier only
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
  public QPath getQPath() {
    return qpath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getUUID()
   */
  public String getIdentifier() {
    return identifier;
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
  public String getParentIdentifier() {
    return parentIdentifier;
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
      return getIdentifier().hashCode() == ((ItemData) obj).getIdentifier().hashCode();
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
    return qpath.getName();
  }

//  serializable --------------
  TransientItemData() {
  }

  public void writeExternal(ObjectOutput out) throws IOException {
//    System.out.println("-->TransientItemData--> writeExternal(ObjectOutput out)");

    out.writeInt(qpath.getAsString().getBytes().length);
    out.write(qpath.getAsString().getBytes());

    out.writeInt(identifier.getBytes().length);
    out.write(identifier.getBytes());

    out.writeInt(parentIdentifier.getBytes().length);
    out.write(parentIdentifier.getBytes());

    out.writeInt(persistedVersion);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//    System.out.println("-->TransientItemData--> readExternal(ObjectInput in)");
    
    byte[] buf;

    try {
      buf = new byte[in.readInt()];
      in.read(buf);
      String sQPath = new String(buf, Constants.DEFAULT_ENCODING);
      qpath = QPath.parse(sQPath);
    } catch (IllegalPathException e) {
      e.printStackTrace();
    }

    buf = new byte[in.readInt()/*UUIDGenerator.UUID_LENGTH*/];
    in.read(buf);
    identifier = new String(buf , Constants.DEFAULT_ENCODING).intern();

    buf = new byte[in.readInt()];
    in.read(buf);
    parentIdentifier = new String(buf, Constants.DEFAULT_ENCODING).intern();

    persistedVersion = in.readInt();
    
    //hashCode = initHashCode();
  }

}
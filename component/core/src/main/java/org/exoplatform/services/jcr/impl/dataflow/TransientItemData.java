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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.MutableItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: TransientItemData.java 11907 2008-03-13 15:36:21Z ksm $
 */
public abstract class TransientItemData implements MutableItemData, Externalizable {
  
  private int NULL_VALUE = -1;
  
  private int NOT_NULL_VALUE = 1;

  protected QPath  qpath;

  protected String identifier;

  protected String parentIdentifier;

  protected int    persistedVersion;

  /**
   * @param path
   *          QPath
   * @param identifier
   *          id
   * @param version
   *          persisted version
   * @param parentIdentifier
   *          parentId
   */
  TransientItemData(QPath path, String identifier, int version, String parentIdentifier) {
    this.parentIdentifier = parentIdentifier != null ? parentIdentifier : null;
    this.identifier = identifier;
    this.qpath = path;
    this.persistedVersion = version;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getQPath()
   */
  public QPath getQPath() {
    return qpath;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getPersistedVersion()
   */
  public int getPersistedVersion() {
    return persistedVersion;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getParentUUID()
   */
  public String getParentIdentifier() {
    return parentIdentifier;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.MutableItemData#increasePersistedVersion()
   */
  public void increasePersistedVersion() {
    this.persistedVersion++;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    if (obj == null)
      return false;

    if (obj instanceof ItemData) {
      return getIdentifier().hashCode() == ((ItemData) obj).getIdentifier().hashCode();
    }

    return false;
  }

  /**
   * @return Qname - shortcut for getQPath().getName();
   */
  public InternalQName getQName() {
    return qpath.getName();
  }

  // serializable --------------
  TransientItemData() {
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(qpath.getAsString().getBytes().length);
    out.write(qpath.getAsString().getBytes());

    out.writeInt(identifier.getBytes().length);
    out.write(identifier.getBytes());

    if (parentIdentifier != null ) {
      out.writeInt(NOT_NULL_VALUE);  
      out.writeInt(parentIdentifier.getBytes().length);
      out.write(parentIdentifier.getBytes());
    } else 
      out.writeInt(NULL_VALUE);

    out.writeInt(persistedVersion);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    byte[] buf;

    try {
      buf = new byte[in.readInt()];
      in.read(buf);
      String sQPath = new String(buf, Constants.DEFAULT_ENCODING);
      qpath = QPath.parse(sQPath);
    } catch (IllegalPathException e) {
      e.printStackTrace();
    }

    buf = new byte[in.readInt()];
    in.read(buf);
    identifier = new String(buf, Constants.DEFAULT_ENCODING);

    int isNull = in.readInt();
    if (isNull == NOT_NULL_VALUE) { 
      buf = new byte[in.readInt()];
      in.read(buf);
      parentIdentifier = new String(buf, Constants.DEFAULT_ENCODING);
    }

    persistedVersion = in.readInt();
  }

}

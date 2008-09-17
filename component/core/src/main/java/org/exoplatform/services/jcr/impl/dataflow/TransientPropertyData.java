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
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataVisitor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.MutablePropertyData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TransientPropertyData.java 13962 2008-05-07 16:00:48Z pnedonosko $
 */

public class TransientPropertyData extends TransientItemData implements MutablePropertyData, Externalizable {
  
  private static final long serialVersionUID = -8224902483861330191L;
  
  protected final static int NULL_VALUES = -1; 
  
  protected List<ValueData> values;

  protected int             type;

  protected boolean         multiValued = false;

  /**
   * @param path qpath
   * @param identifier id
   * @param version persisted version
   * @param type property type
   * @param parentIdentifier parentId
   * @param multiValued multi-valued state
   */
  public TransientPropertyData(QPath path, String identifier, int version, int type,
      String parentIdentifier, boolean multiValued) {
    super(path, identifier, version, parentIdentifier);
    this.type = type;
    this.multiValued = multiValued;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ItemData#isNode()
   */
  public boolean isNode() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.PropertyData#getType()
   */
  public int getType() {
    return type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.PropertyData#getValues()
   */
  public List<ValueData> getValues() {
    return values;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.PropertyData#isMultiValued()
   */
  public boolean isMultiValued() {
    return multiValued;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.MutablePropertyData#setValues(java.util.List)
   */
  public void setValues(List<ValueData> values) {
    this.values = values;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.MutablePropertyData#setType(int)
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Shortcut for single-valued property data initialization
   * 
   * @param value
   */
  public void setValue(ValueData value) {
    this.values = new ArrayList<ValueData>();
    values.add(value);
  }

  public static TransientPropertyData createPropertyData(NodeData parent, InternalQName name,
      int type, boolean multiValued) {
    TransientPropertyData propData = null;
    QPath path = QPath.makeChildPath(parent.getQPath(), name);
    propData = new TransientPropertyData(path, IdGenerator.generate(), -1, type,
        parent.getIdentifier(), multiValued);

    return propData;
  }

  public static TransientPropertyData createPropertyData(NodeData parent, InternalQName name,
      int type, boolean multiValued, ValueData value) {
    TransientPropertyData propData = createPropertyData(parent, name, type, multiValued);
    propData.setValue(value);
    return propData;
  }

  public static TransientPropertyData createPropertyData(NodeData parent, InternalQName name,
      int type, boolean multiValued, List<ValueData> values) {
    TransientPropertyData propData = createPropertyData(parent, name, type, multiValued);
    propData.setValues(values);
    return propData;
  }

  public void accept(ItemDataVisitor visitor) throws RepositoryException {
    visitor.visit(this);
  }

// ------------ Cloneable ------------------
  
  /**
   * Clone node data without value data!!!
   */
  @Override
  public TransientPropertyData clone() {
    TransientPropertyData dataCopy = new TransientPropertyData(
        getQPath(), 
        getIdentifier(), 
        getPersistedVersion(),
        getType(), 
        getParentIdentifier(),
        isMultiValued());
    
    List<ValueData> copyValues = new ArrayList<ValueData>(); 
    try {
      for (ValueData vdata: getValues()) {
        copyValues.add(((TransientValueData) vdata).createTransientCopy());
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    dataCopy.setValues(copyValues);
    
    return dataCopy;
  }
  
// ----------------- Externalizable
  public TransientPropertyData() {
    super();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);

    out.writeInt(type);
    out.writeBoolean(multiValued);

    if (values != null) {
      int listSize = values.size();
      out.writeInt(listSize);
      for (int i = 0; i < listSize; i++) 
        out.writeObject(values.get(i));
    } else {
      out.writeInt(NULL_VALUES);
    }
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
        
    type = in.readInt();
    
    multiValued = in.readBoolean();

    int listSize = in.readInt();
    if (listSize  != NULL_VALUES) {
      values = new ArrayList<ValueData>();
      for (int i = 0; i < listSize; i++)
        values.add((ValueData) in.readObject());
    }
  }
}
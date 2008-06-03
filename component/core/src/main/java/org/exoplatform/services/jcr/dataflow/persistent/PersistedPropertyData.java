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

import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataVisitor;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;

/**
 * Created by The eXo Platform SAS.</br>
 * 
 * Persisted PropertyData
 * 
 * @author Gennady Azarenkov
 * @version $Id: PersistedPropertyData.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class PersistedPropertyData extends PersistedItemData implements PropertyData {

  protected List <ValueData> values;
  protected final int type;
  protected final boolean multiValued;
  
  public PersistedPropertyData(String id, QPath qpath, String parentId, int version,
      int type, boolean multiValued) {
    super(id, qpath, parentId, version);
    this.values = null;
    this.type = type;
    this.multiValued = multiValued;
  }

  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.PropertyData#getType()
   */
  public int getType() {
    return type;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.PropertyData#getValues()
   */
  public List<ValueData> getValues() {
    return values;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.PropertyData#isMultiValued()
   */
  public boolean isMultiValued() {
    return multiValued;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#isNode()
   */
  public boolean isNode() {
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#accept(org.exoplatform.services.jcr.dataflow.ItemDataVisitor)
   */
  public void accept(ItemDataVisitor visitor) throws RepositoryException {
    visitor.visit(this);
  }  

  /**
   * @param type
   */
  public void setType(int type) {
    throw new RuntimeException("DO NOT call setType! ");
  }

  /**
   * @param values
   * @throws RepositoryException
   */
  public void setValues(List values) throws RepositoryException {
    if(this.values == null)
      this.values = values;
    else
      throw new RuntimeException("The values can not be changed ");
  }

}

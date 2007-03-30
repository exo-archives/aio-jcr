/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow.persistent;

import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataVisitor;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;

/**
 * Created by The eXo Platform SARL        .</br>
 * 
 * Persisted PropertyData
 * 
 * @author Gennady Azarenkov
 * @version $Id: PersistedPropertyData.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class PersistedPropertyData extends PersistedItemData implements PropertyData {

  protected List <ValueData> values;
  protected final int type;
  protected final boolean multiValued;
  
  public PersistedPropertyData(String id, InternalQPath qpath, String parentId, int version,
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

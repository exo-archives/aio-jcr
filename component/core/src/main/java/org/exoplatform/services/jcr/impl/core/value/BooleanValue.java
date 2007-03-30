/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * a boolean value implementation.
 * 
 * @author Gennady Azarenkov
 */
public class BooleanValue extends BaseValue {

  public static final int TYPE = PropertyType.BOOLEAN;
  
  public BooleanValue(boolean bool)  throws IOException {
    super(TYPE, new TransientValueData(bool));
  }

  public BooleanValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getDate()
   */
  public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException("conversion to date failed: inconvertible types");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getLong()
   */
  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException("conversion to long failed: inconvertible types");
  }

//  /**
//   * @see Value#getBoolean
//   */
//  public boolean getBoolean() throws ValueFormatException, IllegalStateException,
//      RepositoryException {
//    setValueConsumed();
//    Boolean bool = new Boolean(getInternalString());
//    if (bool != null) {
//      return bool.booleanValue();
//    } else {
//      throw new ValueFormatException("empty value");
//    }
//  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getDouble()
   */
  public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException("conversion to double failed: inconvertible types");
  }
}

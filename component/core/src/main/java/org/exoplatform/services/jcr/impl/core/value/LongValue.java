/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * A <code>LongValue</code> provides an implementation of the
 * <code>Value</code> interface representing a long value.
 * 
 * @author Gennady Azarenkov 
 */
public class LongValue extends BaseValue {

  public static final int TYPE = PropertyType.LONG;

  public LongValue(long l) throws IOException {
    super(TYPE, new TransientValueData(l));
  }

  LongValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }

  /**
   * @see Value#getDate
   */
  public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
    //setValueConsumed();
    
    Long longNumber = new Long(getInternalString());

    if (longNumber != null) {
      // loosing timezone information...
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date(longNumber.longValue()));
      return cal;
    } else {
      throw new ValueFormatException("empty value");
    }
  }

//  /**
//   * @see Value#getLong
//   */
//  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
//    setValueConsumed();
//
//    Long longNumber = new Long(getInternalString());
//
//    if (longNumber != null) {
//      return longNumber.longValue();
//    } else {
//      throw new ValueFormatException("empty value");
//    }
//  }

  /**
   * @see Value#getBoolean
   */
  public boolean getBoolean() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException("conversion to boolean failed: inconvertible types");
  }

  /**
   * @see Value#getDouble
   */
  public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
    //setValueConsumed();
    
    Long longNumber = new Long(getInternalString());

    if (longNumber != null) {
      return longNumber.doubleValue();
    } else {
      throw new ValueFormatException("empty value");
    }
  }
}

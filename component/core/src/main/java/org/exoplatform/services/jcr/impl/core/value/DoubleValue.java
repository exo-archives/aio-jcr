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
 * a double value impl.
 *
 * @author Gennady Azarenkov
 */
public class DoubleValue extends BaseValue {

  public static final int TYPE = PropertyType.DOUBLE;

  public DoubleValue(double dbl)  throws IOException  {
    super(TYPE, new TransientValueData(dbl));
  }

  DoubleValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }

  /**
   * @see Value#getDate
   */
  public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
    Double doubleNumber = new Double(getInternalString());

    if (doubleNumber != null) {
      // loosing timezone information...
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date(doubleNumber.longValue()));
      return cal;
    } else {
      throw new ValueFormatException("empty value");
    }
  }

  /**
   * @see Value#getLong
   */
  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
    Double doubleNumber = new Double(getInternalString());

    if (doubleNumber != null) {
      return doubleNumber.longValue();
    } else {
      throw new ValueFormatException("empty value");
    }
  }

  /**
   * @see Value#getBoolean
   */
  public boolean getBoolean() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    throw new ValueFormatException("conversion to boolean failed: inconvertible types");
  }
}

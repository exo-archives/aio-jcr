/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;

/**
 * a date value implementation.
 * 
 * @author Gennady Azarenkov
 */
public class DateValue extends BaseValue {

  public static final int TYPE = PropertyType.DATE;

  /**
   * Constructs a <code>DateValue</code> object representing a date.
   * 
   * @param date the date this <code>DateValue</code> should represent s
   */
  public DateValue(Calendar date) throws IOException {
    super(TYPE, new TransientValueData(date));
  }

  DateValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }

  /**
   * @see BaseValue#getInternalString()
   */
  protected String getInternalString() throws ValueFormatException, RepositoryException {
    Calendar date = getInternalCalendar();

    if (date != null) {
      return JCRDateFormat.format(date);
    } 
    
    throw new ValueFormatException("empty value");
  }

  /**
   * @see Value#getLong
   */
  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
    Calendar date = getInternalCalendar();

    if (date != null) {
      return date.getTimeInMillis();
    } else {
      throw new ValueFormatException("empty value");
    }
  }

  /**
   * @see Value#getBoolean
   */
  public boolean getBoolean() throws ValueFormatException, IllegalStateException,
      RepositoryException {

    throw new ValueFormatException("cannot convert date to boolean");
  }

  /**
   * @see Value#getDouble
   */
  public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
    Calendar date = getInternalCalendar();

    if (date != null) {
      long ms = date.getTimeInMillis();
      if (ms <= Double.MAX_VALUE) {
        return ms;
      }
      throw new ValueFormatException("conversion from date to double failed: inconvertible types");
    } else {
      throw new ValueFormatException("empty value");
    }
  }

  // @Override
  public long getLength() {
    try {
      return getInternalString().length();
    } catch (Throwable e) {
      return super.getLength();
    }
  }

  @Override
  public InputStream getStream() throws ValueFormatException, RepositoryException {

    try {
      if(data == null ){
        String inernalString = getInternalString();
        
        //force replace of data
        data = new LocalTransientValueData(true);
        
        //Replace internall stram
        data.stream = new ByteArrayInputStream(inernalString.getBytes(
            Constants.DEFAULT_ENCODING));
      }
      return data.getAsStream();
    } catch (UnsupportedEncodingException e) {
      throw new RepositoryException(Constants.DEFAULT_ENCODING + " not supported on this platform",
          e);
    } 
     catch (IOException e) {
      throw new RepositoryException(e);
    }

  }
}

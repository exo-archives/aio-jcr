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
      if(data == null ) {
        String inernalString = getInternalString();
        
        //force replace of data
        data = new LocalTransientValueData(true);
        
        //Replace internall stram
        data.stream = new ByteArrayInputStream(inernalString.getBytes(Constants.DEFAULT_ENCODING));
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

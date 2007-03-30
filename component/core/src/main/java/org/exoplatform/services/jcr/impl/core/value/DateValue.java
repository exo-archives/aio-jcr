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

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.value.BaseValue.LocalTransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.DateFormatHelper;

/**
 * a date value implementation.
 * 
 * @author Gennady Azarenkov
 */
public class DateValue extends BaseValue {

  public static final int TYPE = PropertyType.DATE;

  // protected static Log log = ExoLogger.getLogger("jcr.DateValue");

  // private final Calendar date;

  /**
   * Constructs a <code>DateValue</code> object representing a date.
   * 
   * @param date the date this <code>DateValue</code> should represent s
   */
  public DateValue(Calendar date) throws IOException {
    super(TYPE, new TransientValueData(date));
    // this.date = date;
    // this.date = (Calendar) date.clone();
  }

  DateValue(TransientValueData data) throws IOException {
    super(TYPE, data);
    // this.date = ISO8601.parse(new String(data.getAsByteArray()));
    // try {
    // this.date = deserializeDate(data.getAsByteArray());
    // } catch(IOException e) {
    // throw new RuntimeException("FATAL ERROR IOException occured: " +
    // e.getMessage(), e);
    // }
  }

  // /**
  // * Serializing calendar date in string containing
  // * ISO8601 formated data (YYYY-MM-DDThh:mm:ss.SSSTZD)
  // * plus information about specific java.util.Calendar fields
  // * (isLenient, getFirstDayOfWeek, getMinimalDaysInFirstWeek and
  // getTimeZone).
  // *
  // * @param date
  // * @return
  // */
  // static public byte[] serializeDate(Calendar date) {
  //    
  // return new DateFormatHelper().serialize(date);
  // }

  // @Deprecated
  // private byte[] serializeCalendar(Calendar date) {
  // if (date == null) {
  // throw new IllegalArgumentException("argument can not be null");
  // }
  //
  // ByteArrayOutputStream baos = new ByteArrayOutputStream();
  // ObjectOutputStream out = null;
  // try {
  // out = new ObjectOutputStream(baos);
  // out.writeObject(date);
  // out.close();
  // return baos.toByteArray();
  // } catch (Exception e) {
  // log.error("Error serialize date value (Calendar) from object " + date
  // + ". Error: " + e);
  // }
  // return null;
  // }

  // /**
  // * Deserializing calendar date from string containing
  // * ISO8601 formated data (YYYY-MM-DDThh:mm:ss.SSSTZD)
  // * plus information about specific java.util.Calendar fields
  // * (isLenient, getFirstDayOfWeek, getMinimalDaysInFirstWeek and
  // getTimeZone).
  // *
  // * @param dateBytes
  // * @return
  // */
  // public static Calendar deserializeDate(byte[] dateBytes) {
  //    
  // try {
  // //serString = new String(dateBytes, DEFAULT_ENCODING);
  // return new DateFormatHelper().deserialize(new String(dateBytes,
  // Constants.DEFAULT_ENCODING));
  // } catch(UnsupportedEncodingException e) {
  // log.warn("Error deserialize date value (Calendar) from bytes '"
  // + new String(dateBytes) + "' with DEFAULT_ENCODING " +
  // Constants.DEFAULT_ENCODING, e);
  // //serString = new String(dateBytes);
  // return new DateFormatHelper().deserialize(new String(dateBytes));
  // }
  // }

  // @Deprecated
  // private Calendar deserializeCalendar(byte[] dateBytes) {
  // if (dateBytes == null) {
  // throw new IllegalArgumentException("argument can not be null");
  // }
  //    
  // ByteArrayInputStream bais = new ByteArrayInputStream(dateBytes);
  // ObjectInputStream in = null;
  // try {
  // in = new ObjectInputStream(bais);
  // Object o = in.readObject();
  // in.close();
  // return o instanceof Calendar ? (Calendar) o : null;
  // } catch (Exception e) {
  // log.error("Error deserialize date value (Calendar) from bytes '"
  // + new String(dateBytes) + "'. Error: " + e);
  // }
  // return null;
  // }

  // /**
  // * Indicates whether some other object is "equal to" this one.
  // * <p>
  // * The result is <code>true</code> if and only if the argument is not
  // * <code>null</code> and is a <code>DateValue</code> object that
  // * represents the same value as this object.
  // *
  // * @param obj the reference object with which to compare.
  // * @return <code>true</code> if this object is the same as the obj argument;
  // * <code>false</code> otherwise.
  // */
  // public boolean equals(Object obj) {
  // if (this == obj) {
  // return true;
  // }
  // if (obj instanceof DateValue) {
  // DateValue other = (DateValue) obj;
  // if (date == other.date) {
  // return true;
  // } else if (date != null && other.date != null) {
  // return date.equals(other.date);
  // }
  // }
  // return false;
  // }

  /**
   * @see BaseValue#getInternalString()
   */
  protected String getInternalString() throws ValueFormatException, RepositoryException {
    Calendar date = getInternalCalendar();
    if (date != null) {
      return ISO8601.format(date);
    } else {
      throw new ValueFormatException("empty value");
    }
  }

  // ----------------------------------------------------------------< Value >
  // /**
  // * @see Value#getDate
  // */
  // public Calendar getDate() throws ValueFormatException,
  // IllegalStateException, RepositoryException {
  // //setValueConsumed();
  //
  // //System.out.println("DATE >>>>>>>>>>>> "+getInternalString());
  // //Calendar date = ISO8601.parse(getInternalString());
  // Calendar date = getInternalCalendar();
  // if (date != null) {
  // return date;
  // } else {
  // throw new ValueFormatException("empty value");
  // }
  // }

  /**
   * @see Value#getLong
   */
  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
    // setValueConsumed();

    // Calendar date = ISO8601.parse(getInternalString());
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

    // setValueConsumed();

    // Calendar date = ISO8601.parse(getInternalString());
    //
    // if (date != null) {
    // throw new ValueFormatException("cannot convert date to boolean");
    // } else {
    // throw new ValueFormatException("empty value");
    // }
  }

  /**
   * @see Value#getDouble
   */
  public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
    // setValueConsumed();

    // Calendar date = ISO8601.parse(getInternalString());
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

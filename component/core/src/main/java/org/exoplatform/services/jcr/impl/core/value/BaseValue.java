/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.value.ExtendedValue;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.log.ExoLogger;

/**
 * This class is the superclass of the type-specific
 * classes implementing the <code>Value</code> interfaces.
 *
 * @author Gennady Azarenkov
 *
 * @version $Id: BaseValue.java 12841 2007-02-16 08:58:38Z peterit $

 */
public abstract class BaseValue implements ExtendedValue {

  protected static Log log = ExoLogger.getLogger("jcr.BinaryValue");

  protected final int type;

  protected LocalTransientValueData data;

  protected TransientValueData internalData;

  /**
   * Package-private default constructor.
   */
  BaseValue(int type, TransientValueData data) throws IOException {
    this.type = type;
    this.internalData = data;
  }

  protected LocalTransientValueData getLocalData(boolean asStream) throws IOException {
    if(data == null)
      data = new LocalTransientValueData(asStream);
    
    return data;
  }
  
  /**
   * Returns the internal string representation of this value without modifying
   * the value state.
   * @return the internal string representation
   * @throws ValueFormatException if the value can not be represented as a
   * <code>String</code> or if the value is <code>null</code>.
   * @throws RepositoryException if another error occurs.
   */
  protected String getInternalString() throws ValueFormatException, RepositoryException {

    try {
      
      return new String(getLocalData(false).getAsByteArray(), Constants.DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new RepositoryException(Constants.DEFAULT_ENCODING + " not supported on this platform", e);
    } catch (IOException e) {
      throw new ValueFormatException("conversion to string failed: " + e.getMessage(), e);
    }
  }

  /**
   * @return the internal calendar
   * @throws ValueFormatException
   * @throws RepositoryException
   */
  protected Calendar getInternalCalendar()
    throws ValueFormatException, RepositoryException {
    try {
      if (type == PropertyType.DATE)
        return new JCRDateFormat().deserialize(new String(getLocalData(false).getAsByteArray(), Constants.DEFAULT_ENCODING));

      return JCRDateFormat.parse(new String(getLocalData(false).getAsByteArray(), Constants.DEFAULT_ENCODING));
    } catch(UnsupportedEncodingException e) {
      throw new RepositoryException(Constants.DEFAULT_ENCODING + " not supported on this platform", e);
    } catch (IOException e) {
      throw new ValueFormatException("conversion to date failed: " + e.getMessage(), e);
    }
  }

  /* (non-Javadoc)
   * @see javax.jcr.Value#getType()
   */
  public final int getType() {
    return type;
  }

  /* (non-Javadoc)
   * @see javax.jcr.Value#getDate()
   */
  public Calendar getDate() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    Calendar cal = getInternalCalendar();

    if (cal == null) {
      throw new ValueFormatException("not a valid date format "+getInternalString());
    } else {
      return cal;
    }
  }

  /* (non-Javadoc)
   * @see javax.jcr.Value#getLong()
   */
  public long getLong() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    try {
      return Long.parseLong(getInternalString());
    } catch (NumberFormatException e) {
      throw new ValueFormatException("conversion to long failed", e);
    }
  }

  /* (non-Javadoc)
   * @see javax.jcr.Value#getBoolean()
   */
  public boolean getBoolean() throws ValueFormatException,
      IllegalStateException, RepositoryException {
    return Boolean.valueOf(getInternalString()).booleanValue();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Value#getDouble()
   */
  public double getDouble() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    try {
      return Double.parseDouble(getInternalString());
    } catch (NumberFormatException e) {
      throw new ValueFormatException("conversion to double failed", e);
    }
  }

  /* (non-Javadoc)
   * @see javax.jcr.Value#getStream()
   */
  public InputStream getStream() throws ValueFormatException,
      RepositoryException {
    try {
      return getLocalData(true).getAsStream();
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /* (non-Javadoc)
   * @see javax.jcr.Value#getString()
   */
  public String getString() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    return getInternalString();
  }

  /**
   * @return
   * @throws ValueFormatException
   * @throws IllegalStateException
   * @throws RepositoryException
   */
  public String getReference() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    throw new ValueFormatException("Can not convert "+
          PropertyType.nameFromValue(type)+" to Reference");
  }

  /**
   * @return Returns the data.
   */
  public TransientValueData getInternalData() {
    return internalData;
  }

  /**
   * Returns the length of the value in bytes if the value is a PropertyType.BINARY,
   * otherwise it returns the number of characters needed to display the value in its string form
   * as defined in 6.2.6 Property Type Conversion.
   * Returns if the implementation cannot determine the length of the value..
   */
  public long getLength() {
    if(data == null)
      return internalData.getLength();
    else
      return data.getLength();
  }


  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if(!obj.getClass().equals(getClass()))
      return false;
    if (obj instanceof BaseValue) {
      BaseValue other = (BaseValue) obj;
      return getInternalData().equals(other.getInternalData());
    }
    return false;
  }

  public int getOrderNumber(){
    return data.getOrderNumber();
  }
  
  public void setOrderNumber(int order){
    data.setOrderNumber(order);
  }

  protected class LocalTransientValueData extends AbstractValueData {

    protected InputStream stream;

    protected byte[] bytes;

    protected final long length;

    /**
     * constructor creates brand new stream or brand new byte array copying shared data
     * @param data
     * @throws IOException
     */
    public LocalTransientValueData(boolean asStream) throws IOException {
      super(getInternalData().getOrderNumber());
      if (!asStream) {
        bytes = getInternalData().getAsByteArray();
        stream = null;
      } else {
        stream = getInternalData().getAsStream();
        bytes = null;
      }
      length = getInternalData().getLength();
    }

    public byte[] getAsByteArray() throws IllegalStateException, IOException {
      if(streamConsumed())
        throw new IllegalStateException("stream value has already been consumed");
      return bytes;
    }

    public InputStream getAsStream() throws IOException, IllegalStateException {
      if(bytesConsumed())
        throw new IllegalStateException("non-stream value has already been consumed");
      return stream;
    }

    public long getLength() {
      return length;
    }

    public boolean isByteArray() {
      return bytes != null;
    }

    private boolean streamConsumed() {
      return stream != null;
    }

    private boolean bytesConsumed() {
      return bytes != null;
    }

    @Override
    public TransientValueData createTransientCopy() {
      throw new RuntimeException("LocalTransientValueData.createTransientCopy()");
    }

  }
}
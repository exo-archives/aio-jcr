/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.value.ExtendedBinaryValue;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.value.BaseValue.LocalTransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.EditableValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * a binary value implementation.
 * 
 * @author Gennady Azarenkov
 */
public class BinaryValue extends BaseValue implements ExtendedBinaryValue {

  public static final int TYPE = PropertyType.BINARY;

  protected EditableValueData changedData = null;
  
  protected boolean changed = false;
  
  protected static Log log = ExoLogger.getLogger("jcr.BinaryValue");
  
  /**
   * @param text
   * @throws IOException
   */
  public BinaryValue(String text) throws IOException {
    super(TYPE, new TransientValueData(text));
  }

  
  /**
   * @param stream
   * @param fileCleaner
   * @param tempDirectory
   * @param maxFufferSize
   * @throws IOException
   */
  public BinaryValue(InputStream stream, FileCleaner fileCleaner,
      File tempDirectory, int maxFufferSize) throws IOException {
    this(new TransientValueData(stream), fileCleaner, tempDirectory, maxFufferSize);
  }
  
  BinaryValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }
  
  /** used in ValueFactory.loadValue */
  BinaryValue(TransientValueData data, FileCleaner fileCleaner,
      File tempDirectory, int maxFufferSize) throws IOException {
    super(TYPE, data);
    internalData.setFileCleaner(fileCleaner);
    internalData.setTempDirectory(tempDirectory);
    internalData.setMaxBufferSize(maxFufferSize);
  }

  @Override
  public TransientValueData getInternalData() {
    if (changedData != null)
      return changedData;
    
    return super.getInternalData();
  }

  @Override
  protected LocalTransientValueData getLocalData(boolean asStream) throws IOException {
    
    if (this.changed) {
      // reset to be recreated with new stream/bytes
      this.data = null;
      this.changed = false;
    }
    
    return super.getLocalData(asStream);
  }
  
//  @Override
//  public InputStream getStream() throws ValueFormatException, RepositoryException {
//    try {
//      return getInternalData().getAsStream();
//    } catch (IOException e) {
//      throw new RepositoryException(e);
//    }
//  }

//  /**
//   * Returns the internal string representation of this value.
//   * @return the internal string representation
//   * @throws ValueFormatException if the value can not be represented as a
//   * <code>String</code> or if the value is <code>null</code>.
//   * @throws RepositoryException if another error occurs.
//   */
//  protected String getInternalString() throws ValueFormatException, RepositoryException {
//
//    try {
//      return new String(getInternalData().getAsByteArray(), Constants.DEFAULT_ENCODING);
//    } catch (UnsupportedEncodingException e) {
//      throw new RepositoryException(Constants.DEFAULT_ENCODING + " not supported on this platform", e);
//    } catch (IOException e) {
//      throw new ValueFormatException("conversion to string failed: " + e.getMessage(), e);
//    }
//  }

  public String getReference() throws ValueFormatException,
      IllegalStateException, RepositoryException {
    return getInternalString();
  }

  /**
   * Update with <code>length</code> bytes from the specified InputStream
   * <code>stream</code> to this binary value at <code>position</code>
   * 
   * @param   stream     the data.
   * @param   length   the number of bytes from buffer to write.
   * @param   position position in file to write data  
   * */
  public void update(InputStream stream, int length, long position) throws IOException, RepositoryException {
    if (changedData == null) {
      changedData = this.getInternalData().createEditableCopy();
    }
    
    this.changedData.update(stream, length, position);
    
    this.changed = true;
  }
  
  
  /**
   * Truncates binary value to <code> size </code>
   * 
   * @param size
   * @throws IOException
   */
  public void setLength(long size) throws IOException, RepositoryException {
    if (changedData == null) {
      changedData = this.getInternalData().createEditableCopy();
    }
    
    this.changedData.setLength(size);
    
    this.changed = true;
  }
  
  
}
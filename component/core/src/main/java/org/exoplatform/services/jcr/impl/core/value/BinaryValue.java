/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.value;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.value.ExtendedBinaryValue;
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
    super(TYPE, new TransientValueData(stream));
    internalData.setFileCleaner(fileCleaner);
    internalData.setTempDirectory(tempDirectory);
    internalData.setMaxBufferSize(maxFufferSize);
  }
  
  BinaryValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }

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
  public void update(InputStream stream, int length, long position) throws IOException {
    this.getInternalData().update(stream, length, position);
  }
  
  
  /**
   * Truncates binary value to <code> size </code>
   * 
   * @param size
   * @throws IOException
   */
  public void truncate(long size) throws IOException{
    this.getInternalData().truncate(size);
  }
  
  
}
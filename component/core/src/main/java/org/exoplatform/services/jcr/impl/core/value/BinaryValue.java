/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * a binary value implementation.
 * 
 * @author Gennady Azarenkov
 */
public class BinaryValue extends BaseValue {

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
}
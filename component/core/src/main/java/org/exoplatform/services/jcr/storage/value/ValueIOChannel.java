/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.storage.value;

import java.io.IOException;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.jdbc.ValueReference;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueIOChannel.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ValueIOChannel {
  
  /**
   * reads values
   * @param propertyId
   * @param maxBufferSize - maximum size when value will be read to memory buffer
   * @return List of ValueData
   * @throws IOException
   */
  ValueData read(String propertyId, int orderNumber, int maxBufferSize) throws IOException;
  
  /**
   * writes values
   * @param propertyId
   * @param data - list of ValueData
   * @throws IOException
   */
  void write(String propertyId, ValueData data) throws IOException;

  /**
   * deletes values
   * @param propertyId
   * @throws IOException
   */
  boolean delete(String propertyId) throws IOException;

  /**
   * closes channel 
   */
  void close();
  
  /**
   * Return this value storage id. 
   */
  String getStorageId();
}

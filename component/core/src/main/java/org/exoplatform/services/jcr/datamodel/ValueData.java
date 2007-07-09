/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.datamodel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueData.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ValueData {
  
  /**
   * set number of this value (values should be ordered)
   */
  void setOrderNumber(int orderNum);
  
  /**
   * @return number of this value (values should be ordered)
   */
  int getOrderNumber();
  
  /**
   * @return true if data rendered as byte array, false otherwise
   */
  boolean isByteArray();
  
  /**
   * @return this value data as array of bytes
   * @throws IllegalStateException 
   */
  byte[] getAsByteArray() throws IllegalStateException, IOException;
  
  /**
   * renders this value data as stream of bytes
   * NOTE: client is responsible for closing this stream, else IllegalStateException occurs in close()! 
   * @return this value data as stream of bytes
   * @throws IOException
   */
  InputStream getAsStream() throws IOException;
  
  
  /**
   * Return this data length in bytes
   */
  long getLength();
  

}

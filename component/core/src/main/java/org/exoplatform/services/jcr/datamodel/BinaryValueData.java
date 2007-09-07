/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.datamodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface BinaryValueData extends ValueData {

  /**
   * Set length of the Value in bytes to the specified <code>size</code>.
   *  
   * If <code>size</code> is lower 0 the IOException exception will be thrown. 
   * 
   * This operation can be used both for extend and for truncate the Value size. 
   * 
   * This method used internally in update operation in case of extending the size 
   * to the given position.
   * 
   * @param size
   * @throws IOException
   */
  void setLength(long size) throws IOException;
  
  /** 
   * Update with <code>length</code> bytes from the specified <code>stream</code> 
   * to this value data at <code>position</code>. 
   * 
   * If <code>position</code> is lower 0 the IOException exception will be thrown. 
   * 
   * If  <code>position</code> is higher of current Value length 
   * the Value length will be increased before to <code>position</code> size 
   * and <code>length</code> bytes will be added after the <code>position</code>. 
   * 
   * @param stream
   *          the data.
   * @param length
   *          the number of bytes from buffer to write.
   * @param position
   *          position in file to write data
   * 
   * @throws IOException
   */
  void update(InputStream stream, long length, long position) throws IOException;
  
  long read(OutputStream stream, long length, long position) throws IOException;
  
  // TOOD check use case
  //InputStream read(long length, long position) throws IOException;
}

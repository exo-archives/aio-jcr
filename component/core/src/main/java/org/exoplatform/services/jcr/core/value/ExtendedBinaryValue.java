/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.core.value;

import java.io.IOException;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ExtendedBinaryValue extends ExtendedValue{

  /**
   * Writes <code>len</code> bytes from the <code>offset</code> in byte array 
   * starting at <code>position</code> in this binary value.
   *
   * @param   buff     the data.
   * @param   offset   the start offset in the data.
   * @param   length   the number of bytes from buffer to write.
   * @param   position position in file to write data  
   * */
  void writeBytes(byte[] buff, int offset, int length, long position) throws IOException ;
 
  /**
   * Truncates binary value to <code> size </code>
   * 
   * @param size
   * @throws IOException
   */
  void truncate(long size) throws IOException;
  
}

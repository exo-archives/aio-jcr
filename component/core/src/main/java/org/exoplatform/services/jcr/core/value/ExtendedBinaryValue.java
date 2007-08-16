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
public interface ExtendedBinaryValue {

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this binary value.
   *
   * @param   buff  the data.
   * @param   off   the start offset in the data.
   * @param   len   the number of bytes to write.  
   * */
  void writeBytes(byte[] buff, int off, int len) throws IOException;
  
}

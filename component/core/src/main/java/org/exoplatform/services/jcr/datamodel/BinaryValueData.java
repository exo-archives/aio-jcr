/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.datamodel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface BinaryValueData extends ValueData {

  void truncate(long size) throws IOException;
  
  /**
   * Update with <code>length</code> bytes from the specified InputStream
   * <code>stream</code> to this value data at <code>position</code>
   * 
   * @author Karpenko
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
}

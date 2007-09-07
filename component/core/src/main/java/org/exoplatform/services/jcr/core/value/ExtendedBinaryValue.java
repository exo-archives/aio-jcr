/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.core.value;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ExtendedBinaryValue extends ExtendedValue {

  /**
   * Update with <code>length</code> bytes from the specified InputStream
   * <code>stream</code> to this binary value at <code>position</code>
   * 
   * @param   stream     the data.
   * @param   length   the number of bytes from buffer to write.
   * @param   position position in file to write data  
   * */
  void update(InputStream stream, int length, long position) throws IOException, RepositoryException ;
 
  /**
   * Truncates binary value to <code> size </code>
   * 
   * @param size
   * @throws IOException
   */
  void setLength(long size) throws IOException, RepositoryException;
 
  long read(OutputStream stream, long length, long position) throws IOException, RepositoryException ;
  
}

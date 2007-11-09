/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.core.value;

import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ReadableBinaryValue extends ExtendedValue {

  /**
   * Read <code>length</code> bytes from the binary value at <code>position</code>
   * to the <code>stream</code>. 
   * 
   * @param stream - destenation OutputStream
   * @param length - data length to be read
   * @param position - position in value data from which the read will be performed 
   * @return - The number of bytes, possibly zero,
   *          that were actually transferred
   * @throws IOException
   * @throws RepositoryException
   */
  long read(OutputStream stream, long length, long position) throws IOException, RepositoryException ;
  
}

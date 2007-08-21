/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.core;

import java.io.InputStream;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ExtendedProperty extends Property {

  /**
   * Write binary data portion to the property value data.
   * 
   * @param index - value index, 0 for first-in-multivalue/single-value, 1 - second etc.
   * @param value - stream with the data portion
   * @param length - value bytes count will be written
   * @param position - position in the property value data from which the value
   *          will be written
   */
  void updateValue(int index, InputStream value, long length, long position) 
      throws ValueFormatException, VersionException, LockException, 
      ConstraintViolationException, RepositoryException;

}

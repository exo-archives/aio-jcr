/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by The eXo Platform SARL        .<br/>
 * iterator of PlainChangesLog
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ChangesLogIterator {

  private Iterator <PlainChangesLog> internalIterator; 

  public ChangesLogIterator(Collection <PlainChangesLog> logs) {
    internalIterator = logs.iterator();
  }

  /**
   * @return if there is next changes log 
   */
  public boolean hasNextLog() {
    return internalIterator.hasNext();
  }
  
  /**
   * @return next changes log
   */
  public PlainChangesLog nextLog() {
    return internalIterator.next();
  }
  
}

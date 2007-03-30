/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: ItemDataKeeper.java 13421 2007-03-15 10:46:47Z geaz $
 */

public interface ItemDataKeeper {
  /**
   * Saves the list of changes from this storage
   * @param items for commit
   * @throws InvalidItemStateException 
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured
   */
  void save(ItemStateChangesLog changes) throws InvalidItemStateException,
    UnsupportedOperationException, RepositoryException;

}

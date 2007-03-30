/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.dataflow.persistent;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;


/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: WorkspaceStorageDataManager.java 12843 2007-02-16 09:11:18Z peterit $
 */
public abstract interface WorkspaceStorageDataManager extends ItemDataConsumer {
  /**
   * Saves the list of changes from this storage
   * @param items for commit
   * @throws InvalidItemStateException 
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured

   */
  //void save(ItemDataChangesLog changes) throws InvalidItemStateException,
  //UnsupportedOperationException, RepositoryException;

}

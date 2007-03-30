/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.datamodel;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataVisitor;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ItemData.java 12405 2007-01-29 21:37:19Z peterit $
 */

public interface ItemData {
	
	/**
	 * @return QPath of this item
	 */
	
	InternalQPath getQPath();

	/**
	 * @return UUID
	 */
	String getUUID();

	/**
	 * @return number of item version retrieved from container. 
	 * If item is not persisted returns -1;  
	 */
	int getPersistedVersion();
	
	/**
	 * @return parent NodeData. Parent is initialized on demand. It is possible that
	 * the method return null for root node only (but not neccessary)
	 * @throws IllegalStateException if parent can not be initialized 
	 * (for example was deleted by other session)
	 */
	String getParentUUID();

	
	/**
	 * @return if item data is node data
	 */
	boolean isNode();
  
  /**
   * Accept visitor
   * @param visitor
   * @throws RepositoryException
   */
  void accept(ItemDataVisitor visitor) throws RepositoryException;

}
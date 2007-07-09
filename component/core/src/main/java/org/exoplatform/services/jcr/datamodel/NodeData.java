/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.datamodel;

import org.exoplatform.services.jcr.access.AccessControlList;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: NodeData.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface NodeData extends ItemData {
	
	
	/**
	 * @return this node order number 
	 */
	int getOrderNumber();

	/**
	 * @return name of primary node type of this node. The jcr:primaryType property
	 * is loaded twice here and lazy initialized like others.
	 */
	InternalQName getPrimaryTypeName();
	
	/**
	 * @return names of mixin node types. The jcr:mixinTypes property
	 * is loaded twice here and lazy initialized like others.
	 * return empty array if no mixin types found
	 */
	InternalQName[] getMixinTypeNames();
	

	/**
	 * @return access control list either this node's data or nearest ancestor's data 
	 */
	AccessControlList getACL();
  
  /**
   * @param acl
   */
  void setACL(AccessControlList acl);

}
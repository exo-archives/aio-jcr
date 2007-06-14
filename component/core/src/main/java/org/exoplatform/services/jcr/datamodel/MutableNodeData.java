/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.datamodel;

import org.exoplatform.services.jcr.access.AccessControlList;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: MutableNodeData.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface MutableNodeData extends NodeData, MutableItemData {
	 
	void setOrderNumber(int orderNum);

	void setMixinTypeNames(InternalQName[] mixinTypeNames);

	void setIdentifier(String identifier);
	
	void setACL(AccessControlList acl);
	
}
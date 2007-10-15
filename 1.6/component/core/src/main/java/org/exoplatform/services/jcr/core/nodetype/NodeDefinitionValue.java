/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

import java.util.List;


/**
 * Created by The eXo Platform SARL        .<br/>
 * NodeDefinition value object
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: NodeDefinitionValue.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class NodeDefinitionValue extends ItemDefinitionValue {

  private String defaultNodeTypeName;

	private List requiredNodeTypeNames;

	private boolean sameNameSiblings;

	
	public NodeDefinitionValue() {
	}

	/**
	 * @return Returns the defaultNodeTypeName.
	 */
	public String getDefaultNodeTypeName() {
		return defaultNodeTypeName;
	}
	/**
	 * @param defaultNodeTypeName The defaultNodeTypeName to set.
	 */
	public void setDefaultNodeTypeName(String defaultNodeTypeName) {
		this.defaultNodeTypeName = defaultNodeTypeName;
	}
	/**
	 * @return Returns the sameNameSiblings.
	 */
	public boolean isSameNameSiblings() {
		return sameNameSiblings;
	}
	/**
	 * @param sameNameSiblings The sameNameSiblings to set.
	 */
	public void setSameNameSiblings(boolean multiple) {
		this.sameNameSiblings = multiple;
	}
	/**
	 * @return Returns the requiredNodeTypeNames.
	 */
	public List getRequiredNodeTypeNames() {
		return requiredNodeTypeNames;
	}
	/**
	 * @param requiredNodeTypeNames The requiredNodeTypeNames to set.
	 */
	public void setRequiredNodeTypeNames(List requiredNodeTypeNames) {
		this.requiredNodeTypeNames = requiredNodeTypeNames;
	}
}

/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

import java.util.List;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: PropertyDefinitionValue.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class PropertyDefinitionValue extends ItemDefinitionValue {
	
	private int requiredType;

	private List valueConstraints;

	private List defaultValueStrings;

	private boolean multiple;

	public PropertyDefinitionValue() {
	}

	/**
	 * @return Returns the defaultValues.
	 */
	public List getDefaultValueStrings() {
		return defaultValueStrings;
	}
	/**
	 * @param defaultValues The defaultValues to set.
	 */
	public void setDefaultValueStrings(List defaultValues) {
		this.defaultValueStrings = defaultValues;
	}
	/**
	 * @return Returns the multiple.
	 */
	public boolean isMultiple() {
		return multiple;
	}
	/**
	 * @param multiple The multiple to set.
	 */
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}
	/**
	 * @return Returns the requiredType.
	 */
	public int getRequiredType() {
		return requiredType;
	}
	/**
	 * @param requiredType The requiredType to set.
	 */
	public void setRequiredType(int requiredType) {
		this.requiredType = requiredType;
	}
	/**
	 * @return Returns the valueConstraints.
	 */
	public List getValueConstraints() {
		return valueConstraints;
	}
	/**
	 * @param valueConstraints The valueConstraints to set.
	 */
	public void setValueConstraints(List valueConstraints) {
		this.valueConstraints = valueConstraints;
	}
}

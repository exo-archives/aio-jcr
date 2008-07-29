/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.core.nodetype;

import java.util.List;


/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: PropertyDefinitionValue.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class PropertyDefinitionValue extends ItemDefinitionValue {
	
	private int requiredType;

	private List<String> valueConstraints;

	private List<String> defaultValueStrings;

	private boolean multiple;

	public PropertyDefinitionValue() {
	}

	/**
	 * @return Returns the defaultValues.
	 */
	public List<String> getDefaultValueStrings() {
		return defaultValueStrings;
	}
	/**
	 * @param defaultValues The defaultValues to set.
	 */
	public void setDefaultValueStrings(List<String> defaultValues) {
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
	public List<String> getValueConstraints() {
		return valueConstraints;
	}
	/**
	 * @param valueConstraints The valueConstraints to set.
	 */
	public void setValueConstraints(List<String> valueConstraints) {
		this.valueConstraints = valueConstraints;
	}
}

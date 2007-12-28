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

/**
 * Created by The eXo Platform SAS.<br/>
 * ItemDefinition value object
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ItemDefinitionValue.java 12843 2007-02-16 09:11:18Z peterit $
 */

public abstract class ItemDefinitionValue {
	
	protected String name;

	protected boolean autoCreate;

	protected int onVersion;

	protected boolean readOnly;

	protected boolean mandatory;

	public ItemDefinitionValue() {
	}

	/**
	 * @return Returns the autoCreate.
	 */
	public boolean isAutoCreate() {
		return autoCreate;
	}
	/**
	 * @param autoCreate The autoCreate to set.
	 */
	public void setAutoCreate(boolean autoCreate) {
		this.autoCreate = autoCreate;
	}
	/**
	 * @return Returns the mandatory.
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	/**
	 * @param mandatory The mandatory to set.
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the onVersion.
	 */
	public int getOnVersion() {
		return onVersion;
	}
	/**
	 * @param onVersion The onVersion to set.
	 */
	public void setOnVersion(int onVersion) {
		this.onVersion = onVersion;
	}
	/**
	 * @return Returns the readOnly.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	/**
	 * @param readOnly The readOnly to set.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}

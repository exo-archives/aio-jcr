/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

/**
 * Created by The eXo Platform SARL        .<br/>
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

/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.datamodel;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: Uuid.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class Identifier {
	
	private final String string;

	public Identifier(String stringValue) {
		this.string = stringValue;
	}
	
	public Identifier(byte[] value) {
		this.string = new String(value);
	}

	/**
	 * @return Returns the stringValue.
	 */
	public String getString() {
		return string;
	}
}

/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: CredentialsImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class CredentialsImpl implements Credentials {
	
	private SimpleCredentials simpleCredentials;

    public CredentialsImpl(String userID, char[] password) {
    	this.simpleCredentials = new SimpleCredentials(userID, password);
    }	

	/**
	 * @param name
	 * @return
	 */
	public Object getAttribute(String name) {
		return simpleCredentials.getAttribute(name);
	}
	/**
	 * @return
	 */
	public String[] getAttributeNames() {
		return simpleCredentials.getAttributeNames();
	}
	/**
	 * @return
	 */
	public char[] getPassword() {
		return simpleCredentials.getPassword();
	}
	/**
	 * @return
	 */
	public String getUserID() {
		return simpleCredentials.getUserID();
	}

	/**
	 * @param name
	 */
	public void removeAttribute(String name) {
		simpleCredentials.removeAttribute(name);
	}
	/**
	 * @param name
	 * @param value
	 */
	public void setAttribute(String name, Object value) {
		simpleCredentials.setAttribute(name, value);
	}

	public String toString() {
		return simpleCredentials.toString();
	}
	
	public int hashCode() {
		return simpleCredentials.hashCode();
	}

	public boolean equals(Object arg0) {
		return simpleCredentials.equals(arg0);
	}

}

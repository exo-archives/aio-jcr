/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MimeTypes {
	
  public static final String ALL = "*/*";
	private String[] mimeTypes;
	
	public MimeTypes(String s) {
		mimeTypes = HeaderUtils.parse(s);
	}

	public String[] getMimeTypes() {
		return mimeTypes;
	}

	public String getMimeType(int i) {
		return mimeTypes[i];
	}

	public boolean hasMimeType(String s) {
		for(String m : mimeTypes) {
			if(m.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

}

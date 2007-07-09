/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.observation;

import javax.jcr.observation.EventListener;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ThrowableEventListener.java 12843 2007-02-16 09:11:18Z peterit $
 */

public abstract class ThrowableEventListener implements EventListener {

	private Throwable exception;

	public ThrowableEventListener(Throwable exception) {
		super();
		this.exception = exception;
	}
	
	/**
	 * @return Returns the exception.
	 */
	public Throwable getException() {
		return exception;
	}
	

}

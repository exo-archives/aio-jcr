/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class InputEntityTransformer implements GenericEntityTransformer {
	
	protected Class<?> entityType;
	
	public void setType(Class<?> entityType) {
		this.entityType = entityType;
	}

	public Class<?> getType() {
		return entityType;
	}

	abstract public Object readFrom(InputStream entityDataStream) throws IOException;

}

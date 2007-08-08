/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Object implement this interface should be able to write
 * entity data by own methods.<br/>
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface SerializableEntity {
	public void writeObject(OutputStream out) throws IOException;
}

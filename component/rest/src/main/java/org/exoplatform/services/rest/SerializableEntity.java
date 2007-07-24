/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Object implement this interface should be able to read/write
 * entity data by own methods.<br/>
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface SerializableEntity {
	public void readObject(InputStream in) throws IOException;
	public void writeObject(OutputStream out) throws IOException;
}

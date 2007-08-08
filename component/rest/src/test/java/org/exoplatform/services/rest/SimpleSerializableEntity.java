/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.IOException;
import java.io.OutputStream;

import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SimpleSerializableEntity implements SerializableEntity {

	String data;

	public SimpleSerializableEntity() {
	}

	public void writeObject(OutputStream out) throws IOException {
		out.write(data.getBytes());
	}

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class PassthroughOutputTransformer extends OutputEntityTransformer {

	@Override
	public void writeTo(Object entity, OutputStream entityDataStream)
			throws IOException {
    InputStream entity_ = (InputStream)entity;
    byte[] buf = new byte[4096];
    int rd = -1;
    while((rd = entity_.read(buf)) != -1)
      entityDataStream.write(buf, 0, rd);    
	}

}

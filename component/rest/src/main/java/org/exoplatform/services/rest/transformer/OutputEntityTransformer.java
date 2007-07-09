/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class OutputEntityTransformer implements GenericEntityTransformer {
	
  abstract public void writeTo(Object entity, OutputStream entityDataStream) throws IOException;
	
}

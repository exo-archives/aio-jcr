/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry.transformer;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.TransformerException;

import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RegistryEntryOutputTransformer extends OutputEntityTransformer {

	@Override
	public void writeTo(Object entity, OutputStream entityDataStream)
			throws IOException {
		
		RegistryEntry regEntry = (RegistryEntry) entity;
		PassthroughOutputTransformer transformer = new PassthroughOutputTransformer();
		try {
			transformer.writeTo(regEntry.getAsInputStream(), entityDataStream);
		} catch (TransformerException tre) {
			throw new IOException("Can't get RegistryEntry as stream " + tre);
		}
	}

}

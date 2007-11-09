/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry.transformer;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.rest.transformer.InputEntityTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RegistryEntryInputTransformer extends InputEntityTransformer {

	public RegistryEntry readFrom(InputStream entityDataStream)
			throws IOException {
		try {
			return RegistryEntry.parse(entityDataStream);
		} catch (ParserConfigurationException pce) {
			throw new IOException("Can't read from input stream " + pce);
		} catch (SAXException saxe) {
			throw new IOException("Can't read from input stream " + saxe);
		}
	}

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JAXBInputTransformer extends InputEntityTransformer {

	@Override
	public Object readFrom(InputStream entityDataStream) throws IOException {
    try {
    	JAXBContext jaxbContext = JAXBContext.newInstance(entityType);
      return jaxbContext.createUnmarshaller().unmarshal(entityDataStream);
    } catch(JAXBException jaxbe) {
      throw new IOException("Can't transform InputStream to Object: " + jaxbe);
    }
	}

}

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class JAXBOutputTransformer extends OutputEntityTransformer {

	@Override
	public void writeTo(Object entity, OutputStream entityDataStream)
			throws IOException {
    try{
    	JAXBContext jaxbContext = JAXBContext.newInstance(entity.getClass());
      jaxbContext.createMarshaller().marshal(entity, entityDataStream);
    } catch(JAXBException jaxbe) {
      throw new IOException("Can't transform Object to OutputStream: " + jaxbe);
    }
 	}

}

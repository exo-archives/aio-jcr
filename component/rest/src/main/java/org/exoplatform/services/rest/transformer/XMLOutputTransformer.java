/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.transformer;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLOutputTransformer extends OutputEntityTransformer {

	@Override
	public void writeTo(Object entity, OutputStream entityDataStream)
			throws IOException {
    Document entity_ = (Document)entity;
    try {
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource(entity_),
          new StreamResult(entityDataStream));
    } catch (TransformerException tre) {
      throw new IOException("Can't write to output stream " + tre);
    }
	}

}

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
 * This type of transformer can write XML in output stream.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLOutputTransformer extends OutputEntityTransformer {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.OutputEntityTransformer#writeTo
   *      (java.lang.Object, java.io.OutputStream)
   */
  @Override
  public final void writeTo(Object entity, OutputStream entityDataStream) throws IOException {
    Document e = (Document) entity;
    try {
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource(e),
          new StreamResult(entityDataStream));
    } catch (TransformerException tre) {
      throw new IOException("Can't write to output stream " + tre);
    }
  }

}
